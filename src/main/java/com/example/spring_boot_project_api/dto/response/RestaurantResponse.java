package com.example.spring_boot_project_api.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantResponse {

    private Long id;
    private String name;
    private String description;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Long tourismPlaceId;
    private String tourismPlaceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
