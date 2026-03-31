package com.marcosdias.miniifood.order.domain;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING("Pendente"),
    CONFIRMED("Confirmado"),
    PREPARING("Preparando"),
    READY_FOR_PICKUP("Pronto para Retirada"),
    OUT_FOR_DELIVERY("Saiu para Entrega"),
    DELIVERED("Entregue"),
    CANCELLED("Cancelado");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Set<OrderStatus> allowedNextStatuses() {
        return switch (this) {
            case PENDING -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(PREPARING, CANCELLED);
            case PREPARING -> EnumSet.of(READY_FOR_PICKUP, OUT_FOR_DELIVERY);
            case READY_FOR_PICKUP -> EnumSet.of(DELIVERED);
            case OUT_FOR_DELIVERY -> EnumSet.of(DELIVERED);
            case DELIVERED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return this == newStatus || allowedNextStatuses().contains(newStatus);
    }
}

