package com.example.spring_boot_project_api.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String fullname;
    private String username;
    private String email;
    private String phone;
    private String gender;
    private String address;
    private LocalDate dateOfBirth;
    private String status;
    private List<String> roles;
    private List<String> permissions;
    private List<String> assignedBusinesses;
    private String ownerStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}