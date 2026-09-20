package com.example.spring_boot_project_api.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.response.UserResponse;
import com.example.spring_boot_project_api.model.Users;

public class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(Users user) {
        if (user == null) return null;

        List<String> assignedBusinesses = Collections.emptyList();
        String ownerStatus = null;
        if (user.getBusinesssOwnerProfiles() != null && !user.getBusinesssOwnerProfiles().isEmpty()) {
            var profile = user.getBusinesssOwnerProfiles().get(0);
            if (profile.getContractedBusinessTypes() != null) {
                assignedBusinesses = profile.getContractedBusinessTypes().stream()
                        .map(String::toLowerCase)
                        .sorted()
                        .collect(Collectors.toList());
            }
            ownerStatus = profile.getStatus();
        }

        return UserResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .roles(user.getUserRoles() == null ? Collections.emptyList()
                        : user.getUserRoles().stream()
                                .map(ur -> ur.getRole() != null ? ur.getRole().getName() : null)
                                .filter(name -> name != null)
                                .collect(Collectors.toList()))
                .permissions(collectPermissions(user))
                .assignedBusinesses(assignedBusinesses)
                .ownerStatus(ownerStatus)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static List<String> collectPermissions(Users user) {
        if (user == null || user.getUserRoles() == null) {
            return Collections.emptyList();
        }
        return user.getUserRoles().stream()
                .map(ur -> ur.getRole())
                .filter(role -> role != null && role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .filter(permission -> permission != null && !permission.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<UserResponse> toResponseList(List<Users> users) {
        if (users == null) return Collections.emptyList();
        return users.stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }
}