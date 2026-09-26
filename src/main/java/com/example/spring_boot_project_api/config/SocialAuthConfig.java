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
    // GitHub
    // =========================

    // GitHub has no browser SDK: the web flow is an OAuth code redirect, so the
    // backend exchanges the code itself. The client secret must never leave here.
    @Value("${social.github.api-url:https://api.github.com}")
    private String githubApiUrl;

    @Value("${social.github.token-url:https://github.com/login/oauth/access_token}")
    private String githubTokenUrl;

    @Value("${social.github.client-id:}")
    private String githubClientId;

    @Value("${social.github.client-secret:}")
    private String githubClientSecret;
}