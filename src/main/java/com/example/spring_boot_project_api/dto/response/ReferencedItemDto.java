package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferencedItemDto {
    private Long id;
    private String type; // "TOUR_PLACE", "HOTEL", "RESTAURANT", "FOOD", "TOUR_PACKAGE", "PROMOTION"
    private String name;
    private String description;
    private String location;
    private BigDecimal price;
    private BigDecimal rating;
    private String imageUrl;
}
