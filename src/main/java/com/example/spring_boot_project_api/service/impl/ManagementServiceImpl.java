package com.example.spring_boot_project_api.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.example.spring_boot_project_api.enums.GenderEnum;
import com.example.spring_boot_project_api.enums.UserEnum;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.model.BusinesssOwnerProfiles;
import com.example.spring_boot_project_api.model.Notifications;
import com.example.spring_boot_project_api.model.Payments;
import com.example.spring_boot_project_api.model.Promotions;
import com.example.spring_boot_project_api.model.Reviews;
import com.example.spring_boot_project_api.model.Roles;
import com.example.spring_boot_project_api.model.TourGuides;
import com.example.spring_boot_project_api.model.TourPackages;
import com.example.spring_boot_project_api.model.UserRoles;
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
import com.example.spring_boot_project_api.repository.UserRoleRepository;
import com.example.spring_boot_project_api.service.ManagementService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagementServiceImpl implements ManagementService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final BusinessOwnerProfileRepository businessOwnerProfileRepository;
    private final RoleRepository roleRepository;
    private final ReviewRepository reviewRepository;
    private final PromotionRepository promotionRepository;
    private final NotificationRepository notificationRepository;
    private final PaymentRepository paymentRepository;
    private final TourPackageRepository tourPackageRepository;
    private final TourGuideRepository tourGuideRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse findUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserAdminUpdateRequest request) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already taken: " + request.getEmail());
        }
        if (!user.getUsername().equalsIgnoreCase(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken: " + request.getUsername());
        }

        user.setFullname(request.getFullname());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        if (request.getRoles() != null) {
            user.getUserRoles().clear();
            for (String roleName : request.getRoles()) {
                Roles role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role name: " + roleName));
                UserRoles userRole = new UserRoles();
                userRole.setUser(user);
                userRole.setRole(role);
                user.getUserRoles().add(userRole);
            }
        }

        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public MessageResponse deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        return MessageResponse.builder().message("User deleted successfully").build();
    }

    @Override
    public List<OwnerResponse> findAllOwners() {
        return businessOwnerProfileRepository.findAll().stream()
                .map(this::toOwnerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OwnerResponse findOwnerById(Long id) {
        return businessOwnerProfileRepository.findById(id)
                .map(this::toOwnerResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Business Owner Profile", id));
    }

    @Override
    @Transactional
    public ApiResponse<OwnerResponse> createOwner(OwnerAdminRequest request) {
        Users user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));
        } else if (request.getUserEmail() != null && !request.getUserEmail().isBlank()) {
            String email = request.getUserEmail().trim().toLowerCase();
            user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = new Users();
                String fullname = (request.getUserName() != null && !request.getUserName().isBlank())
                        ? request.getUserName().trim()
                        : "Business Owner";
                String baseUsername = email.contains("@") ? email.substring(0, email.indexOf('@')) : "owner";
                String username = baseUsername;
                int counter = 1;
                while (userRepository.existsByUsername(username)) {
                    username = baseUsername + "_" + counter++;
                }
                user.setFullname(fullname);
                user.setEmail(email);
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode("owner123"));
                user.setGender(GenderEnum.Male);
                user.setStatus(UserEnum.Online);
                if (request.getAddress() != null && !request.getAddress().isBlank()) {
                    user.setAddress(request.getAddress().trim());
                }
                user = userRepository.save(user);

                // Assign OWNER role
                Roles ownerRole = roleRepository.findByName("OWNER")
                        .orElseGet(() -> {
                            Roles r = new Roles();
                            r.setName("OWNER");
                            return roleRepository.save(r);
                        });
                UserRoles ur = new UserRoles();
                ur.setUser(user);
                ur.setRole(ownerRole);
                userRoleRepository.save(ur);
            }
        }

        if (user == null) {
            // Pick or create fallback owner user
            user = userRepository.findAll().stream()
                    .filter(u -> u.getUserRoles().stream()
                            .anyMatch(ur -> ur.getRole() != null && "OWNER".equalsIgnoreCase(ur.getRole().getName())))
                    .findFirst()
                    .orElseGet(() -> {
                        Users fallback = new Users();
                        fallback.setFullname("Business Owner");
                        fallback.setUsername("owner_" + UUID.randomUUID().toString().substring(0, 8));
                        fallback.setEmail("owner_" + System.currentTimeMillis() + "@smart-tourism.com");
                        fallback.setPassword(passwordEncoder.encode("owner123"));
                        fallback.setGender(GenderEnum.Male);
                        fallback.setStatus(UserEnum.Online);
                        return userRepository.save(fallback);
                    });
        }

        String license = request.getBusinessLicenseNo();
        if (license == null || license.isBlank()) {
            license = "LIC-" + System.currentTimeMillis();
        } else if (businessOwnerProfileRepository.existsByBusinessLicenseNo(license)) {
            license = license + "-" + UUID.randomUUID().toString().substring(0, 4);
        }

        BusinesssOwnerProfiles profile = new BusinesssOwnerProfiles();
        profile.setBusinessName(request.getBusinessName());
        profile.setBusinessLicenseNo(license);

        String status = (request.getVerificationStatus() != null && !request.getVerificationStatus().isBlank())
                ? request.getVerificationStatus().trim().toUpperCase()
                : "PENDING";
        profile.setVerificationStatus(status);
        if ("VERIFIED".equalsIgnoreCase(status)) {
            profile.setVerifiedAt(LocalDate.now());
        } else {
            profile.setVerifiedAt(null);
        }
        profile.setUsers(user);

        return ApiResponse.<OwnerResponse>builder()
                .message("Owner created successfully")
                .data(toOwnerResponse(businessOwnerProfileRepository.save(profile)))
                .build();
    }

    @Override
    @Transactional
    public OwnerResponse updateOwner(Long id, OwnerAdminRequest request) {
        BusinesssOwnerProfiles profile = businessOwnerProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business Owner Profile", id));

        if (request.getBusinessName() != null && !request.getBusinessName().isBlank()) {
            profile.setBusinessName(request.getBusinessName().trim());
        }
        if (request.getBusinessLicenseNo() != null && !request.getBusinessLicenseNo().isBlank()) {
            profile.setBusinessLicenseNo(request.getBusinessLicenseNo().trim());
        }
        if (request.getVerificationStatus() != null && !request.getVerificationStatus().isBlank()) {
            String newStatus = request.getVerificationStatus().trim().toUpperCase();
            profile.setVerificationStatus(newStatus);
            if ("VERIFIED".equalsIgnoreCase(newStatus)) {
                if (profile.getVerifiedAt() == null) {
                    profile.setVerifiedAt(LocalDate.now());
                }
            } else {
                profile.setVerifiedAt(null);
            }
        }

        if (profile.getUsers() != null) {
            Users user = profile.getUsers();
            boolean userChanged = false;
            if (request.getUserName() != null && !request.getUserName().isBlank()) {
                user.setFullname(request.getUserName().trim());
                userChanged = true;
            }
            if (request.getUserEmail() != null && !request.getUserEmail().isBlank()
                    && !request.getUserEmail().equalsIgnoreCase(user.getEmail())) {
                String newEmail = request.getUserEmail().trim().toLowerCase();
                if (!userRepository.existsByEmail(newEmail)) {
                    user.setEmail(newEmail);
                    userChanged = true;
                }
            }
            if (request.getAddress() != null) {
                user.setAddress(request.getAddress().trim());
                userChanged = true;
            }
            if (userChanged) {
                userRepository.save(user);
            }
        }

        return toOwnerResponse(businessOwnerProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public OwnerResponse verifyOwner(Long id, String status) {
        BusinesssOwnerProfiles profile = businessOwnerProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business Owner Profile", id));

        String normalized = (status != null && !status.isBlank())
                ? status.trim().toUpperCase()
                : "VERIFIED";
        profile.setVerificationStatus(normalized);
        if ("VERIFIED".equalsIgnoreCase(normalized)) {
            profile.setVerifiedAt(LocalDate.now());
        } else {
            profile.setVerifiedAt(null);
        }
        return toOwnerResponse(businessOwnerProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public MessageResponse deleteOwner(Long id) {
        if (!businessOwnerProfileRepository.existsById(id)) {
            throw new ResourceNotFoundException("Business Owner Profile", id);
        }
        businessOwnerProfileRepository.deleteById(id);
        return MessageResponse.builder().message("Business account deleted successfully").build();
    }

    @Override
    public List<RoleResponse> findAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> createRole(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        String cleanName = name.trim().toUpperCase();
        return roleRepository.findByName(cleanName)
                .map(r -> ApiResponse.<RoleResponse>builder()
                        .message("Role created successfully")
                        .data(toRoleResponse(r))
                        .build())
                .orElseGet(() -> {
                    Roles r = new Roles();
                    r.setName(cleanName);
                    return ApiResponse.<RoleResponse>builder()
                            .message("Role created successfully")
                            .data(toRoleResponse(roleRepository.save(r)))
                            .build();
                });
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, String name) {
        Roles role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        if (name != null && !name.isBlank()) {
            role.setName(name.trim().toUpperCase());
        }
        return toRoleResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public MessageResponse deleteRole(Long id) {
        Roles role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        if ("ADMIN".equalsIgnoreCase(role.getName())) {
            throw new IllegalArgumentException("Cannot delete ADMIN system role");
        }
        roleRepository.delete(role);
        return MessageResponse.builder().message("Role deleted successfully").build();
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

        String bName = o.getBusinessName() != null ? o.getBusinessName() : "";
        String bLic = o.getBusinessLicenseNo() != null ? o.getBusinessLicenseNo() : "";
        String bType = "hotel";
        String lowerName = bName.toLowerCase();
        String upperLic = bLic.toUpperCase();
        if (upperLic.contains("REST") || lowerName.matches(".*(dining|cuisine|restaurant|bistro|cafe|food).*")) {
            bType = "restaurant";
        } else if (upperLic.contains("TOUR") || lowerName.matches(".*(tour|adventure|expedition|guide|travel).*")) {
            bType = "tour";
        }

        String userAddress = o.getUsers() != null ? o.getUsers().getAddress() : null;
        String city = "Siem Reap";
        if (userAddress != null) {
            String lowerAddr = userAddress.toLowerCase();
            if (lowerAddr.contains("phnom penh")) city = "Phnom Penh";
            else if (lowerAddr.contains("siem reap")) city = "Siem Reap";
            else if (lowerAddr.contains("kampot")) city = "Kampot";
            else if (lowerAddr.contains("koh kong")) city = "Koh Kong";
            else if (lowerAddr.contains("battambang")) city = "Battambang";
            else if (lowerAddr.contains("kratie")) city = "Kratie";
            else if (lowerAddr.contains("sihanoukville")) city = "Sihanoukville";
            else if (lowerAddr.contains("kep")) city = "Kep";
        }

        return OwnerResponse.builder()
                .id(o.getId())
                .businessName(o.getBusinessName())
                .businessLicenseNo(o.getBusinessLicenseNo())
                .verificationStatus(o.getVerificationStatus())
                .verifiedAt(o.getVerifiedAt())
                .businessType(bType)
                .userId(o.getUsers() != null ? o.getUsers().getId() : null)
                .userName(o.getUsers() != null ? o.getUsers().getFullname() : null)
                .userEmail(o.getUsers() != null ? o.getUsers().getEmail() : null)
                .address(userAddress)
                .city(city)
                .phone(null)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private RoleResponse toRoleResponse(Roles r) {
        if (r == null) return null;
        return RoleResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .userCount((long) (r.getUserRoles() != null ? r.getUserRoles().size() : 0))
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private ReviewResponse toReviewResponse(Reviews r) {
        if (r == null) return null;
        String targetType = "UNKNOWN";
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
                .paymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null)
                .transactionId(p.getTransactionId())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
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
