package com.example.spring_boot_project_api.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Integer rating;
    private String comment;
    private Long userId;
    private String userName;
    private String targetType;
    private Long targetId;
    private String targetName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}