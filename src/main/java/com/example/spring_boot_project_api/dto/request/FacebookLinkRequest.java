package com.example.spring_boot_project_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FacebookLinkRequest(@NotBlank @Size(max = 8192) String token) {

    @Override
    public String toString() {
        return "FacebookLinkRequest[token=REDACTED]";
    }
}
