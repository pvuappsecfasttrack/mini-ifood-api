# CLIENT SETUP HIGHLIGHTS:
# - Search for CLIENT_TODO markers to tailor this scanner for a new repository.

#!/usr/bin/env python3

import argparse
import json
import os
import subprocess
import sys
import time
from datetime import datetime
from pathlib import Path

import requests

ENDPOINT_PATH = "/api/java-agent/create-vulnerabilities"
SEMGREP_IMAGE = "returntocorp/semgrep"
ODC_IMAGE = "owasp/dependency-check:12.1.9"
OUTPUT_FILENAME = "semgrep-report.json"
ODC_OUTPUT_FILENAME = "dependency-check-report.json"
DEFAULT_EXCLUDES = ["node_modules"]
SEVERITY_ENUM = {"Critical": 1, "High": 2, "Medium": 3, "Low": 4}
TOOL_ID = 2
ODC_TOOL_ID = 3
DEFAULT_PAYLOAD_MAX_POST_BYTES = 950000


def log(msg: str):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] {msg}", flush=True)


def shell(cmd, check=True, stream=False):
    if stream:
        p = subprocess.Popen(cmd)
        rc = p.wait()
        if check and rc != 0:
            raise RuntimeError(f"Command failed ({rc}): {' '.join(cmd)}")
        return rc, "", ""

    p = subprocess.run(cmd, text=True, capture_output=True)
    if check and p.returncode != 0:
        raise RuntimeError(f"Command failed ({p.returncode}): {' '.join(cmd)}\nSTDOUT:\n{p.stdout}\nSTDERR:\n{p.stderr}")
    return p.returncode, p.stdout, p.stderr


def docker_available():
    try:
        shell(["docker", "--version"], check=True)
        return True
    except Exception:
        return False


def read_config_from_script_dir():
    cfg_path = Path(__file__).with_name("asft_config.properties")
    if not cfg_path.exists():
        raise FileNotFoundError(f"Configuration file not found: {cfg_path.name}")
    data = json.loads(cfg_path.read_text(encoding="utf-8"))
    controller_base = data.get("asftUrl")
    upload_base = data.get("asftUploadBaseUrl") or controller_base
    token = data.get("asftAuthKey")
    project_name = data.get("applicationName")
    if not controller_base or not token:
        raise ValueError("asft_config.properties is missing 'asftUrl' or 'asftAuthKey'.")
    return controller_base.rstrip("/"), upload_base.rstrip("/"), token, project_name


def get_scan_version(project_dir: Path):
    code, out, _ = shell(["git", "-C", str(project_dir), "rev-parse", "HEAD"], check=False)
    commit = out.strip() if code == 0 else ""
    return commit or str(int(time.time()))


def run_semgrep_docker(project_dir: Path, excludes, script_dir: Path):
    out_host_path = script_dir / OUTPUT_FILENAME
    out_container_path = f"/out/{OUTPUT_FILENAME}"
    user_flag = ["--user", f"{os.getuid()}:{os.getgid()}"] if hasattr(os, "getuid") else []
    cmd = [
        "docker", "run", "--rm", *user_flag,
        "-v", f"{str(project_dir)}:/src", "-w", "/src",
        "-v", f"{str(script_dir)}:/out",
        SEMGREP_IMAGE, "semgrep", "--config=auto", "--json", "--output", out_container_path,
    ]
    for ex in list(excludes or []) + DEFAULT_EXCLUDES:
        cmd.extend(["--exclude", ex])
    log(f"Executing Semgrep container command: {' '.join(cmd)}")
    code, out, err = shell(cmd, check=False, stream=True)
    if not out_host_path.exists():
        raise RuntimeError(f"Semgrep JSON report missing. ExitCode={code}\nSTDOUT:\n{out}\nSTDERR:\n{err}")
    return out_host_path


