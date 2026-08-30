package com.example.spring_boot_project_api.enums;

public enum OrderStatusEnum {
    DRAFT,
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    CANCELLED,
    COMPLETED;

    public static boolean isValid(String status) {
        if (status == null) return false;
        for (OrderStatusEnum value : values()) {
            if (value.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }
}
