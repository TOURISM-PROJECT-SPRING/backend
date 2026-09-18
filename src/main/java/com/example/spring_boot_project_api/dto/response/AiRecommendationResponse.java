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
public class AiRecommendationResponse {

    private String category;

    private String userPreference;

    private String destination;

    private String aiAnalysis;

    @Builder.Default
    private List<ReferencedItemDto> recommendations = new ArrayList<>();

    private String modelUsed;

    private boolean fallbackUsed;
}
