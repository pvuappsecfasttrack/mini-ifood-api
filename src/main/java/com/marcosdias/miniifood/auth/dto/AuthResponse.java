package com.marcosdias.miniifood.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
    @Schema(description = "JWT access token for API authentication")
    String accessToken,

    @Schema(description = "Token type", example = "Bearer")
    String tokenType
) {
}

