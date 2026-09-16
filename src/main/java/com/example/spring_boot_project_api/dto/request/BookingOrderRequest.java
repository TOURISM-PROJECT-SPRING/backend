package com.example.spring_boot_project_api.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingOrderRequest {

    private Long userId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String customerEmail;

    private String customerPhone;

    // ROOM, TICKET, FOOD_ORDER
    @NotBlank(message = "Booking type is required")
    private String bookingType;

    // Reference ID to roomId, ticketId, tourPlaceId, or restaurantId
    private Long referenceId;

    private String serviceName;

    private Long ownerId;

    private Integer quantity;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;

    private String paymentMethod;

    private String bookingDate;

    private String notes;

    private List<String> items;
}
