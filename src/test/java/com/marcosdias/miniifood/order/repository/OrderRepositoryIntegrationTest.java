package com.marcosdias.miniifood.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.marcosdias.miniifood.order.domain.Order;
import com.marcosdias.miniifood.order.domain.OrderStatus;
import com.marcosdias.miniifood.order.service.CreateOrderItemServiceRequest;
import com.marcosdias.miniifood.order.service.OrderService;
import com.marcosdias.miniifood.product.domain.Product;
import com.marcosdias.miniifood.product.repository.ProductRepository;
import com.marcosdias.miniifood.user.domain.User;
import com.marcosdias.miniifood.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
            .name("Order User")
            .email("order-user@email.com")
            .password("encoded")
            .build());

        product = productRepository.save(Product.builder()
            .name("Order Product")
            .description("Product used in order repository test")
            .price(BigDecimal.valueOf(19.90))
            .quantityAvailable(20)
            .build());
    }

    @Test
    void shouldFindOrdersByUserIdAndStatus() {
        List<CreateOrderItemServiceRequest> items = List.of(new CreateOrderItemServiceRequest(product.getId(), 1));

        Order pendingOrder = orderService.createOrder(user.getId(), items);
        Order confirmedOrder = orderService.createOrder(user.getId(), items);
        orderService.updateStatus(confirmedOrder.getId(), OrderStatus.CONFIRMED);

        Page<Order> pendingOrders = orderRepository.findByUserIdAndStatus(
            user.getId(),
            OrderStatus.PENDING,
            PageRequest.of(0, 10)
        );

        Page<Order> confirmedOrders = orderRepository.findByUserIdAndStatus(
            user.getId(),
            OrderStatus.CONFIRMED,
            PageRequest.of(0, 10)
        );

        assertThat(pendingOrders).hasSize(1);
        assertThat(pendingOrders.getContent().get(0).getId()).isEqualTo(pendingOrder.getId());
        assertThat(confirmedOrders).hasSize(1);
        assertThat(confirmedOrders.getContent().get(0).getId()).isEqualTo(confirmedOrder.getId());
    }
}

