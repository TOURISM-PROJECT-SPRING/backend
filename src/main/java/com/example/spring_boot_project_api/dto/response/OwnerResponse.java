package com.example.spring_boot_project_api.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerResponse {

    private Long id;
    private String businessName;
    private String businessLicenseNo;
    private String verificationStatus;
    private LocalDate verifiedAt;
    private String status; // "ACTIVE" or "SUSPENDED"
    private String businessType;
    private List<String> businessTypes;
    private List<OwnerManagedBusinessDTO> managedBusinesses;
    private Long userId;
    private String userName;
    private String userEmail;
    private String address;
    private String city;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
