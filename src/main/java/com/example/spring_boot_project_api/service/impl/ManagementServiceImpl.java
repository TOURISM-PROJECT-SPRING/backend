package com.example.spring_boot_project_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.response.NotificationResponse;
import com.example.spring_boot_project_api.dto.response.OwnerResponse;
import com.example.spring_boot_project_api.dto.response.PaymentResponse;
import com.example.spring_boot_project_api.dto.response.PromotionResponse;
import com.example.spring_boot_project_api.dto.response.ReviewResponse;
import com.example.spring_boot_project_api.dto.response.RoleResponse;
import com.example.spring_boot_project_api.dto.response.TourGuideResponse;
import com.example.spring_boot_project_api.dto.response.TourPackageResponse;
import com.example.spring_boot_project_api.dto.response.UserResponse;
import com.example.spring_boot_project_api.model.BusinesssOwnerProfiles;
import com.example.spring_boot_project_api.model.Notifications;
import com.example.spring_boot_project_api.model.Payments;
import com.example.spring_boot_project_api.model.Promotions;
import com.example.spring_boot_project_api.model.Reviews;
import com.example.spring_boot_project_api.model.Roles;
import com.example.spring_boot_project_api.model.TourGuides;
import com.example.spring_boot_project_api.model.TourPackages;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.BusinessOwnerProfileRepository;
import com.example.spring_boot_project_api.repository.NotificationRepository;
import com.example.spring_boot_project_api.repository.PaymentRepository;
import com.example.spring_boot_project_api.repository.PromotionRepository;
import com.example.spring_boot_project_api.repository.ReviewRepository;
import com.example.spring_boot_project_api.repository.RoleRepository;
import com.example.spring_boot_project_api.repository.TourGuideRepository;
import com.example.spring_boot_project_api.repository.TourPackageRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.ManagementService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagementServiceImpl implements ManagementService {

    private final UserRepository userRepository;
    private final BusinessOwnerProfileRepository businessOwnerProfileRepository;
    private final RoleRepository roleRepository;
    private final ReviewRepository reviewRepository;
    private final PromotionRepository promotionRepository;
    private final NotificationRepository notificationRepository;
    private final PaymentRepository paymentRepository;
    private final TourPackageRepository tourPackageRepository;
    private final TourGuideRepository tourGuideRepository;

    @Override
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse findUserById(Long id) {
        return toUserResponse(userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("User not found with id: " + id)));
    }

    @Override
    public List<OwnerResponse> findAllOwners() {
        return businessOwnerProfileRepository.findAll().stream()
                .map(this::toOwnerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleResponse> findAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> findAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::toReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionResponse> findAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::toPromotionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> findAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> findAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TourPackageResponse> findAllTourPackages() {
        return tourPackageRepository.findAll().stream()
                .map(this::toTourPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TourGuideResponse> findAllTourGuides() {
        return tourGuideRepository.findAll().stream()
                .map(this::toTourGuideResponse)
                .collect(Collectors.toList());
    }

    private UserResponse toUserResponse(Users u) {
        if (u == null) return null;
        return UserResponse.builder()
                .id(u.getId())
                .fullname(u.getFullname())
                .username(u.getUsername())
                .email(u.getEmail())
                .gender(u.getGender() != null ? u.getGender().name() : null)
                .address(u.getAddress())
                .dateOfBirth(u.getDateOfBirth())
                .status(u.getStatus() != null ? u.getStatus().name() : null)
                .roles(u.getUserRoles().stream()
                        .map(ur -> ur.getRole() != null ? ur.getRole().getName() : null)
                        .filter(r -> r != null)
                        .collect(Collectors.toList()))
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    private OwnerResponse toOwnerResponse(BusinesssOwnerProfiles o) {
        if (o == null) return null;
        return OwnerResponse.builder()
                .id(o.getId())
                .businessName(o.getBusinessName())
                .businessLicenseNo(o.getBusinessLicenseNo())
                .verificationStatus(o.getVerificationStatus())
                .verifiedAt(o.getVerifiedAt())
                .userId(o.getUsers() != null ? o.getUsers().getId() : null)
                .userName(o.getUsers() != null ? o.getUsers().getFullname() : null)
                .userEmail(o.getUsers() != null ? o.getUsers().getEmail() : null)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private RoleResponse toRoleResponse(Roles r) {
        if (r == null) return null;
        return RoleResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .userCount((long) r.getUserRoles().size())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private ReviewResponse toReviewResponse(Reviews r) {
        if (r == null) return null;
        String targetType = "OTHER";
        Long targetId = null;
        String targetName = null;
        if (r.getTourPlace() != null) {
            targetType = "TOUR_PLACE";
            targetId = r.getTourPlace().getId();
            targetName = r.getTourPlace().getName();
        } else if (r.getHotel() != null) {
            targetType = "HOTEL";
            targetId = r.getHotel().getId();
            targetName = r.getHotel().getHotelName();
        } else if (r.getRestaurant() != null) {
            targetType = "RESTAURANT";
            targetId = r.getRestaurant().getId();
            targetName = r.getRestaurant().getName();
        } else if (r.getTourPackage() != null) {
            targetType = "TOUR_PACKAGE";
            targetId = r.getTourPackage().getId();
            targetName = r.getTourPackage().getName();
        }
        return ReviewResponse.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .userName(r.getUser() != null ? r.getUser().getFullname() : null)
                .targetType(targetType)
                .targetId(targetId)
                .targetName(targetName)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private PromotionResponse toPromotionResponse(Promotions p) {
        if (p == null) return null;
        return PromotionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .code(p.getCode())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .status(p.getStatus())
                .startAt(p.getStartAt())
                .endAt(p.getEndAt())
                .hotelId(p.getHotels() != null ? p.getHotels().getId() : null)
                .hotelName(p.getHotels() != null ? p.getHotels().getHotelName() : null)
                .restaurantId(p.getRestraurants() != null ? p.getRestraurants().getId() : null)
                .restaurantName(p.getRestraurants() != null ? p.getRestraurants().getName() : null)
                .tourPackageId(p.getTourPackages() != null ? p.getTourPackages().getId() : null)
                .tourPackageName(p.getTourPackages() != null ? p.getTourPackages().getName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private NotificationResponse toNotificationResponse(Notifications n) {
        if (n == null) return null;
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.getIsRead())
                .userId(n.getUser() != null ? n.getUser().getId() : null)
                .userName(n.getUser() != null ? n.getUser().getFullname() : null)
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }

    private PaymentResponse toPaymentResponse(Payments p) {
        if (p == null) return null;
        String bookingType = "OTHER";
        Long referenceId = null;
        String referenceName = null;
        if (p.getRoomBookings() != null) {
            bookingType = "ROOM";
            referenceId = p.getRoomBookings().getId();
            referenceName = p.getRoomBookings().getRooms() != null
                    && p.getRoomBookings().getRooms().getHotels() != null
                    ? p.getRoomBookings().getRooms().getHotels().getHotelName() : null;
        } else if (p.getTicketBookings() != null) {
            bookingType = "TICKET";
            referenceId = p.getTicketBookings().getId();
            referenceName = p.getTicketBookings().getTickets() != null
                    ? p.getTicketBookings().getTickets().getName() : null;
        } else if (p.getFoodOrders() != null) {
            bookingType = "FOOD";
            referenceId = p.getFoodOrders().getId();
            referenceName = p.getFoodOrders().getRestuarants() != null
                    ? p.getFoodOrders().getRestuarants().getName() : null;
        } else if (p.getTourBookings() != null) {
            bookingType = "TOUR";
            referenceId = p.getTourBookings().getId();
            referenceName = p.getTourBookings().getTourPackages() != null
                    ? p.getTourBookings().getTourPackages().getName() : null;
        }
        return PaymentResponse.builder()
                .id(p.getId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .transactionId(p.getTransactionId())
                .status(p.getStatus())
                .paidAt(p.getPaidAt())
                .bookingType(bookingType)
                .referenceId(referenceId)
                .referenceName(referenceName)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private TourPackageResponse toTourPackageResponse(TourPackages p) {
        if (p == null) return null;
        double avg = p.getReviews().isEmpty() ? 0.0
                : p.getReviews().stream().mapToInt(Reviews::getRating).average().orElse(0.0);
        String locationName = null;
        if (p.getLocations() != null) {
            locationName = p.getLocations().getDistrict() != null ? p.getLocations().getDistrict() : "";
            if (p.getLocations().getProvince() != null) {
                locationName = locationName.isEmpty()
                        ? p.getLocations().getProvince()
                        : locationName + ", " + p.getLocations().getProvince();
            }
        }
        return TourPackageResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .durationDays(p.getDurationDays())
                .maxPeople(p.getMaxPeople())
                .price(p.getPrice())
                .locationId(p.getLocations() != null ? p.getLocations().getId() : null)
                .locationName(locationName)
                .tourGuideId(p.getToureGuides() != null ? p.getToureGuides().getId() : null)
                .tourGuideName(p.getToureGuides() != null && p.getToureGuides().getUser() != null
                        ? p.getToureGuides().getUser().getFullname() : null)
                .reviewCount((long) p.getReviews().size())
                .avgRating(avg > 0 ? Math.round(avg * 10.0) / 10.0 : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private TourGuideResponse toTourGuideResponse(TourGuides g) {
        if (g == null) return null;
        return TourGuideResponse.builder()
                .id(g.getId())
                .languageSpoken(g.getLanguageSpoken())
                .experienceYear(g.getExperienceYear())
                .ratePerDay(g.getRatePerDay())
                .userId(g.getUser() != null ? g.getUser().getId() : null)
                .userName(g.getUser() != null ? g.getUser().getFullname() : null)
                .userEmail(g.getUser() != null ? g.getUser().getEmail() : null)
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}