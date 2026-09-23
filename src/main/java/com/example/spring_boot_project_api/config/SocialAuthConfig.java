package com.example.spring_boot_project_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class SocialAuthConfig {

    // =========================
    // Google
    // =========================

    @Value("${social.google.token-info-url:https://oauth2.googleapis.com/tokeninfo}")
    private String googleTokenInfoUrl;

    @Value("${social.google.audience:}")
    private String googleAudience;


    // =========================
    // Facebook
    // =========================

    @Value("${social.facebook.me-url:https://graph.facebook.com/v19.0/me}")
    private String facebookMeUrl;

    @Value("${social.facebook.app-id:}")
    private String facebookAppId;

    @Value("${social.facebook.app-secret:}")
    private String facebookAppSecret;
}