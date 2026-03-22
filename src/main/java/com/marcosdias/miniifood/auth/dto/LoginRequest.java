package com.marcosdias.miniifood.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank
    @Email
    @Schema(description = "User email address", example = "marcos@email.com")
    String email,

    @NotBlank
    @Schema(description = "User password", example = "123456")
    String password
) {
}

