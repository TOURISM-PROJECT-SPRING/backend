package com.example.spring_boot_project_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {

    // Google hands the browser an access token directly.
    private String token;

    // GitHub has no browser SDK, so the browser only gets a short-lived
    // authorization code that the backend exchanges server-side.
    private String code;
}
