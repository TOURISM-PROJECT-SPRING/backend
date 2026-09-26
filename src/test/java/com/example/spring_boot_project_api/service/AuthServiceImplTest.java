package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sun.net.httpserver.HttpServer;

import com.example.spring_boot_project_api.config.SocialAuthConfig;
import com.example.spring_boot_project_api.dto.request.ChangePasswordRequest;
import com.example.spring_boot_project_api.dto.request.ForgotPasswordRequest;
import com.example.spring_boot_project_api.dto.request.LoginRequest;
import com.example.spring_boot_project_api.dto.request.RegisterRequest;
import com.example.spring_boot_project_api.dto.request.ResetPasswordRequest;
import com.example.spring_boot_project_api.dto.request.SocialLoginRequest;
import com.example.spring_boot_project_api.dto.response.AuthResponse;
import com.example.spring_boot_project_api.dto.response.ForgotPasswordResponse;
import com.example.spring_boot_project_api.enums.GenderEnum;
import com.example.spring_boot_project_api.exception.UnauthorizedException;
import com.example.spring_boot_project_api.model.PasswordResetToken;
import com.example.spring_boot_project_api.model.Roles;
import com.example.spring_boot_project_api.model.SocialAccount;
import com.example.spring_boot_project_api.model.UserRoles;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.PasswordResetTokenRepository;
import com.example.spring_boot_project_api.repository.RoleRepository;
import com.example.spring_boot_project_api.repository.SocialAccountRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.repository.UserRoleRepository;
import com.example.spring_boot_project_api.security.AppUserDetails;
import com.example.spring_boot_project_api.security.JwtService;
import com.example.spring_boot_project_api.security.TokenBlacklistService;
import com.example.spring_boot_project_api.service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private SocialAuthConfig socialAuthConfig;
    @Mock
    private SocialAccountRepository socialAccountRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() throws Exception {
        Field expiryField = AuthServiceImpl.class.getDeclaredField("resetTokenExpirationMinutes");
        expiryField.setAccessible(true);
        expiryField.setLong(authService, 30L);
    }

    private Roles role(String name) {
        Roles role = new Roles();
        role.setId(1L);
        role.setName(name);
        return role;
    }

    private Users user(Long id, String username, String password) {
        Users user = new Users();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@smart-tourism.com");
        user.setPassword(password);
        user.setGender(GenderEnum.Male);
        return user;
    }

    private RegisterRequest registerRequest() {
        return RegisterRequest.builder()
                .fullname("New User")
                .username("newuser")
                .email("newuser@smart-tourism.com")
                .password("password123")
                .gender(GenderEnum.Male)
                .build();
    }

    private void mockToken() {
        when(jwtService.generateToken(any(Users.class))).thenReturn("token-abc");
        when(jwtService.extractExpiration("token-abc")).thenReturn(new Date(System.currentTimeMillis() + 60_000));
    }

    private void stubGithubConfig(HttpServer server) {
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        when(socialAuthConfig.getGithubTokenUrl()).thenReturn(base + "/login/oauth/access_token");
        when(socialAuthConfig.getGithubApiUrl()).thenReturn(base);
        when(socialAuthConfig.getGithubClientId()).thenReturn("Iv1.client");
        when(socialAuthConfig.getGithubClientSecret()).thenReturn("s3cret");
    }

    /**
     * Stands in for github.com/login/oauth/access_token, /user and /user/emails.
     * The profile deliberately omits a public email so /user/emails is exercised.
     */
    private HttpServer startGithubServer(String tokenResponse, String profileResponse)
            throws Exception {
        return startGithubServer(tokenResponse, profileResponse,
                "[{\"email\":\"unverified@github.com\",\"primary\":true,\"verified\":false},"
                        + "{\"email\":\"octocat@github.com\",\"primary\":true,\"verified\":true}]");
    }

    private HttpServer startGithubServer(String tokenResponse, String profileResponse,
                                         String emailsResponse) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/login/oauth/access_token", exchange -> {
            respond(exchange, 200, tokenResponse);
        });
        server.createContext("/user", exchange -> {
            respond(exchange, 200, profileResponse);
        });
        server.createContext("/user/emails", exchange -> {
            respond(exchange, 200, emailsResponse);
        });
        server.start();
        return server;
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body)
            throws java.io.IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    @Test
    void socialLogin_exchangesGithubCodeAndUsesGithubHandleAsUsername() throws Exception {
        HttpServer server = startGithubServer(
                "{\"access_token\":\"gho_token\",\"token_type\":\"bearer\"}",
                "{\"id\":4242,\"login\":\"octocat\",\"name\":\"The Octocat\"}");
        try {
            stubGithubConfig(server);
            when(socialAccountRepository.findByProviderAndProviderUserId("github", "4242"))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("TOURIST")).thenReturn(Optional.of(role("TOURIST")));
            when(userRepository.existsByUsername("octocat")).thenReturn(false);
            when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
                Users saved = invocation.getArgument(0);
                saved.setId(91L);
                return saved;
            });
            when(userRoleRepository.save(any(UserRoles.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(socialAccountRepository.save(any(SocialAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            mockToken();

            AuthResponse response = authService.loginWithSocial(
                    "GitHub", SocialLoginRequest.builder().code("auth-code").build());

            assertThat(response.getUser().getId()).isEqualTo(91L);
            assertThat(response.getUser().getUsername()).isEqualTo("octocat");
            assertThat(response.getUser().getFullname()).isEqualTo("The Octocat");
            // /user exposes no public email, so /user/emails must supply it.
            assertThat(response.getUser().getEmail()).isEqualTo("octocat@github.com");
            assertThat(response.getUser().getRoles()).containsExactly("TOURIST");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void socialLogin_githubWithoutVerifiedEmailFallsBackToNullEmail() throws Exception {
        HttpServer server = startGithubServer(
                "{\"access_token\":\"gho_token\"}",
                "{\"id\":77,\"login\":\"ghost\",\"name\":null}",
                "[{\"email\":\"unverified@github.com\",\"primary\":true,\"verified\":false}]");
        try {
            stubGithubConfig(server);
            when(socialAccountRepository.findByProviderAndProviderUserId("github", "77"))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("TOURIST")).thenReturn(Optional.of(role("TOURIST")));
            when(userRepository.existsByUsername("ghost")).thenReturn(false);
            when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
                Users saved = invocation.getArgument(0);
                saved.setId(92L);
                return saved;
            });
            when(userRoleRepository.save(any(UserRoles.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(socialAccountRepository.save(any(SocialAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            mockToken();

            AuthResponse response = authService.loginWithSocial(
                    "github", SocialLoginRequest.builder().code("auth-code").build());

            assertThat(response.getUser().getUsername()).isEqualTo("ghost");
            // No `name` on the GitHub profile, so the handle doubles as the name.
            assertThat(response.getUser().getFullname()).isEqualTo("ghost");
            assertThat(response.getUser().getEmail()).isNull();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void socialLogin_usesLinkedProviderIdentityBeforeEmail() throws Exception {
        HttpServer server = startGithubServer(
                "{\"access_token\":\"gho_token\"}",
                "{\"id\":77,\"login\":\"ghost\",\"name\":\"Ghost\"}");
        try {
            Users existing = user(77L, "ghost", "encoded");
            SocialAccount linked = new SocialAccount();
            linked.setProvider("github");
            linked.setProviderUserId("77");
            linked.setUser(existing);
            stubGithubConfig(server);
            when(socialAccountRepository.findByProviderAndProviderUserId("github", "77"))
                    .thenReturn(Optional.of(linked));
            mockToken();

            AuthResponse response = authService.loginWithSocial(
                    "github", SocialLoginRequest.builder().code("auth-code").build());

            // The provider identity wins even though /user/emails now returns an
            // address, so changing it on GitHub cannot hijack another account.
            assertThat(response.getUser().getId()).isEqualTo(77L);
            verify(socialAccountRepository, never()).save(any(SocialAccount.class));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void socialLogin_githubRequiresCode() {
        assertThatThrownBy(() -> authService.loginWithSocial(
                "github", SocialLoginRequest.builder().build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("token or authorization code");
    }

    @Test
    void socialLogin_githubWithoutConfiguredSecretFails() {
        when(socialAuthConfig.getGithubClientId()).thenReturn("");
        when(socialAuthConfig.getGithubClientSecret()).thenReturn("");

        assertThatThrownBy(() -> authService.loginWithSocial(
                "github", SocialLoginRequest.builder().code("auth-code").build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GITHUB_CLIENT_ID");
    }

    @Test
    void register_assignsDefaultTouristRoleAndReturnsToken() {
        Roles tourist = role("TOURIST");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@smart-tourism.com")).thenReturn(false);
        when(roleRepository.findByName("TOURIST")).thenReturn(Optional.of(tourist));
        when(userRepository.save(any(Users.class))).thenAnswer(inv -> {
            Users u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        mockToken();

        AuthResponse response = authService.register(registerRequest());

        assertThat(response.getAccessToken()).isEqualTo("token-abc");
        assertThat(response.getUser().getId()).isEqualTo(99L);
        assertThat(response.getUser().getRoles()).contains("TOURIST");
        verify(userRoleRepository).save(any(UserRoles.class));
    }

    @Test
    void register_rejectsDuplicateUsername() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@smart-tourism.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsWhenTouristRoleNotConfigured() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@smart-tourism.com")).thenReturn(false);
        when(roleRepository.findByName("TOURIST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TOURIST");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        Users user = user(1L, "admin", "encoded");
        AppUserDetails principal = new AppUserDetails(
                1L, "admin", "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "encoded");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        mockToken();

        AuthResponse response = authService.login(
                LoginRequest.builder().username("admin").password("admin123").build());

        assertThat(response.getAccessToken()).isEqualTo("token-abc");
        assertThat(response.getUser().getUsername()).isEqualTo("admin");
    }

    @Test
    void login_rejectsInvalidCredentials() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(
                LoginRequest.builder().username("admin").password("wrong").build()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid username or password");

        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void forgotPassword_returnsResetToken() {
        Users user = user(1L, "user", "encoded");
        when(userRepository.findByEmail("user@smart-tourism.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ForgotPasswordResponse response = authService.forgotPassword(
                ForgotPasswordRequest.builder().email("user@smart-tourism.com").build());

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getExpiresAt()).isNotNull();
        assertThat(response.getMessage()).contains("reset token");
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void forgotPassword_throwsForUnknownEmail() {
        when(userRepository.findByEmail("nobody@smart-tourism.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword(
                ForgotPasswordRequest.builder().email("nobody@smart-tourism.com").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No account");

        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void resetPassword_updatesPasswordAndMarksTokenUsed() {
        Users user = user(1L, "user", "old-encoded");
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("reset-token");
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        resetToken.setUsed(false);
        resetToken.setUser(user);

        when(passwordResetTokenRepository.findByToken("reset-token"))
                .thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newpass123")).thenReturn("new-encoded");

        authService.resetPassword(ResetPasswordRequest.builder()
                .token("reset-token").newPassword("newpass123").build());

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("new-encoded");
        assertThat(resetToken.isUsed()).isTrue();
    }

    @Test
    void resetPassword_rejectsExpiredToken() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("expired-token");
        resetToken.setExpiresAt(LocalDateTime.now().minusMinutes(5));
        resetToken.setUsed(false);
        resetToken.setUser(user(1L, "user", "old"));

        when(passwordResetTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> authService.resetPassword(ResetPasswordRequest.builder()
                .token("expired-token").newPassword("newpass123").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired");

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_rejectsAlreadyUsedToken() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("used-token");
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        resetToken.setUsed(true);
        resetToken.setUser(user(1L, "user", "old"));

        when(passwordResetTokenRepository.findByToken("used-token"))
                .thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> authService.resetPassword(ResetPasswordRequest.builder()
                .token("used-token").newPassword("newpass123").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already been used");

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_updatesPasswordWhenCurrentMatches() {
        Users user = user(1L, "user", "old-encoded");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AppUserDetails(1L, "user", "old-encoded", List.of()),
                        "token"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpass", "old-encoded")).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("new-encoded");

        authService.changePassword(ChangePasswordRequest.builder()
                .currentPassword("oldpass").newPassword("newpass123").build());

        assertThat(user.getPassword()).isEqualTo("new-encoded");
        verify(userRepository).save(user);
        SecurityContextHolder.clearContext();
    }

    @Test
    void changePassword_rejectsWrongCurrentPassword() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AppUserDetails(1L, "user", "old-encoded", List.of()),
                        "token"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "user", "old-encoded")));
        when(passwordEncoder.matches("wrongpass", "old-encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(ChangePasswordRequest.builder()
                .currentPassword("wrongpass").newPassword("newpass123").build()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
        SecurityContextHolder.clearContext();
    }

    @Test
    void logout_blacklistsTokenUntilExpiry() {
        Date expiresSoon = new Date(System.currentTimeMillis() + 120_000);
        when(jwtService.extractExpiration("token-abc")).thenReturn(expiresSoon);

        authService.logout("Bearer token-abc");

        verify(tokenBlacklistService)
                .blacklist(eq("token-abc"), any(Duration.class));
    }
}