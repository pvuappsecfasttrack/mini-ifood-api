package com.marcosdias.miniifood.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcosdias.miniifood.order.domain.Order;
import com.marcosdias.miniifood.order.domain.OrderStatus;
import com.marcosdias.miniifood.order.repository.OrderRepository;
import com.marcosdias.miniifood.product.repository.ProductRepository;
import com.marcosdias.miniifood.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldFindOrdersByUserAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order = Order.builder().id(1L).status(OrderStatus.PENDING).build();
        Page<Order> expected = new PageImpl<>(java.util.List.of(order));

        when(orderRepository.findByUserIdAndStatus(5L, OrderStatus.PENDING, pageable)).thenReturn(expected);

        Page<Order> result = orderService.findByUserIdAndStatus(5L, OrderStatus.PENDING, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findByUserIdAndStatus(5L, OrderStatus.PENDING, pageable);
    }

    @Test
    void shouldThrowWhenUpdatingStatusWithNullValue() {
        Order order = Order.builder().id(10L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(10L, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must not be null");

        verify(orderRepository, never()).save(order);
    }

    @Test
    void shouldThrowWhenCancellingOrderWithInvalidStatus() {
        Order order = Order.builder().id(20L).status(OrderStatus.DELIVERED).build();
        when(orderRepository.findById(20L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(20L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot cancel order");

        verify(orderRepository, never()).save(order);
    }
}

