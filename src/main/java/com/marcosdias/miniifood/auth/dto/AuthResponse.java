package com.marcosdias.miniifood.auth.dto;

public record AuthResponse(
    String accessToken,
    String tokenType
) {
}