def run_dependency_check_docker(project_dir: Path, script_dir: Path):
    out_host_path = script_dir / ODC_OUTPUT_FILENAME
    dc_data_dir = script_dir / ".odc-data"
    dc_data_dir.mkdir(exist_ok=True)
    user_flag = ["--user", f"{os.getuid()}:{os.getgid()}"] if hasattr(os, "getuid") else []
    cmd = [
        "docker", "run", "--rm", *user_flag,
        "-v", f"{str(project_dir)}:/src", "-w", "/src",
        "-v", f"{str(dc_data_dir)}:/usr/share/dependency-check/data",
        "-v", f"{str(script_dir)}:/report",
    ]
    nvd_api_key = os.environ.get("NVD_API_KEY")
    if nvd_api_key:
        cmd.extend(["-e", f"NVD_API_KEY={nvd_api_key}"])
    cmd += [ODC_IMAGE, "--scan", "/src", "--format", "JSON", "--out", "/report", "--project", "asft-odc"]
    log(f"Executing ODC container command: {' '.join(cmd)}")
    code, out, err = shell(cmd, check=False, stream=True)
    if code != 0:
        log(f"[ERROR] Dependency-Check execution failed (exit code {code}). Continuing with available SAST results.")
    if not out_host_path.exists():
        log(f"[ERROR] Dependency-Check JSON report missing. ExitCode={code}. Continuing without ODC findings.")
        return None
    return out_host_path


def map_severity_to_enum(sev_str: str) -> int:
    s = (sev_str or "Medium").strip().lower()
    if s in ("critical", "crit"):
        return SEVERITY_ENUM["Critical"]
    if s == "high":
        return SEVERITY_ENUM["High"]
    if s in ("low", "info", "information", "note"):
        return SEVERITY_ENUM["Low"]
    return SEVERITY_ENUM["Medium"]


def build_vulnerabilities(semgrep_json: dict, scan_version: str, project_name: str) -> list:
    results = []
    items = semgrep_json.get("results", []) if isinstance(semgrep_json, dict) else []
    for r in items:
        extra = r.get("extra") or {}
        results.append({
            "severity": map_severity_to_enum(extra.get("severity")),
            "bugType": r.get("check_id"),
            "codeLocation": r.get("path"),
            "line": (r.get("start") or {}).get("line"),
            "message": extra.get("message"),
            "metadata": extra.get("metadata") or {},
            "scanTool": TOOL_ID,
            "scanVersion": scan_version,
            "projectName": project_name,
        })
    return results


def build_vulnerabilities_from_odc(odc_json, scan_version, project_name) -> list:
    results = []
    for dep in (odc_json or {}).get("dependencies", []):
        file_path = dep.get("filePath") or dep.get("fileName") or ""
        for v in dep.get("vulnerabilities", []) or []:
            results.append({
                "severity": map_severity_to_enum(v.get("severity")),
                "bugType": v.get("name") or v.get("id"),
                "codeLocation": file_path,
                "line": None,
                "message": (v.get("description") or v.get("title") or "")[:2000],
                "metadata": {
                    "odc_source": True,
                    "filePath": file_path,
                    "cvss": v.get("cvssv3") or v.get("cvssv2") or {},
                    "cwe": v.get("cwe"),
                    "references": v.get("references") or [],
                    "source": v.get("source"),
                },
                "scanTool": ODC_TOOL_ID,
                "scanVersion": scan_version,
                "projectName": project_name,
            })
    return results


def post_results(base_url: str, token: str, payload: dict, timeout=120):
    url = f"{base_url}/{ENDPOINT_PATH.lstrip('/')}"
    headers = {"Authorization": f"{token}", "Content-Type": "application/json"}
    auth_preview = f"{token[:4]}...{token[-4:]}" if len(token) >= 8 else "***"
    vuln_count = len(payload.get("vulnerabilities", [])) if isinstance(payload, dict) else "unknown"
    log(f"[DEBUG] POST URL: {url}")
    log(f"[DEBUG] Request headers: Authorization=<masked:{auth_preview}> Content-Type={headers['Content-Type']}")
    log(f"[DEBUG] Request timeout: {timeout}s")
    log(f"[DEBUG] Vulnerability payload count: {vuln_count}")
    resp = requests.post(url, headers=headers, json=payload, timeout=timeout)
    log(f"[DEBUG] Response status: HTTP {resp.status_code}")
    log(f"[DEBUG] Response body length: {len(resp.text)}")
    return resp


