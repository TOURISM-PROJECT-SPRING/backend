package com.example.spring_boot_project_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerManagedBusinessDTO {
    private Long id;
    private String name;
    private String type; // "HOTEL", "RESTAURANT", "TOUR"
    private String details;
    private String address;
    private String status;
}
