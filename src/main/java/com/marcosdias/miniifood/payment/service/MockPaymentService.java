package com.marcosdias.miniifood.payment.service;

import com.marcosdias.miniifood.payment.web.dto.MockPaymentRequest;
import com.marcosdias.miniifood.payment.web.dto.MockPaymentResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MockPaymentService {

    public MockPaymentResponse process(MockPaymentRequest request) {
        boolean approved = request.approved() == null || request.approved();
        String paymentStatus = approved ? "APPROVED" : "DECLINED";
        String message = approved ? "Mock payment approved" : "Mock payment declined";

        log.info("Processing mock payment for order {} with result {}", request.orderId(), paymentStatus);

        return new MockPaymentResponse(
                request.orderId(),
                paymentStatus,
                message,
                "txn-" + UUID.randomUUID()
        );
    }
}

