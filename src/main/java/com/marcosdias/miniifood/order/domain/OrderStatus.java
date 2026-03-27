package com.marcosdias.miniifood.order.domain;

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
}

