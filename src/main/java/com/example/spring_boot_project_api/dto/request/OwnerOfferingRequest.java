package com.example.spring_boot_project_api.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerOfferingRequest {

    @NotBlank(message = "Offering name is required")
    private String name;

    // "TOUR", "ROOM", "FOOD"
    @NotBlank(message = "Offering type is required")
    private String offeringType;

    private String category;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    private String description;

    private String imageUrl;

    private Boolean isAvailable;

    private Long parentId; // e.g. hotelId, restaurantId, or tourPlaceId
}
