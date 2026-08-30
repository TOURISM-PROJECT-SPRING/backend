package com.example.spring_boot_project_api.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodOrderRequest {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Restaurant ID is required")
    @Positive(message = "Restaurant ID must be positive")
    private Long restaurantId;

    @NotNull(message = "Pickup time is required")
    @Future(message = "Pickup time must be in the future")
    private LocalDateTime pickupTime;

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    private List<FoodOrderItemRequest> items;
}
