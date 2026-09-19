package com.example.spring_boot_project_api.dto.response;

import java.time.LocalDateTime;
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
public class RoleResponse {

    private Long id;
    private String name;
    private String label;
    private String description;
    private String color;
    private List<String> permissions = new ArrayList<>();
    private Long userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}