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
public class AiRecommendationRequest {

    /**
     * Category to recommend: ALL, PLACES, HOTELS, RESTAURANTS, FOODS, TOURS
     */
    @Builder.Default
    private String category = "ALL";

    /**
     * User's preference or question (e.g. "quiet boutique hotel with pool", "romantic dinner riverside")
     */
    @NotBlank(message = "Preference query cannot be blank")
    private String preference;

    /**
     * Optional target location/province (e.g. "Siem Reap", "Phnom Penh")
     */
    private String destination;
}
