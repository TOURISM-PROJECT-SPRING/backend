package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private String image;
    private Boolean isAvailable;
    private Long restaurantId;
    private String restaurantName;
    private Long foodCategoryId;
    private String foodCategoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
