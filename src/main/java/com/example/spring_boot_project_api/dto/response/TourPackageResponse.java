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
public class TourPackageResponse {

    private Long id;
    private String name;
    private String description;
    private Integer durationDays;
    private Integer maxPeople;
    private BigDecimal price;
    private Long locationId;
    private String locationName;
    private Long tourGuideId;
    private String tourGuideName;
    private Long reviewCount;
    private Double avgRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}