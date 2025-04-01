package com.technischools.marcel.order.model;

import java.util.Optional;

public enum OrderStatus {
    NOWE, W_TRAKCIE, ZAKONCZONE, ANULOWANE;

    public static Optional<OrderStatus> toValidStatus(String status) {
        if (status == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(OrderStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
