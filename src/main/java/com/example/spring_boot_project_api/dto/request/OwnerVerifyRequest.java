package com.example.spring_boot_project_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerVerifyRequest {

    @NotBlank(message = "Verification status is required")
    private String status;
}
