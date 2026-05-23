# AppSecFasttrack Client Onboarding Configuration Guide

Use this guide when reusing this repository for a **new client**.  
No workflow logic changes are required; only configuration values should be updated.

## 1) Update scanner config file

File: `security/scanner/asft_config.properties`

Set these values:

1. `asftUrl`
   - Purpose: AppSecFasttrack SaaS controller URL used by the scanner.
   - Example: `https://client-a.appsecfasttrack.com/`
2. `asftUploadBaseUrl`
   - Purpose: Base upload endpoint (same host as `asftUrl`, without trailing slash path).
   - Example: `https://client-a.appsecfasttrack.com`
3. `applicationName`
   - Purpose: Project/application label shown on the AppSecFasttrack side.
   - Example: `Client A - Orders API`

> Keep `asftAuthKey` as `${ASFT_AUTH_KEY}`.  
> The workflow injects the real secret from GitHub Actions at runtime.

## 2) Configure required GitHub secret

Repository Settings → Secrets and variables → Actions → **Secrets**

- `ASFT_AUTH_KEY` (required)
  - Purpose: Auth key issued for the client tenant.

## 3) Configure optional GitHub secrets (DAST authenticated scanning)

Repository Settings → Secrets and variables → Actions → **Secrets**

- `ZAP_SCANNER_EMAIL` (optional)
- `ZAP_SCANNER_PASSWORD` (optional)

Use these only if your target API requires authenticated flows in DAST.

## 4) Configure optional GitHub variables

Repository Settings → Secrets and variables → Actions → **Variables**

- `ASFT_SCANNER_TIMEOUT_MIN` (default `120`)
- `ASFT_APP_HEALTHCHECK_URL` (**required**; no default)
- `ASFT_ENFORCE_LINT` (default `false`)
- `ASFT_PAYLOAD_MAX_POST_BYTES` (default `950000`)
- `ZAP_SCANNER_NAME` (default `ZAP Scanner`)

## 5) (Optional) Update DAST target scope

File: `security/scanner/automation.yaml`

Update when the client API surface differs:

- `env.contexts[].name`
- `env.contexts[].urls`
- `env.contexts[].includePaths`
- `env.contexts[].excludePaths`
- `jobs[type=openapi].parameters.apiUrl`
- `jobs[type=openapi].parameters.targetUrl`

## 6) Verify pipeline in a PR

1. Create a test branch and open a pull request.
2. Confirm workflow `AppSecFasttrack SSLC (Semgrep + ODC)` starts.
3. Validate logs show:
   - auth key injection length check passed
   - scanner execution completed
   - expected reports produced/artifact upload completed

## Export set for Professional Services (PS)

Share these assets with PS for client onboarding:

- `.github/workflows/appsecfasttrack-sslc.yml`
- `security/scanner/` (entire folder and contents)
