package com.example.spring_boot_project_api.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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
import com.example.spring_boot_project_api.model.UserRoles;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.PasswordResetTokenRepository;
import com.example.spring_boot_project_api.repository.RoleRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.repository.UserRoleRepository;
import com.example.spring_boot_project_api.security.AppUserDetails;
import com.example.spring_boot_project_api.security.JwtService;
import com.example.spring_boot_project_api.security.TokenBlacklistService;
import com.example.spring_boot_project_api.service.AuthService;
import com.example.spring_boot_project_api.util.RoleNames;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final Map<String, String> SUPPORTED_PROVIDERS = Map.of(
            "google", "Google",
            "facebook", "Facebook");

    // RestClient is instantiated directly (verified against the provider APIs with
    // a short timeout) rather than injected, so it never collides with the
    // Bakong / Gemini RestTemplate beans.
    private static final RestClient SOCIAL_REST_CLIENT = buildSocialRestClient();

    private static RestClient buildSocialRestClient() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofSeconds(10));
        factory.setReadTimeout(java.time.Duration.ofSeconds(15));
        return RestClient.builder().requestFactory(factory).build();
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
        String displayName = SUPPORTED_PROVIDERS.get(provider);
        if (displayName == null) {
            throw new IllegalArgumentException("Unsupported social provider: " + provider);
        }
        SocialUser social = verifySocialToken(provider, request.getToken());
        if (social.email() == null || social.email().isBlank()) {
            throw new UnauthorizedException(
                    "Your " + displayName + " account does not provide an email address");
        }
        if (provider.equals("google") && !social.emailVerified()) {
            throw new UnauthorizedException("Your Google email address is not verified");
        }
        Users user = userRepository.findByEmail(social.email()).orElse(null);
        if (user == null) {
            user = createSocialUser(provider, social);
        }
        return buildAuthResponse(user);
    }

    private SocialUser verifySocialToken(String provider, String token) {
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
                        !"false".equalsIgnoreCase(stringOf(info == null ? null : info.get("email_verified"))));
            }
            if (provider.equals("facebook")) {
                String url = socialAuthConfig.getFacebookMeUrl()
                        + "?fields=id,name,email&access_token=" + token;
                Map<String, Object> info = SOCIAL_REST_CLIENT.get().uri(url)
                        .retrieve().body(Map.class);
                return new SocialUser(
                        stringOf(info == null ? null : info.get("id")),
                        stringOf(info == null ? null : info.get("email")),
                        stringOf(info == null ? null : info.get("name")),
                        true);
            }
            throw new UnauthorizedException("Unsupported social provider: " + provider);
        } catch (HttpClientErrorException ex) {
            throw new UnauthorizedException("The social token is invalid or expired");
        } catch (RestClientException ex) {
            throw new UnauthorizedException("Could not verify your social account: " + ex.getMessage());
        }
    }

    private Users createSocialUser(String provider, SocialUser social) {
        Roles defaultRole = roleRepository.findByName(RoleNames.TOURIST)
                .orElseThrow(() -> new IllegalStateException(
                        "Default " + RoleNames.TOURIST + " role is not configured"));

        String name = social.name() != null && !social.name().isBlank()
                ? social.name()
                : social.email().substring(0, social.email().indexOf('@'));

        Users user = new Users();
        user.setFullname(name);
        user.setUsername(uniqueUsername(provider + "_" + social.id()));
        user.setEmail(social.email());
        // Random unusable password: the account is only ever signed in via the
        // provider, not with a password.
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

    private String uniqueUsername(String base) {
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }

    private static String stringOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record SocialUser(String id, String email, String name, boolean emailVerified) {
    }

    @Override
    @Transactional
    public AuthResponse updateProfile(ProfileUpdateRequest request) {
        Users user = currentUser();
        if (userRepository.existsByEmail(request.getEmail())
                && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        user.setFullname(request.getFullname());
        user.setEmail(request.getEmail());
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