package com.example.spring_boot_project_api.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiItineraryResponse {

    private String destination;

    private String title;

    private String overview;

    private Integer numberOfDays;

    private String budgetLevel;

    private String estimatedTotalBudget;

    @Builder.Default
    private List<AiDayPlanDto> days = new ArrayList<>();

    @Builder.Default
    private List<String> practicalTips = new ArrayList<>();

    @Builder.Default
    private List<ReferencedItemDto> recommendedDatabaseItems = new ArrayList<>();

    private String fullMarkdownItinerary;

    private String modelUsed;

    private boolean fallbackUsed;
}
