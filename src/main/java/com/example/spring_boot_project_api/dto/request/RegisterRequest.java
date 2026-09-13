package com.example.spring_boot_project_api.dto.request;

import java.time.LocalDate;

import com.example.spring_boot_project_api.enums.GenderEnum;

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
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    private String fullname;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 200, message = "Username must be between 3 and 200 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 200, message = "Email must not exceed 200 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    private GenderEnum gender;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    private LocalDate dateOfBirth;
}