package com.marcosdias.miniifood.payment.web.dto;

public record MockPaymentResponse(
        Long orderId,
        String paymentStatus,
        String message,
        String transactionId
) {
}

