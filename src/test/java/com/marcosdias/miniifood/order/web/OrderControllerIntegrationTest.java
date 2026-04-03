package com.marcosdias.miniifood.order.web;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marcosdias.miniifood.order.domain.Order;
import com.marcosdias.miniifood.order.domain.OrderItem;
import com.marcosdias.miniifood.order.domain.OrderStatus;
import com.marcosdias.miniifood.order.service.OrderService;
import com.marcosdias.miniifood.product.domain.Product;
import com.marcosdias.miniifood.user.domain.User;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldReturnForbiddenWhenCreatingOrderWithoutAuthentication() throws Exception {
        String body = """
            {
              "items": [
                { "productId": 1, "quantity": 2 }
              ]
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void shouldCreateOrderWhenAuthenticatedUser() throws Exception {
        Order order = sampleOrder(10L, 1L, OrderStatus.PENDING);
        when(orderService.createOrder(eq(1L), anyList())).thenReturn(order);

        String body = """
            {
              "items": [
                { "productId": 1, "quantity": 2 }
              ]
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "2", roles = {"USER"})
    void shouldReturnForbiddenForUserOnAdminOrderListEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("status", "PENDING"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "99", roles = {"ADMIN"})
    void shouldAllowAdminToListOrdersByStatus() throws Exception {
        Order order = sampleOrder(11L, 1L, OrderStatus.CONFIRMED);
        when(orderService.findByStatus(eq(OrderStatus.CONFIRMED), eq(PageRequest.of(0, 10))))
            .thenReturn(new PageImpl<>(List.of(order)));

        mockMvc.perform(get("/api/v1/orders").param("status", "CONFIRMED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(11))
            .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "3", roles = {"USER"})
    void shouldReturnForbiddenForUserOnAdminStatusUpdateEndpoint() throws Exception {
        mockMvc.perform(put("/api/v1/orders/11/status").param("status", "CONFIRMED"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "99", roles = {"ADMIN"})
    void shouldAllowAdminToUpdateOrderStatus() throws Exception {
        Order order = sampleOrder(11L, 1L, OrderStatus.CONFIRMED);
        when(orderService.updateStatus(11L, OrderStatus.CONFIRMED)).thenReturn(order);

        mockMvc.perform(put("/api/v1/orders/11/status").param("status", "CONFIRMED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(11))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "99", roles = {"ADMIN"})
    void shouldReturnBadRequestWhenStatusValueIsInvalid() throws Exception {
        mockMvc.perform(put("/api/v1/orders/11/status").param("status", "INVALID"))
            .andExpect(status().isBadRequest());
    }

    private Order sampleOrder(Long orderId, Long userId, OrderStatus status) {
        User user = User.builder().id(userId).name("Test User").email("user@email.com").password("x").build();
        Product product = Product.builder().id(1L).name("Burger").description("x").price(BigDecimal.valueOf(20.00)).quantityAvailable(10).build();

        OrderItem item = OrderItem.builder()
            .id(1L)
            .product(product)
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(20.00))
            .build();

        Order order = Order.builder()
            .id(orderId)
            .user(user)
            .status(status)
            .totalPrice(BigDecimal.valueOf(40.00))
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

        order.addItem(item);
        return order;
    }
}

