package com.marcosdias.miniifood.user.web.dto;

import java.time.OffsetDateTime;

public record UserResponse(
    Long id,
    String name,
    String email,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}

