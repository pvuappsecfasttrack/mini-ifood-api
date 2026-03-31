package com.marcosdias.miniifood.payment.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MockPaymentRequest(
        @NotNull(message = "Order id is required")
        @Positive(message = "Order id must be positive")
        Long orderId,
        Boolean approved,
        String paymentMethod
) {
}

