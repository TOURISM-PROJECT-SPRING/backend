package com.example.spring_boot_project_api.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationResponse {

    private Long id;
    private String province;
    private String district;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
