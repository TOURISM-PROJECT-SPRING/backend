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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.spring_boot_project_api.config.SocialAuthConfig;
import com.example.spring_boot_project_api.dto.request.ChangePasswordRequest;
import com.example.spring_boot_project_api.dto.request.FacebookLinkRequest;
import com.example.spring_boot_project_api.dto.response.ConnectedAccountsResponse;
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

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Duration FACEBOOK_LINK_WINDOW = Duration.ofMinutes(5);
    private static final String TOKEN_TYPE = "Bearer";
    private static final Map<String, String> SUPPORTED_PROVIDERS = Map.of(
            "google", "Google",
            "github", "GitHub",
            "facebook", "Facebook");

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

    private RestClient socialRestClient = SOCIAL_REST_CLIENT;

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
    private final PlatformTransactionManager transactionManager;

    @Value("${auth.dev-password-reset-enabled:false}")
    private boolean devPasswordResetEnabled;

    @Value("${auth.reset-token-expiration-minutes:30}")
    private long resetTokenExpirationMinutes;

    @Value("${social.facebook.app-id:}")
    private String facebookAppId;

    @Value("${social.facebook.app-secret:}")
    private String facebookAppSecret;

    @Value("${social.facebook.graph-version:v24.0}")
    private String facebookGraphVersion;

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
            Users user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
            return buildAuthResponse(user, true);
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

        boolean canLinkFacebook = user != null && normalizedProvider.equals("google")
                && !isBlank(socialAuthConfig.getGoogleAudience());
        if (user == null) {
            Users existingByEmail = email == null ? null
                    : userRepository.findByEmailIgnoreCase(email).orElse(null);
            // A Facebook profile email is not sufficient proof of ownership of an
            // existing local account. Require explicit account linking instead.
            if (normalizedProvider.equals("facebook") && existingByEmail != null) {
                throw new IllegalArgumentException(
                        "This email is already registered. Sign in to the existing account before linking Facebook.");
            }
            if (normalizedProvider.equals("google") && existingByEmail != null
                    && !social.emailExplicitlyVerified()) {
                throw new UnauthorizedException("Google must verify your email to sign in to the existing account");
            }
            user = existingByEmail;
            if (user == null) {
                user = createSocialUser(normalizedProvider, displayName, social, email);
            }
            linkSocialAccount(normalizedProvider, providerUserId, user);
        }
        return buildAuthResponse(user, canLinkFacebook);
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectedAccountsResponse connectedAccounts() {
        AppUserDetails principal = currentPrincipal();
        Instant deadline = facebookLinkDeadline(principal);
        return new ConnectedAccountsResponse(socialAccountRepository.findProvidersByUserId(principal.getId()), deadline);
    }

    @Override
    public ConnectedAccountsResponse linkFacebook(FacebookLinkRequest request) {
        AppUserDetails principal = currentPrincipal();
        Instant deadline = facebookLinkDeadline(principal);
        if (deadline == null) {
            throw new AccessDeniedException("Sign in again with your password or connected Google account before linking Facebook.");
        }

        String facebookId = verifySocialToken("facebook", request.token().trim(), null).id();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return transaction.execute(status -> persistFacebookLink(principal.getId(), facebookId, deadline));
    }

    private ConnectedAccountsResponse persistFacebookLink(Long userId, String facebookId, Instant deadline) {
        socialAccountRepository.lockUserForLinking(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        List<String> providers = socialAccountRepository.findProvidersByUserId(userId);
        if (!Instant.now().isBefore(deadline)) {
            throw new AccessDeniedException("Your recent sign-in expired. Sign in again before linking Facebook.");
        }

        if (!providers.contains("facebook") && socialAccountRepository.insertFacebookLink(userId, facebookId) == 1) {
            return new ConnectedAccountsResponse(socialAccountRepository.findProvidersByUserId(userId), deadline);
        }
        boolean alreadyLinkedHere = socialAccountRepository.findByProviderAndProviderUserId("facebook", facebookId)
                .map(account -> userId.equals(account.getUser().getId()))
                .orElse(false);
        if (alreadyLinkedHere) {
            return new ConnectedAccountsResponse(providers, deadline);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "A Facebook account is already linked");
    }

    private Instant facebookLinkDeadline(AppUserDetails principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getCredentials() instanceof String token)) {
            throw new UnauthorizedException("Authentication required");
        }
        try {
            if (!principal.getId().equals(jwtService.extractUserId(token))) {
                throw new UnauthorizedException("Authentication required");
            }
            Long authenticatedAt = jwtService.extractFacebookLinkAuthenticatedAt(token);
            Instant now = Instant.now();
            if (authenticatedAt == null || authenticatedAt > now.getEpochSecond()) {
                return null;
            }
            Instant deadline = Instant.ofEpochSecond(authenticatedAt).plus(FACEBOOK_LINK_WINDOW);
            return now.isBefore(deadline) ? deadline : null;
        } catch (ExpiredJwtException ex) {
            throw new UnauthorizedException("Authentication required");
        }
    }

    private SocialUser verifySocialToken(String provider, String token, String code) {
        try {
            if (provider.equals("google")) {
                if (token == null) {
                    throw new UnauthorizedException("Google access token is required");
                }
                Map<String, Object> info = socialRestClient.get()
                        .uri(socialAuthConfig.getGoogleTokenInfoUrl() + "?access_token={token}", token)
                        .retrieve().body(Map.class);
                String audience = socialAuthConfig.getGoogleAudience();
                if (info != null && audience != null && !audience.isBlank()
                        && !audience.equals(stringOf(info.get("aud")))) {
                    throw new UnauthorizedException("Google token was issued for a different client");
                }
                String emailVerified = stringOf(info == null ? null : info.get("email_verified"));
                return new SocialUser(
                        stringOf(info == null ? null : info.get("sub")),
                        stringOf(info == null ? null : info.get("email")),
                        stringOf(info == null ? null : info.get("name")),
                        !"false".equalsIgnoreCase(emailVerified),
                        null,
                        "true".equalsIgnoreCase(emailVerified));
            }
            if (provider.equals("facebook")) {
                if (token == null) {
                    throw new UnauthorizedException("Facebook access token is required");
                }
                return verifyFacebookToken(token);
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
                        login,
                        email != null);
            }
            throw new UnauthorizedException("Unsupported social provider: " + provider);
        } catch (HttpClientErrorException ex) {
            throw new UnauthorizedException("The social token is invalid or expired");
        } catch (RestClientException ex) {
            throw new UnauthorizedException("Could not verify your social account");
        }
    }

    /** Verify Facebook access token's validity and app binding before using profile fields. */
    private SocialUser verifyFacebookToken(String userAccessToken) {
        if (isBlank(facebookAppId) || isBlank(facebookAppSecret)) {
            throw new IllegalStateException("Facebook login is not configured");
        }

        // Meta /debug_token requires an app access token and returns the app ID,
        // validity and user ID of the user access token.
        Map<?, ?> inspection = socialRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("graph.facebook.com")
                        .path("/" + facebookGraphVersion + "/debug_token")
                        .queryParam("input_token", "{token}")
                        .build(userAccessToken))
                .headers(headers -> headers.setBearerAuth(facebookAppId + "|" + facebookAppSecret))
                .retrieve().body(Map.class);

        if (inspection == null || !(inspection.get("data") instanceof Map<?, ?> data)) {
            throw new UnauthorizedException("Facebook token verification failed");
        }
        if (!Boolean.TRUE.equals(data.get("is_valid"))
                || isExpiredFacebookTimestamp(data.get("expires_at"))
                || isExpiredFacebookTimestamp(data.get("data_access_expires_at"))) {
            throw new UnauthorizedException("Facebook access token is invalid");
        }
        if (!facebookAppId.equals(stringOf(data.get("app_id")))) {
            throw new UnauthorizedException("Facebook token belongs to a different application");
        }
        String verifiedUserId = trimToNull(stringOf(data.get("user_id")));
        if (verifiedUserId == null || !verifiedUserId.matches("[0-9]{1,255}")) {
            throw new UnauthorizedException("Facebook user id is missing");
        }

        // Fetch only basic fields granted to the application. Email may be absent.
        Map<?, ?> profile = socialRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("graph.facebook.com")
                        .path("/" + facebookGraphVersion + "/me")
                        .queryParam("fields", "id,name,email")
                        .build())
                .headers(headers -> headers.setBearerAuth(userAccessToken))
                .retrieve().body(Map.class);

        if (profile == null || !verifiedUserId.equals(stringOf(profile.get("id")))) {
            throw new UnauthorizedException("Facebook user identity mismatch");
        }
        return new SocialUser(
                verifiedUserId,
                normalizeEmail(stringOf(profile.get("email"))),
                trimToNull(stringOf(profile.get("name"))),
                false,
                null,
                false);
    }

    private static boolean isExpiredFacebookTimestamp(Object timestamp) {
        return timestamp instanceof Number seconds && seconds.longValue() != 0
                && seconds.longValue() <= Instant.now().getEpochSecond();
    }

    private String exchangeGithubCode(String code) {
        String clientId = socialAuthConfig.getGithubClientId();
        String clientSecret = socialAuthConfig.getGithubClientSecret();
        if (isBlank(clientId) || isBlank(clientSecret)) {
            throw new IllegalStateException(
                    "GitHub login is not configured (set GITHUB_CLIENT_ID and GITHUB_CLIENT_SECRET)");
        }
        Map<String, Object> body = socialRestClient.post()
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
        return socialRestClient.get()
                .uri(socialAuthConfig.getGithubApiUrl() + path)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve().body(Map.class);
    }

    private String primaryVerifiedGithubEmail(String accessToken) {
        List<Map<String, Object>> emails = socialRestClient.get()
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
                              String usernameHint, boolean emailExplicitlyVerified) {
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
        requireDevPasswordReset();
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
        requireDevPasswordReset();
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

    private void requireDevPasswordReset() {
        if (!devPasswordResetEnabled) {
            throw new AccessDeniedException("Password reset is unavailable");
        }
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
        return buildAuthResponse(user, false);
    }

    private AuthResponse buildAuthResponse(Users user, boolean canLinkFacebook) {
        String token = canLinkFacebook
                ? jwtService.generateRecentlyAuthenticatedToken(user)
                : jwtService.generateToken(user);
        Instant expiresAt = jwtService.extractExpiration(token).toInstant();
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType(TOKEN_TYPE)
                .expiresAt(expiresAt)
                .user(UserMapper.toResponse(user))
                .build();
    }

    private AppUserDetails currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AppUserDetails principal)) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal;
    }

    private Users currentUser() {
        AppUserDetails principal = currentPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));
    }
}