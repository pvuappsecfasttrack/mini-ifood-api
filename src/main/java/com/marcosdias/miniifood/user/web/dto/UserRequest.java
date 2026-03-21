package com.marcosdias.miniifood.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Email @Size(max = 180) String email,
    @NotBlank @Size(min = 6, max = 255) String password
) {
}

