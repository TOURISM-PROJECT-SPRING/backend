package com.example.spring_boot_project_api.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.example.spring_boot_project_api.enums.GenderEnum;
import com.example.spring_boot_project_api.enums.UserEnum;

import jakarta.validation.constraints.Email;
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
public class UserAdminUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    private String fullname;

    @NotBlank(message = "Username is required")
    @Size(max = 200, message = "Username must not exceed 200 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 200, message = "Email must not exceed 200 characters")
    private String email;

    private GenderEnum gender;

    private String address;

    private LocalDate dateOfBirth;

    private UserEnum status;

    private List<String> roles;
}