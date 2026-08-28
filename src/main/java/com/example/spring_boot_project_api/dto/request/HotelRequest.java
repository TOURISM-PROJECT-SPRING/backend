package com.example.spring_boot_project_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelRequest {

    @NotBlank(message = "Hotel name is required")
    @Size(max = 200, message = "Hotel name must not exceed 200 characters")
    private String hotelName;

    @Size(max = 30, message = "Phone contact must not exceed 30 characters")
    private String phoneContact;

    @Email(message = "Email contact must be valid")
    @Size(max = 200, message = "Email contact must not exceed 200 characters")
    private String emailContact;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    @NotNull(message = "Owner user ID is required")
    private Long ownerId;
}