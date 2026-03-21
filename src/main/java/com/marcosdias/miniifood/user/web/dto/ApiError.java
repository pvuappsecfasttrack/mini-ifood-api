package com.marcosdias.miniifood.user.web.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(
    int status,
    String message,
    OffsetDateTime timestamp,
    List<String> details
) {
}

