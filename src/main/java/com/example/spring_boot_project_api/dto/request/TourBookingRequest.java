package com.example.spring_boot_project_api.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourBookingRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Tour package ID is required")
    private Long tourPackageId;

    private Integer numPeople;
    private LocalDate tourDate;
    private BigDecimal totalPrice;
}
