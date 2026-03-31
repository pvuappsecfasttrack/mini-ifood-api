package com.marcosdias.miniifood.order.service;

import com.marcosdias.miniifood.order.domain.Order;
import com.marcosdias.miniifood.order.domain.OrderItem;
import com.marcosdias.miniifood.order.domain.OrderStatus;
import com.marcosdias.miniifood.order.repository.OrderRepository;
import com.marcosdias.miniifood.product.domain.Product;
import com.marcosdias.miniifood.product.repository.ProductRepository;
import com.marcosdias.miniifood.user.domain.User;
import com.marcosdias.miniifood.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public Order createOrder(Long userId, List<CreateOrderItemServiceRequest> itemRequests) {
        log.info("Creating order for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .build();

        for (CreateOrderItemServiceRequest itemRequest : itemRequests) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + itemRequest.productId()));

            if (product.getQuantityAvailable() < itemRequest.quantity()) {
                log.warn("Insufficient quantity for product: {} (requested: {}, available: {})",
                        product.getId(), itemRequest.quantity(), product.getQuantityAvailable());
                throw new IllegalArgumentException("Insufficient quantity for product: " + product.getName());
            }

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addItem(item);
        }

        order.calculateTotalPrice();
        Order saved = orderRepository.save(order);
        log.info("Order created successfully with id: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + orderId));
    }

    @Transactional(readOnly = true)
    public Page<Order> findByUserId(Long userId, Pageable pageable) {
        log.debug("Finding orders for user: {}", userId);
        return orderRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        log.debug("Finding orders by status: {}", status);
        return orderRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable) {
        log.debug("Finding orders for user: {} with status: {}", userId, status);
        return orderRepository.findByUserIdAndStatus(userId, status, pageable);
    }

    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order: {} status to: {}", orderId, newStatus);
        Order order = findById(orderId);

        if (newStatus == null) {
            throw new IllegalArgumentException("New status must not be null");
        }

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition from " + order.getStatus() + " to " + newStatus
            );
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public void cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = findById(orderId);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Cannot cancel order with status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}

