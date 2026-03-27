package com.marcosdias.miniifood.order.web;

import com.marcosdias.miniifood.order.domain.OrderStatus;
import com.marcosdias.miniifood.order.service.CreateOrderItemServiceRequest;
import com.marcosdias.miniifood.order.service.OrderService;
import com.marcosdias.miniifood.order.web.dto.CreateOrderItemRequest;
import com.marcosdias.miniifood.order.web.dto.CreateOrderRequest;
import com.marcosdias.miniifood.order.web.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Create a new order with items for the authenticated user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        log.info("Creating order for user: {}", authentication.getName());
        
        Long userId = Long.parseLong(authentication.getName());
        var serviceItems = request.items().stream()
                .map(item -> new CreateOrderItemServiceRequest(item.productId(), item.quantity()))
                .toList();
        var order = orderService.createOrder(userId, serviceItems);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by its ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        log.info("Fetching order: {}", orderId);
        var order = orderService.findById(orderId);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/user/my-orders")
    @Operation(summary = "Get authenticated user's orders", description = "Retrieve all orders for the authenticated user with pagination")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        log.info("Fetching orders for user: {}", authentication.getName());
        
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        
        Page<OrderResponse> orders = orderService.findByUserId(userId, pageable)
                .map(OrderResponse::from);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    @Operation(summary = "Get orders by status", description = "Retrieve orders filtered by status with pagination")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @Parameter(description = "Order status filter")
            @RequestParam OrderStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching orders with status: {}", status);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.findByStatus(status, pageable)
                .map(OrderResponse::from);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update the status of an order (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Parameter(description = "New order status")
            @RequestParam OrderStatus status) {
        log.info("Updating order: {} status to: {}", orderId, status);
        
        var order = orderService.updateStatus(orderId, status);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel order", description = "Cancel an order (only if pending or confirmed)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        log.info("Cancelling order: {}", orderId);
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}

