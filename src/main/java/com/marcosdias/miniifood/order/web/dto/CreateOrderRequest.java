package com.marcosdias.miniifood.order.web.dto;

import java.util.List;

public record CreateOrderRequest(
        List<CreateOrderItemRequest> items
) {}

