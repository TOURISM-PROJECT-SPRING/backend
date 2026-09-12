package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {

    private Long id;
    private String name;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Long hotelId;
    private String hotelName;
    private Long restaurantId;
    private String restaurantName;
    private Long tourPackageId;
    private String tourPackageName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}