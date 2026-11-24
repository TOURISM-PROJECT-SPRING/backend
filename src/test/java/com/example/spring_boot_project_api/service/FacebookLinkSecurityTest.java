package com.example.spring_boot_project_api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.example.spring_boot_project_api.config.SecurityConfig;
import com.example.spring_boot_project_api.controller.AuthController;
import com.example.spring_boot_project_api.dto.response.ConnectedAccountsResponse;
import com.example.spring_boot_project_api.exception.GlobalExceptionHandler;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.security.AppUserDetails;
import com.example.spring_boot_project_api.security.AppUserDetailsService;
import com.example.spring_boot_project_api.security.JwtService;
import com.example.spring_boot_project_api.security.RestAccessDeniedHandler;
import com.example.spring_boot_project_api.security.RestAuthenticationEntryPoint;
import com.example.spring_boot_project_api.security.TokenBlacklistService;

import tools.jackson.databind.ObjectMapper;

@SpringJUnitWebConfig(FacebookLinkSecurityTest.MvcConfig.class)
@TestPropertySource(properties = {
        "jwt.secret=test-only-signing-key-with-at-least-32-bytes",
        "jwt.expiration-ms=60000"
})
class FacebookLinkSecurityTest {

    private static final String LINK = "/api/auth/social/facebook/link";

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, AuthController.class, JwtService.class, GlobalExceptionHandler.class,
            RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
    static class MvcConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired private WebApplicationContext context;
    @Autowired private JwtService jwtService;
    @MockitoBean private AuthService authService;
    @MockitoBean private AppUserDetailsService userDetailsService;
    @MockitoBean private TokenBlacklistService blacklist;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void missingAndInvalidApplicationTokensCannotReachLinking() throws Exception {
        mvc.perform(post(LINK).contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"meta-token\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(LINK).header("Authorization", "Bearer bogus.jwt.signature")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"meta-token\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/social/connections")).andExpect(status().isUnauthorized());
        verifyNoInteractions(authService);
    }

    @Test
    void validApplicationTokenCanReachLinking() throws Exception {
        var owner = new Users();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setFullname("Owner");
        when(userDetailsService.loadUserByUsername("owner"))
                .thenReturn(new AppUserDetails(1L, "owner", "password", List.of()));
        when(authService.linkFacebook(any())).thenReturn(new ConnectedAccountsResponse(List.of("facebook"), null));

        mvc.perform(post(LINK).header("Authorization", "Bearer " + jwtService.generateRecentlyAuthenticatedToken(owner))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"meta-token\"}"))
                .andExpect(status().isOk());
        verify(authService).linkFacebook(any());
    }
}
