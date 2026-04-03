package com.marcosdias.miniifood.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void shouldRejectInvalidTransitionFromConfirmedToDelivered() {
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
    }

    @Test
    void shouldRejectAnyTransitionFromCancelled() {
        assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CONFIRMED)).isFalse();
        assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
    }

    @Test
    void shouldRejectTransitionFromDeliveredBackToPreparing() {
        assertThat(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PREPARING)).isFalse();
    }
}

