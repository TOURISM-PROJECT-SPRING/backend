package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourBookingResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long tourPackageId;
    private String tourPackageName;
    private Integer numPeople;
    private LocalDate tourDate;
    private BigDecimal totalPrice;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
