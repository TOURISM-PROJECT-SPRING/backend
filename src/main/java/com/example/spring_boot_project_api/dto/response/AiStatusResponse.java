package com.example.spring_boot_project_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStatusResponse {
    private boolean configured;
    private String model;
    private String fallbackModel;
    private String status; // "UP", "DEGRADED", "UNCONFIGURED"
    private String message;
    private String maskedApiKey;
}
