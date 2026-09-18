package com.example.spring_boot_project_api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiItineraryRequest {

    @NotBlank(message = "Destination is required (e.g. Siem Reap, Phnom Penh, Kampot)")
    private String destination;

    @Min(value = 1, message = "Number of days must be at least 1")
    @Max(value = 14, message = "Number of days cannot exceed 14")
    @Builder.Default
    private Integer numberOfDays = 3;

    /**
     * Budget level: BUDGET, MODERATE, LUXURY
     */
    @Builder.Default
    private String budget = "MODERATE";

    /**
     * Travel style: CULTURE_HERITAGE, NATURE_ADVENTURE, RELAXATION, FOODIE, FAMILY
     */
    @Builder.Default
    private String travelStyle = "CULTURE_HERITAGE";

    /**
     * Group type: SOLO, COUPLE, FAMILY, FRIENDS
     */
    @Builder.Default
    private String groupType = "COUPLE";

    /**
     * Any specific requests, dietary preferences, or mobility requirements
     */
    private String specialRequests;
}
