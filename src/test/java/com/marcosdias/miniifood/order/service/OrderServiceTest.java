package com.marcosdias.miniifood.order.service;

import static org.assertj.core.api.Assertions.*;

import com.marcosdias.miniifood.order.domain.Order;
import com.marcosdias.miniifood.order.domain.OrderStatus;
import com.marcosdias.miniifood.order.repository.OrderRepository;
import com.marcosdias.miniifood.product.domain.Product;
import com.marcosdias.miniifood.product.repository.ProductRepository;
import com.marcosdias.miniifood.user.domain.User;
import com.marcosdias.miniifood.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword")
                .build();
        testUser = userRepository.save(testUser);

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.00))
                .quantityAvailable(10)
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("Should create order successfully with valid items")
    void testCreateOrderSuccess() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 2)
        );

        Order order = orderService.createOrder(testUser.getId(), items);

        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        assertThat(order.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalPrice()).isEqualTo(BigDecimal.valueOf(200.00));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testCreateOrderUserNotFound() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 1)
        );

        assertThatThrownBy(() -> orderService.createOrder(999L, items))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testCreateOrderProductNotFound() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(999L, 1)
        );

        assertThatThrownBy(() -> orderService.createOrder(testUser.getId(), items))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("Should throw exception when insufficient product quantity")
    void testCreateOrderInsufficientQuantity() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 20)
        );

        assertThatThrownBy(() -> orderService.createOrder(testUser.getId(), items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient quantity");
    }

    @Test
    @DisplayName("Should find order by ID")
    void testFindOrderById() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 1)
        );
        Order createdOrder = orderService.createOrder(testUser.getId(), items);

        Order foundOrder = orderService.findById(createdOrder.getId());

        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getId()).isEqualTo(createdOrder.getId());
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void testFindOrderByIdNotFound() {
        assertThatThrownBy(() -> orderService.findById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Should find orders by user ID with pagination")
    void testFindByUserId() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 1)
        );
        orderService.createOrder(testUser.getId(), items);
        orderService.createOrder(testUser.getId(), items);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = orderService.findByUserId(testUser.getId(), pageable);

        assertThat(orders).hasSize(2);
    }

    @Test
    @DisplayName("Should find orders by status with pagination")
    void testFindByStatus() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 1)
        );
        orderService.createOrder(testUser.getId(), items);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = orderService.findByStatus(OrderStatus.PENDING, pageable);

        assertThat(orders).hasSize(1);
    }

    @Test
    @DisplayName("Should update order status successfully")
    void testUpdateOrderStatus() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 1)
        );
        Order createdOrder = orderService.createOrder(testUser.getId(), items);

        Order updatedOrder = orderService.updateStatus(createdOrder.getId(), OrderStatus.CONFIRMED);

        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should cancel order when status is PENDING")
    void testCancelOrderPending() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 1)
        );
        Order createdOrder = orderService.createOrder(testUser.getId(), items);

        orderService.cancelOrder(createdOrder.getId());

        Order cancelledOrder = orderService.findById(createdOrder.getId());
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should throw exception when cancelling order with invalid status")
    void testCancelOrderInvalidStatus() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 1)
        );
        Order createdOrder = orderService.createOrder(testUser.getId(), items);
        orderService.updateStatus(createdOrder.getId(), OrderStatus.CONFIRMED);
        orderService.updateStatus(createdOrder.getId(), OrderStatus.PREPARING);
        orderService.updateStatus(createdOrder.getId(), OrderStatus.OUT_FOR_DELIVERY);

        assertThatThrownBy(() -> orderService.cancelOrder(createdOrder.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot cancel order");
    }

    @Test
    @DisplayName("Should throw exception when order status transition is invalid")
    void testUpdateOrderStatusInvalidTransition() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 1)
        );
        Order createdOrder = orderService.createOrder(testUser.getId(), items);

        assertThatThrownBy(() -> orderService.updateStatus(createdOrder.getId(), OrderStatus.DELIVERED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("Should throw exception when trying to move from CONFIRMED directly to DELIVERED")
    void testInvalidTransitionConfirmedToDelivered() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 1)
        );
        Order createdOrder = orderService.createOrder(testUser.getId(), items);
        orderService.updateStatus(createdOrder.getId(), OrderStatus.CONFIRMED);

        assertThatThrownBy(() -> orderService.updateStatus(createdOrder.getId(), OrderStatus.DELIVERED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("Should throw exception when trying to move from CANCELLED back to PENDING")
    void testInvalidTransitionCancelledToPending() {
        List<CreateOrderItemServiceRequest> items = List.of(
                new CreateOrderItemServiceRequest(testProduct.getId(), 1)
        );
        Order createdOrder = orderService.createOrder(testUser.getId(), items);
        orderService.cancelOrder(createdOrder.getId());

        assertThatThrownBy(() -> orderService.updateStatus(createdOrder.getId(), OrderStatus.PENDING))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition");
    }
}

