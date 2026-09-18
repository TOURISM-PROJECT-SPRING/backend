package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerOfferingDTO {

    private Long id;
    private String name;
    private String offeringType; // "TOUR", "ROOM", "FOOD"
    private String category;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private Boolean isAvailable;
    private Long referenceId;
    private String status;
}
