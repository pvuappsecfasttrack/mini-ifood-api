package com.marcosdias.miniifood.payment.web.dto;

import com.marcosdias.miniifood.order.domain.OrderStatus;

public record MockPaymentResponse(
        Long orderId,
        OrderStatus orderStatus,
        String paymentStatus,
        String message,
        String transactionId
) {
}

