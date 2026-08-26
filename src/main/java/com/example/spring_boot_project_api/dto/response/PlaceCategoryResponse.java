package com.example.spring_boot_project_api.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceCategoryResponse {

    private Long id;
    private String name;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
