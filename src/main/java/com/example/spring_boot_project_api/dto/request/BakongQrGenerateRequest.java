package com.example.spring_boot_project_api.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BakongQrGenerateRequest {

    private Long bookingId;

    /**
     * Type of booking: "TICKET", "ROOM", "FOOD_ORDER", or "GENERAL"
     */
    private String bookingType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    /**
     * "USD" (default) or "KHR"
     */
    @Builder.Default
    private String currency = "USD";

    private String description;

    private String customerPhone;
}
