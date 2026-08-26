package com.example.spring_boot_project_api.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistrictResponse {

    private Long id;
    private String name;
    private Long provinceId;
    private String provinceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