def main():
    parser = argparse.ArgumentParser(description="Scan Semgrep and optional ODC, then POST results to ASFT server.")
    parser.add_argument("project_dir")
    parser.add_argument("--with-odc", action="store_true")
    parser.add_argument("--exclude", action="append")
    args = parser.parse_args()

    project_dir = Path(args.project_dir).resolve()
    if not project_dir.is_dir():
        print(f"❌ invalid project_dir: {project_dir}", file=sys.stderr)
        sys.exit(2)
    if not docker_available():
        print("❌ Docker is not ready.", file=sys.stderr)
        sys.exit(3)

    script_dir = Path(__file__).resolve().parent
    controller_base_url, upload_base_url, token, project_name = read_config_from_script_dir()
    scan_version = get_scan_version(project_dir)
    log(f"[DEBUG] ASFT controller URL: {controller_base_url}")
    log(f"[DEBUG] SAST/SCA upload base URL: {upload_base_url}")

    log("Running Semgrep in Docker...")
    semgrep_out = run_semgrep_docker(project_dir, args.exclude, script_dir)
    semgrep_json = json.loads(semgrep_out.read_text(encoding="utf-8"))
    vulns = build_vulnerabilities(semgrep_json, scan_version, project_name)

    if args.with_odc:
        log("Running OWASP Dependency-Check in Docker...")
        odc_out = run_dependency_check_docker(project_dir, script_dir)
        if odc_out is not None:
            odc_json = json.loads(odc_out.read_text(encoding="utf-8"))
            vulns.extend(build_vulnerabilities_from_odc(odc_json, scan_version, project_name))
        else:
            vulns.append({
                "severity": SEVERITY_ENUM["Low"],
                "bugType": "ODC_SCAN_EXECUTION_ERROR",
                "codeLocation": "security/scanner/sast_scan.py",
                "line": None,
                "message": "OWASP Dependency-Check failed to execute or produce a report. See workflow logs for details.",
                "metadata": {"odc_error": True},
                "scanTool": ODC_TOOL_ID,
                "scanVersion": scan_version,
                "projectName": project_name,
            })

    payload = {"vulnerabilities": vulns}
    payload_bytes = len(json.dumps(payload, separators=(",", ":"), ensure_ascii=False).encode("utf-8"))
    max_post_limit_raw = os.environ.get("ASFT_PAYLOAD_MAX_POST_BYTES", str(DEFAULT_PAYLOAD_MAX_POST_BYTES))
    try:
        max_post_limit = int(max_post_limit_raw)
    except ValueError:
        max_post_limit = DEFAULT_PAYLOAD_MAX_POST_BYTES
        log(
            f"[WARN] Invalid ASFT_PAYLOAD_MAX_POST_BYTES='{max_post_limit_raw}'. "
            f"Using default={DEFAULT_PAYLOAD_MAX_POST_BYTES}."
        )

    log(f"[DEBUG] Vulnerability payload bytes: {payload_bytes}")
    log(f"[DEBUG] Payload post limit bytes: {max_post_limit}")
    log(f"Sending {len(vulns)} vulnerabilities to ASFT server...")
    if max_post_limit > 0 and payload_bytes > max_post_limit:
        log(
            "[ERROR] Skipping POST to ASFT server: "
            f"payload size {payload_bytes} bytes exceeds limit {max_post_limit} bytes."
        )
        return

    resp = post_results(upload_base_url, token, payload)
    log(f"Server response: HTTP {resp.status_code}")

    if 200 <= resp.status_code < 300:
        log("🎉 Data saved successfully")
        for p in [semgrep_out, script_dir / ODC_OUTPUT_FILENAME]:
            if p.exists():
                p.unlink(missing_ok=True)
    else:
        print(f"❌ API return HTTP error {resp.status_code}:\n{resp.text}", file=sys.stderr)
        sys.exit(5)


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        sys.exit(1)
