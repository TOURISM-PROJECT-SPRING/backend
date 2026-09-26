package com.example.spring_boot_project_api.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.spring_boot_project_api.config.SocialAuthConfig;
import com.example.spring_boot_project_api.dto.request.ChangePasswordRequest;
import com.example.spring_boot_project_api.dto.request.ForgotPasswordRequest;
import com.example.spring_boot_project_api.dto.request.LoginRequest;
import com.example.spring_boot_project_api.dto.request.ProfileUpdateRequest;
import com.example.spring_boot_project_api.dto.request.RegisterRequest;
import com.example.spring_boot_project_api.dto.request.ResetPasswordRequest;
import com.example.spring_boot_project_api.dto.request.SocialLoginRequest;
import com.example.spring_boot_project_api.dto.response.AuthResponse;
import com.example.spring_boot_project_api.dto.response.ForgotPasswordResponse;
import com.example.spring_boot_project_api.dto.response.MessageResponse;
import com.example.spring_boot_project_api.enums.GenderEnum;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.exception.UnauthorizedException;
import com.example.spring_boot_project_api.mapper.UserMapper;
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
import com.example.spring_boot_project_api.service.AuthService;
import com.example.spring_boot_project_api.util.RoleNames;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final Map<String, String> SUPPORTED_PROVIDERS = Map.of(
            "google", "Google",
            "github", "GitHub");

    // RestClient is instantiated directly (verified against the provider APIs with
    // a short timeout) rather than injected, so it never collides with the
    // Bakong / Gemini RestTemplate beans.
    private static final RestClient SOCIAL_REST_CLIENT = buildSocialRestClient();

    private static RestClient buildSocialRestClient() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofSeconds(10));
        factory.setReadTimeout(java.time.Duration.ofSeconds(15));
        // GitHub rejects API calls without a User-Agent, so set one for all providers.
        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "smart-tourism-api")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SocialAuthConfig socialAuthConfig;
    private final SocialAccountRepository socialAccountRepository;

    @Value("${auth.reset-token-expiration-minutes:30}")
    private long resetTokenExpirationMinutes;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Roles defaultRole = roleRepository.findByName(RoleNames.TOURIST)
                .orElseThrow(() -> new IllegalStateException(
                        "Default " + RoleNames.TOURIST + " role is not configured"));

        Users user = new Users();
        user.setFullname(request.getFullname());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender() != null ? request.getGender() : GenderEnum.Male);
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setDateOfBirth(request.getDateOfBirth());
        Users saved = userRepository.save(user);

        UserRoles userRole = new UserRoles();
        userRole.setUser(saved);
        userRole.setRole(defaultRole);
        userRoleRepository.save(userRole);
        saved.getUserRoles().add(userRole);

        return buildAuthResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
            AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
            Users user = userRepository.findByUsername(principal.getUsername())
                    .or(() -> userRepository.findByEmail(principal.getUsername()))
                    .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
            return buildAuthResponse(user);
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse loginWithSocial(String provider, SocialLoginRequest request) {
        String normalizedProvider = provider == null ? "" : provider.trim().toLowerCase(Locale.ROOT);
        String displayName = SUPPORTED_PROVIDERS.get(normalizedProvider);
        if (displayName == null) {
            throw new IllegalArgumentException("Unsupported social provider: " + provider);
        }

        String token = trimToNull(request.getToken());
        String code = trimToNull(request.getCode());
        if (token == null && code == null) {
            throw new IllegalArgumentException("A social token or authorization code is required");
        }

        SocialUser social = verifySocialToken(normalizedProvider, token, code);
        if (social.id() == null || social.id().isBlank()) {
            throw new UnauthorizedException(
                    "The " + displayName + " account did not return a user id");
        }
        String providerUserId = social.id().trim();
        if (normalizedProvider.equals("google") && !social.emailVerified()) {
            throw new UnauthorizedException("Your Google email address is not verified");
        }

        String email = normalizeEmail(social.email());
        Users user = socialAccountRepository
                .findByProviderAndProviderUserId(normalizedProvider, providerUserId)
                .map(SocialAccount::getUser)
                .orElse(null);

        if (user == null) {
            user = email == null ? null : userRepository.findByEmailIgnoreCase(email).orElse(null);
            if (user == null) {
                user = createSocialUser(normalizedProvider, displayName, social, email);
            }
            linkSocialAccount(normalizedProvider, providerUserId, user);
        }
        return buildAuthResponse(user);
    }

    private SocialUser verifySocialToken(String provider, String token, String code) {
        try {
            if (provider.equals("google")) {
                String url = socialAuthConfig.getGoogleTokenInfoUrl() + "?access_token=" + token;
                Map<String, Object> info = SOCIAL_REST_CLIENT.get().uri(url)
                        .retrieve().body(Map.class);
                String audience = socialAuthConfig.getGoogleAudience();
                if (info != null && audience != null && !audience.isBlank()
                        && !audience.equals(stringOf(info.get("aud")))) {
                    throw new UnauthorizedException("Google token was issued for a different client");
                }
                return new SocialUser(
                        stringOf(info == null ? null : info.get("sub")),
                        stringOf(info == null ? null : info.get("email")),
                        stringOf(info == null ? null : info.get("name")),
                        !"false".equalsIgnoreCase(stringOf(info == null ? null : info.get("email_verified"))),
                        null);
            }
            if (provider.equals("github")) {
                String accessToken = exchangeGithubCode(code);
                Map<String, Object> profile = githubGet(accessToken, "/user");
                if (profile == null) {
                    throw new UnauthorizedException("GitHub did not return a profile");
                }
                log.debug("GitHub /user returned fields: {}", profile.keySet());
                String login = stringOf(profile.get("login"));
                // /user only exposes a public email; private addresses need /user/emails.
                String email = normalizeEmail(stringOf(profile.get("email")));
                if (email == null) {
                    email = primaryVerifiedGithubEmail(accessToken);
                }
                return new SocialUser(
                        stringOf(profile.get("id")),
                        email,
                        trimToNull(stringOf(profile.get("name"))) != null
                                ? stringOf(profile.get("name"))
                                : login,
                        email != null,
                        login);
            }
            throw new UnauthorizedException("Unsupported social provider: " + provider);
        } catch (HttpClientErrorException ex) {
            throw new UnauthorizedException("The social token is invalid or expired");
        } catch (RestClientException ex) {
            throw new UnauthorizedException("Could not verify your social account: " + ex.getMessage());
        }
    }

    private String exchangeGithubCode(String code) {
        String clientId = socialAuthConfig.getGithubClientId();
        String clientSecret = socialAuthConfig.getGithubClientSecret();
        if (isBlank(clientId) || isBlank(clientSecret)) {
            throw new IllegalStateException(
                    "GitHub login is not configured (set GITHUB_CLIENT_ID and GITHUB_CLIENT_SECRET)");
        }
        Map<String, Object> body = SOCIAL_REST_CLIENT.post()
                .uri(socialAuthConfig.getGithubTokenUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("client_id", clientId, "client_secret", clientSecret, "code", code))
                .retrieve().body(Map.class);
        String accessToken = trimToNull(stringOf(body == null ? null : body.get("access_token")));
        if (accessToken == null) {
            String description = stringOf(body == null ? null : body.get("error_description"));
            throw new UnauthorizedException("GitHub rejected the authorization code"
                    + (isBlank(description) ? "" : ": " + description));
        }
        return accessToken;
    }

    private Map<String, Object> githubGet(String accessToken, String path) {
        return SOCIAL_REST_CLIENT.get()
                .uri(socialAuthConfig.getGithubApiUrl() + path)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve().body(Map.class);
    }

    private String primaryVerifiedGithubEmail(String accessToken) {
        List<Map<String, Object>> emails = SOCIAL_REST_CLIENT.get()
                .uri(socialAuthConfig.getGithubApiUrl() + "/user/emails")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve().body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        if (emails == null) {
            return null;
        }
        return emails.stream()
                .filter(entry -> Boolean.TRUE.equals(entry.get("verified")))
                .sorted(Comparator.comparing(entry -> !Boolean.TRUE.equals(entry.get("primary"))))
                .map(entry -> normalizeEmail(stringOf(entry.get("email"))))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Users createSocialUser(String provider, String displayName, SocialUser social, String email) {
        Roles defaultRole = roleRepository.findByName(RoleNames.TOURIST)
                .orElseThrow(() -> new IllegalStateException(
                        "Default " + RoleNames.TOURIST + " role is not configured"));

        String name = social.name() == null || social.name().isBlank()
                ? socialNameFromEmail(email, displayName)
                : social.name().trim();
        if (name.length() > 200) {
            name = name.substring(0, 200);
        }
        // GitHub handles are already unique and human-readable, so use them when
        // the provider supplies one; uniqueUsername() still guards collisions.
        String hinted = trimToNull(social.usernameHint());
        String usernameBase = hinted != null ? hinted : provider + "_" + social.id();
        if (usernameBase.length() > 200) {
            usernameBase = usernameBase.substring(0, 200);
        }

        Users user = new Users();
        user.setFullname(name);
        user.setUsername(uniqueUsername(usernameBase));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setGender(GenderEnum.Male);
        Users saved = userRepository.save(user);

        UserRoles userRole = new UserRoles();
        userRole.setUser(saved);
        userRole.setRole(defaultRole);
        userRoleRepository.save(userRole);
        saved.getUserRoles().add(userRole);
        return saved;
    }

    private void linkSocialAccount(String provider, String providerUserId, Users user) {
        SocialAccount socialAccount = new SocialAccount();
        socialAccount.setProvider(provider);
        socialAccount.setProviderUserId(providerUserId);
        socialAccount.setUser(user);
        socialAccountRepository.save(socialAccount);
        if (user.getSocialAccounts() != null) {
            user.getSocialAccounts().add(socialAccount);
        }
    }

    private static String socialNameFromEmail(String email, String displayName) {
        if (email != null) {
            int at = email.indexOf('@');
            if (at > 0) {
                return email.substring(0, at);
            }
        }
        return displayName + " User";
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private String uniqueUsername(String base) {
        String safeBase = base.length() > 200 ? base.substring(0, 200) : base;
        String candidate = safeBase;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            String suffixText = "_" + suffix++;
            int prefixLength = Math.max(0, 200 - suffixText.length());
            candidate = safeBase.substring(0, Math.min(safeBase.length(), prefixLength)) + suffixText;
        }
        return candidate;
    }

    private static String stringOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record SocialUser(String id, String email, String name, boolean emailVerified,
                              String usernameHint) {
    }

    @Override
    @Transactional
    public AuthResponse updateProfile(ProfileUpdateRequest request) {
        Users user = currentUser();
        String email = normalizeEmail(request.getEmail());
        if (email != null && userRepository.existsByEmailIgnoreCase(email)
                && (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email))) {
            throw new IllegalArgumentException("Email is already registered");
        }
        user.setFullname(request.getFullname());
        user.setEmail(email);
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        Users saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No account found with this email"));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiresAt(expiresAt);
        resetToken.setUser(user);
        passwordResetTokenRepository.save(resetToken);

        return ForgotPasswordResponse.builder()
                .token(token)
                .expiresAt(expiresAt.toInstant(ZoneOffset.UTC))
                .message("Use this reset token to reset your password before it expires")
                .build();
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));
        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Reset token has already been used");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        Users user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponse("Password has been reset successfully");
    }

    @Override
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request) {
        Users user = currentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return new MessageResponse("Password has been changed successfully");
    }

    @Override
    @Transactional
    public MessageResponse logout(String bearerToken) {
        String token = extractToken(bearerToken);
        if (token == null || token.isBlank()) {
            return new MessageResponse("Logged out successfully");
        }
        try {
            Date expiration = jwtService.extractExpiration(token);
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            if (remainingMs > 0) {
                tokenBlacklistService.blacklist(token, Duration.ofMillis(remainingMs));
            }
        } catch (Exception ignored) {
            // Token already invalid/expired — nothing to revoke.
        }
        return new MessageResponse("Logged out successfully");
    }

    private String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(TOKEN_TYPE + " ")) {
            return bearerToken.substring(TOKEN_TYPE.length() + 1);
        }
        return bearerToken;
    }

    private AuthResponse buildAuthResponse(Users user) {
        String token = jwtService.generateToken(user);
        Instant expiresAt = jwtService.extractExpiration(token).toInstant();
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType(TOKEN_TYPE)
                .expiresAt(expiresAt)
                .user(UserMapper.toResponse(user))
                .build();
    }

    private Users currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails)) {
            throw new UnauthorizedException("Authentication required");
        }
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));
    }
}