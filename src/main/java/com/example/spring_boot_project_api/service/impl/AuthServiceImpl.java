package com.example.spring_boot_project_api.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
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

import com.example.spring_boot_project_api.dto.request.ChangePasswordRequest;
import com.example.spring_boot_project_api.dto.request.ForgotPasswordRequest;
import com.example.spring_boot_project_api.dto.request.LoginRequest;
import com.example.spring_boot_project_api.dto.request.ProfileUpdateRequest;
import com.example.spring_boot_project_api.dto.request.RegisterRequest;
import com.example.spring_boot_project_api.dto.request.ResetPasswordRequest;
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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

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