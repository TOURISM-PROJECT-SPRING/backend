package com.example.spring_boot_project_api.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
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
public class HotelRoomRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotNull(message = "Room type ID is required")
    private Long roomTypeId;

    @NotNull(message = "Total room is required")
    @Positive(message = "Total room must be greater than 0")
    private Integer totalRoom;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than 0")
    private Integer capacity;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.0", message = "Price per night must be at least 0")
    private BigDecimal pricePerNight;
}