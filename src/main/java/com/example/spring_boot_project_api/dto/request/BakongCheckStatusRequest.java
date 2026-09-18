package com.example.spring_boot_project_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BakongCheckStatusRequest {

    @NotBlank(message = "MD5 hash is required")
    private String md5;

    private Long bookingId;

    private String bookingType;
}
