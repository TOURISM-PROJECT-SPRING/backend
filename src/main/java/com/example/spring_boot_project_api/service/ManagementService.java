package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.OwnerAdminRequest;
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

public interface ManagementService {

    List<UserResponse> findAllUsers();

    UserResponse findUserById(Long id);

    UserResponse updateUser(Long id, UserAdminUpdateRequest request);

    MessageResponse deleteUser(Long id);

    List<OwnerResponse> findAllOwners();

    OwnerResponse findOwnerById(Long id);

    ApiResponse<OwnerResponse> createOwner(OwnerAdminRequest request);

    OwnerResponse updateOwner(Long id, OwnerAdminRequest request);

    OwnerResponse verifyOwner(Long id, String status);

    MessageResponse deleteOwner(Long id);

    List<RoleResponse> findAllRoles();

    ApiResponse<RoleResponse> createRole(String name);

    RoleResponse updateRole(Long id, String name);

    MessageResponse deleteRole(Long id);

    List<ReviewResponse> findAllReviews();

    List<PromotionResponse> findAllPromotions();

    List<NotificationResponse> findAllNotifications();

    List<PaymentResponse> findAllPayments();

    List<TourPackageResponse> findAllTourPackages();

    List<TourGuideResponse> findAllTourGuides();
}
