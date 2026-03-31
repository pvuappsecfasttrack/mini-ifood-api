package com.marcosdias.miniifood.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcosdias.miniifood.order.domain.Order;
import com.marcosdias.miniifood.order.domain.OrderStatus;
import com.marcosdias.miniifood.order.service.OrderService;
import com.marcosdias.miniifood.payment.web.dto.MockPaymentRequest;
import com.marcosdias.miniifood.payment.web.dto.MockPaymentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("MockPaymentService Tests")
@ExtendWith(MockitoExtension.class)
class MockPaymentServiceTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private MockPaymentService mockPaymentService;

    @Test
    @DisplayName("Should approve pending order and update status to confirmed")
    void shouldApprovePendingOrder() {
        Order pendingOrder = new Order();
        pendingOrder.setId(10L);
        pendingOrder.setStatus(OrderStatus.PENDING);

        Order confirmedOrder = new Order();
        confirmedOrder.setId(10L);
        confirmedOrder.setStatus(OrderStatus.CONFIRMED);

        when(orderService.findById(10L)).thenReturn(pendingOrder);
        when(orderService.updateStatus(10L, OrderStatus.CONFIRMED)).thenReturn(confirmedOrder);

        MockPaymentRequest request = new MockPaymentRequest(10L, null, "PIX");

        MockPaymentResponse response = mockPaymentService.process(request);

        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.paymentStatus()).isEqualTo("APPROVED");
        assertThat(response.transactionId()).startsWith("txn-");
        verify(orderService).updateStatus(10L, OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should decline payment when approved is false")
    void shouldDeclineWhenApprovedIsFalse() {
        Order order = new Order();
        order.setId(11L);
        order.setStatus(OrderStatus.PENDING);

        when(orderService.findById(11L)).thenReturn(order);

        MockPaymentRequest request = new MockPaymentRequest(11L, false, "CREDIT_CARD");

        MockPaymentResponse response = mockPaymentService.process(request);

        assertThat(response.orderId()).isEqualTo(11L);
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.paymentStatus()).isEqualTo("DECLINED");
        verify(orderService, never()).updateStatus(anyLong(), eq(OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("Should throw exception when approving non payable order")
    void shouldThrowWhenApprovingNonPayableOrder() {
        Order deliveredOrder = new Order();
        deliveredOrder.setId(12L);
        deliveredOrder.setStatus(OrderStatus.DELIVERED);

        when(orderService.findById(12L)).thenReturn(deliveredOrder);

        MockPaymentRequest request = new MockPaymentRequest(12L, true, "PIX");

        assertThatThrownBy(() -> mockPaymentService.process(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Payment cannot be approved");
    }
}

