package com.marcosdias.miniifood.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
    @Size(max = 120)
    @Schema(description = "User full name", example = "Marcos")
    String name,

    @NotBlank
    @Email
    @Size(max = 180)
    @Schema(description = "User email address", example = "marcos@email.com")
    String email,

    @NotBlank
    @Size(min = 6, max = 255)
    @Schema(description = "User password (min 6 chars)", example = "123456")
    String password
) {
}

