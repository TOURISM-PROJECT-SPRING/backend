package com.example.spring_boot_project_api.dto.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name must not exceed 100 characters")
    private String name;

    @Size(max = 150, message = "Role label must not exceed 150 characters")
    private String label;

    @Size(max = 500, message = "Role description must not exceed 500 characters")
    private String description;

    @Size(max = 30, message = "Role color must not exceed 30 characters")
    private String color;

    private List<String> permissions = new ArrayList<>();
}