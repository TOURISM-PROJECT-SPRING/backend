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
    // Owner of the record. The owner console needs this to tell its own
    // restaurants apart from everyone else's; null means unowned/seed data.
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
