package com.example.spring_boot_project_api.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerPermissionsDTO {
    private Long ownerId;
    private String businessName;
    private String status; // "ACTIVE" or "SUSPENDED"
    private boolean active;
    private List<String> contractedBusinessTypes;
    private boolean canManageHotel;
    private boolean canManageRestaurant;
    private boolean canManageTour;
    private List<OwnerManagedBusinessDTO> managedBusinesses;
}
