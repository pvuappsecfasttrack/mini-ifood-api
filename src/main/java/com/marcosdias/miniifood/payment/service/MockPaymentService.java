package com.marcosdias.miniifood.payment.service;

import com.marcosdias.miniifood.order.domain.Order;
import com.marcosdias.miniifood.order.domain.OrderStatus;
import com.marcosdias.miniifood.order.service.OrderService;
import com.marcosdias.miniifood.payment.web.dto.MockPaymentRequest;
import com.marcosdias.miniifood.payment.web.dto.MockPaymentResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MockPaymentService {

    private final OrderService orderService;

    public MockPaymentResponse process(MockPaymentRequest request) {
        boolean approved = request.approved() == null || request.approved();
        Order order = orderService.findById(request.orderId());
        OrderStatus resultingStatus = order.getStatus();

        if (approved) {
            if (order.getStatus() == OrderStatus.PENDING) {
                resultingStatus = orderService.updateStatus(order.getId(), OrderStatus.CONFIRMED).getStatus();
            } else if (order.getStatus() != OrderStatus.CONFIRMED) {
                throw new IllegalStateException(
                        "Payment cannot be approved for order with status: " + order.getStatus()
                );
            }
        }

        String paymentStatus = approved ? "APPROVED" : "DECLINED";
        String message = approved ? "Mock payment approved" : "Mock payment declined";

        log.info(
                "Processing mock payment for order {} with result {} and order status {}",
                request.orderId(),
                paymentStatus,
                resultingStatus
        );

        return new MockPaymentResponse(
                request.orderId(),
                resultingStatus,
                paymentStatus,
                message,
                "txn-" + UUID.randomUUID()
        );
    }
}

