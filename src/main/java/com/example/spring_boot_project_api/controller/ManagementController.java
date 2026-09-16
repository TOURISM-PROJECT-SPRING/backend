package com.example.spring_boot_project_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.UserAdminUpdateRequest;
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

    @GetMapping("/owners")
    public ResponseEntity<List<OwnerResponse>> getOwners() {
        return ResponseEntity.ok(managementService.findAllOwners());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(managementService.findAllRoles());
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