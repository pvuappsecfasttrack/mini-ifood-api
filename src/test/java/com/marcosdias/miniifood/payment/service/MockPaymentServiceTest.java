package com.marcosdias.miniifood.payment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.marcosdias.miniifood.payment.web.dto.MockPaymentRequest;
import com.marcosdias.miniifood.payment.web.dto.MockPaymentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MockPaymentService Tests")
class MockPaymentServiceTest {

    private final MockPaymentService mockPaymentService = new MockPaymentService();

    @Test
    @DisplayName("Should approve payment by default")
    void shouldApproveByDefault() {
        MockPaymentRequest request = new MockPaymentRequest(10L, null, "PIX");

        MockPaymentResponse response = mockPaymentService.process(request);

        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(response.paymentStatus()).isEqualTo("APPROVED");
        assertThat(response.transactionId()).startsWith("txn-");
    }

    @Test
    @DisplayName("Should decline payment when approved is false")
    void shouldDeclineWhenApprovedIsFalse() {
        MockPaymentRequest request = new MockPaymentRequest(11L, false, "CREDIT_CARD");

        MockPaymentResponse response = mockPaymentService.process(request);

        assertThat(response.orderId()).isEqualTo(11L);
        assertThat(response.paymentStatus()).isEqualTo("DECLINED");
    }
}

