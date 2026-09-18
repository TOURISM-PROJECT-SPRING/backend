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
public class AiDayPlanDto {
    private int dayNumber;
    private String theme;
    private String morningActivity;
    private String lunchRecommendation;
    private String afternoonActivity;
    private String eveningActivity;
    private String dinnerRecommendation;

    @Builder.Default
    private List<String> highlights = new ArrayList<>();
}
