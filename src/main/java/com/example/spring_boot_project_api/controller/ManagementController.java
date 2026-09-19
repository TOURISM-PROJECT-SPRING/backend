package com.example.spring_boot_project_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.OwnerAdminRequest;
import com.example.spring_boot_project_api.dto.request.OwnerVerifyRequest;
import com.example.spring_boot_project_api.dto.request.RoleCreateRequest;
import com.example.spring_boot_project_api.dto.request.UserAdminUpdateRequest;
import com.example.spring_boot_project_api.dto.response.ApiResponse;
import com.example.spring_boot_project_api.dto.response.MessageResponse;
import com.example.spring_boot_project_api.dto.response.NotificationResponse;
import com.example.spring_boot_project_api.dto.response.OwnerResponse;
import com.example.spring_boot_project_api.dto.response.PaymentResponse;
import com.example.spring_boot_project_api.dto.response.PromotionResponse;
import com.example.spring_boot_project_api.dto.response.ReviewResponse;
import com.example.spring_boot_project_api.dto.response.RoleResponse;
import com.example.spring_boot_project_api.dto.response.TourGuideResponse;
import com.example.spring_boot_project_api.dto.response.TourPackageResponse;
import com.example.spring_boot_project_api.dto.response.UserResponse;
import com.example.spring_boot_project_api.service.ManagementService;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/management")
@RequiredArgsConstructor
public class ManagementController {

    private final ManagementService managementService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(managementService.findAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(managementService.findUserById(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserAdminUpdateRequest request) {
        return ResponseEntity.ok(managementService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(managementService.deleteUser(id));
    }

    // Owner / Business Management
    @GetMapping("/owners")
    public ResponseEntity<List<OwnerResponse>> getOwners() {
        return ResponseEntity.ok(managementService.findAllOwners());
    }

    @GetMapping("/owners/{id}")
    public ResponseEntity<OwnerResponse> getOwnerById(@PathVariable Long id) {
        return ResponseEntity.ok(managementService.findOwnerById(id));
    }

    @PostMapping("/owners")
    public ResponseEntity<ApiResponse<OwnerResponse>> createOwner(@RequestBody @Valid OwnerAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managementService.createOwner(request));
    }

    @PutMapping("/owners/{id}")
    public ResponseEntity<OwnerResponse> updateOwner(
            @PathVariable Long id,
            @RequestBody @Valid OwnerAdminRequest request) {
        return ResponseEntity.ok(managementService.updateOwner(id, request));
    }

    @PatchMapping("/owners/{id}/verify")
    public ResponseEntity<OwnerResponse> verifyOwner(
            @PathVariable Long id,
            @RequestBody OwnerVerifyRequest request) {
        String status = request != null ? request.getStatus() : "VERIFIED";
        return ResponseEntity.ok(managementService.verifyOwner(id, status));
    }

    @DeleteMapping("/owners/{id}")
    public ResponseEntity<MessageResponse> deleteOwner(@PathVariable Long id) {
        return ResponseEntity.ok(managementService.deleteOwner(id));
    }

    // Roles Management
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(managementService.findAllRoles());
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@RequestBody @Valid RoleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managementService.createRole(request));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long id,
            @RequestBody @Valid RoleCreateRequest request) {
        return ResponseEntity.ok(managementService.updateRole(id, request));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<MessageResponse> deleteRole(@PathVariable Long id) {
        return ResponseEntity.ok(managementService.deleteRole(id));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews() {
        return ResponseEntity.ok(managementService.findAllReviews());
    }

    @GetMapping("/promotions")
    public ResponseEntity<List<PromotionResponse>> getPromotions() {
        return ResponseEntity.ok(managementService.findAllPromotions());
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        return ResponseEntity.ok(managementService.findAllNotifications());
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> getPayments() {
        return ResponseEntity.ok(managementService.findAllPayments());
    }

    @GetMapping("/tour-packages")
    public ResponseEntity<List<TourPackageResponse>> getTourPackages() {
        return ResponseEntity.ok(managementService.findAllTourPackages());
    }

    @GetMapping("/tour-guides")
    public ResponseEntity<List<TourGuideResponse>> getTourGuides() {
        return ResponseEntity.ok(managementService.findAllTourGuides());
    }
}
