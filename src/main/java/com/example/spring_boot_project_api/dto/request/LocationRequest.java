package com.example.spring_boot_project_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequest {

    @NotBlank(message = "Province is required")
    @Size(max = 200, message = "Province must not exceed 200 characters")
    private String province;

    @NotBlank(message = "District is required")
    @Size(max = 200, message = "District must not exceed 200 characters")
    private String district;
}
