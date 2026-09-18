package com.example.spring_boot_project_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerAdminRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name must not exceed 150 characters")
    private String businessName;

    @NotBlank(message = "Business license number is required")
    @Size(max = 255, message = "Business license number must not exceed 255 characters")
    private String businessLicenseNo;

    private String verificationStatus;

    private String businessType;

    private Long userId;

    private String userName;

    private String userEmail;

    private String address;

    private String phone;
}
