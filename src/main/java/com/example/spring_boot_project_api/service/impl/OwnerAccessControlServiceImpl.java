package com.example.spring_boot_project_api.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.response.OwnerManagedBusinessDTO;
import com.example.spring_boot_project_api.dto.response.OwnerPermissionsDTO;
import com.example.spring_boot_project_api.model.BusinesssOwnerProfiles;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.BusinessOwnerProfileRepository;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.OwnerAccessControlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerAccessControlServiceImpl implements OwnerAccessControlService {

    private final BusinessOwnerProfileRepository businessOwnerProfileRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RestaurantRepository restaurantRepository;
    private final TourismPlaceRepository tourismPlaceRepository;

    public static String normalizeType(String type) {
        if (type == null) return "";
        String upper = type.trim().toUpperCase(Locale.ROOT);
        if ("TOURS".equals(upper) || "TOURIST".equals(upper) || "TOURISTS".equals(upper) || "ATTRACTION".equals(upper)) {
            return "TOUR";
        }
        if ("ROOM".equals(upper) || "HOTELS".equals(upper) || "STAY".equals(upper) || "ACCOMMODATION".equals(upper)) {
            return "HOTEL";
        }
        if ("DINING".equals(upper) || "FOOD".equals(upper) || "RESTAURANTS".equals(upper)) {
            return "RESTAURANT";
        }
        return upper;
    }

    private boolean isUserAdmin(Long userId) {
        if (userId == null) return false;
        return userRepository.findById(userId)
                .map(u -> u.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole() != null && "ADMIN".equalsIgnoreCase(ur.getRole().getName())))
                .orElse(false);
    }

    @Override
    public Set<String> getContractedBusinesses(Long userId) {
        if (userId == null) return Collections.emptySet();
        if (isUserAdmin(userId)) {
            return Set.of("HOTEL", "RESTAURANT", "TOUR");
        }
        Optional<BusinesssOwnerProfiles> profileOpt = businessOwnerProfileRepository.findByUsersId(userId);
        if (profileOpt.isEmpty()) {
            return Collections.emptySet();
        }
        BusinesssOwnerProfiles profile = profileOpt.get();
        if (!profile.isActive()) {
            return Collections.emptySet();
        }
        Set<String> raw = profile.getContractedBusinessTypes();
        if (raw == null || raw.isEmpty()) {
            return Collections.emptySet();
        }
        return raw.stream()
                .map(OwnerAccessControlServiceImpl::normalizeType)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isOwnerActive(Long userId) {
        if (userId == null) return false;
        if (isUserAdmin(userId)) return true;

        boolean isUserOnline = userRepository.findById(userId)
                .map(u -> u.getStatus() == null || !"SUSPENDED".equalsIgnoreCase(u.getStatus().name()))
                .orElse(false);
        if (!isUserOnline) return false;

        return businessOwnerProfileRepository.findByUsersId(userId)
                .map(BusinesssOwnerProfiles::isActive)
                .orElse(false);
    }

    @Override
    public boolean hasBusinessAccess(Long userId, String businessType) {
        if (userId == null || businessType == null) return false;
        if (isUserAdmin(userId)) return true;
        String normalized = normalizeType(businessType);
        return getContractedBusinesses(userId).contains(normalized);
    }

    @Override
    public void requireActiveOwner(Long userId) {
        if (userId == null) {
            throw new AccessDeniedException("Authentication required: owner ID is missing");
        }
        if (isUserAdmin(userId)) {
            return;
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + userId));
        if (user.getStatus() != null && "SUSPENDED".equalsIgnoreCase(user.getStatus().name())) {
            throw new AccessDeniedException("User account is suspended. Please contact your system administrator.");
        }

        Optional<BusinesssOwnerProfiles> profileOpt = businessOwnerProfileRepository.findByUsersId(userId);
        if (profileOpt.isEmpty()) {
            throw new AccessDeniedException("No business owner profile found for user: " + userId);
        }
        BusinesssOwnerProfiles profile = profileOpt.get();
        if (!profile.isActive()) {
            throw new AccessDeniedException("Owner account is suspended or inactive. Please contact your system administrator.");
        }
    }

    @Override
    public void requireBusinessAccess(Long userId, String businessType) {
        requireActiveOwner(userId);
        if (isUserAdmin(userId)) {
            return;
        }
        String normalized = normalizeType(businessType);
        if (!hasBusinessAccess(userId, normalized)) {
            throw new AccessDeniedException("Access denied: your contract does not include " + normalized + " management.");
        }
    }

    @Override
    public List<OwnerManagedBusinessDTO> getManagedBusinesses(Long userId) {
        if (userId == null) return Collections.emptyList();
        List<OwnerManagedBusinessDTO> list = new ArrayList<>();

        // 1. Hotels
        try {
            List<Hotels> hotels = hotelRepository.findByOwnerId(userId);
            for (Hotels h : hotels) {
                list.add(OwnerManagedBusinessDTO.builder()
                        .id(h.getId())
                        .name(h.getHotelName())
                        .type("HOTEL")
                        .address(h.getLocation() != null ? (h.getLocation().getProvince() + ", " + h.getLocation().getDistrict()) : null)
                        .details(h.getPhoneContact())
                        .status("Active")
                        .build());
            }
        } catch (Exception e) {
            log.warn("Could not load owner hotels for user {}: {}", userId, e.getMessage());
        }

        // 2. Restaurants
        try {
            List<Restaurants> rests = restaurantRepository.findByOwnerId(userId);
            for (Restaurants r : rests) {
                list.add(OwnerManagedBusinessDTO.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .type("RESTAURANT")
                        .address(r.getTourPlaces() != null ? r.getTourPlaces().getAddress() : null)
                        .details(r.getDescription())
                        .status("Active")
                        .build());
            }
        } catch (Exception e) {
            log.warn("Could not load owner restaurants for user {}: {}", userId, e.getMessage());
        }

        // 3. Tour Places
        try {
            List<TourPlaces> tours = tourismPlaceRepository.findByUserId(userId);
            for (TourPlaces t : tours) {
                list.add(OwnerManagedBusinessDTO.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .type("TOUR")
                        .address(t.getAddress())
                        .details(t.getDescription())
                        .status(t.getStaus() != null ? t.getStaus() : "Active")
                        .build());
            }
        } catch (Exception e) {
            log.warn("Could not load owner tours for user {}: {}", userId, e.getMessage());
        }

        return list;
    }

    @Override
    public OwnerPermissionsDTO getOwnerPermissions(Long userId) {
        if (userId == null) {
            return OwnerPermissionsDTO.builder()
                    .ownerId(null)
                    .active(false)
                    .status("INACTIVE")
                    .contractedBusinessTypes(Collections.emptyList())
                    .canManageHotel(false)
                    .canManageRestaurant(false)
                    .canManageTour(false)
                    .managedBusinesses(Collections.emptyList())
                    .build();
        }

        boolean isAdmin = isUserAdmin(userId);
        Optional<BusinesssOwnerProfiles> profileOpt = businessOwnerProfileRepository.findByUsersId(userId);

        String businessName = profileOpt.map(BusinesssOwnerProfiles::getBusinessName)
                .orElseGet(() -> userRepository.findById(userId).map(Users::getFullname).orElse("Business Operations"));

        String status = isAdmin ? "ACTIVE" : profileOpt.map(BusinesssOwnerProfiles::getStatus).orElse("INACTIVE");
        boolean active = isAdmin || (profileOpt.isPresent() && profileOpt.get().isActive());

        Set<String> contracted = getContractedBusinesses(userId);
        List<String> contractedList = contracted.stream().map(String::toLowerCase).sorted().collect(Collectors.toList());

        boolean canHotel = isAdmin || contracted.contains("HOTEL");
        boolean canRest = isAdmin || contracted.contains("RESTAURANT");
        boolean canTour = isAdmin || contracted.contains("TOUR");

        List<OwnerManagedBusinessDTO> managed = getManagedBusinesses(userId);

        return OwnerPermissionsDTO.builder()
                .ownerId(userId)
                .businessName(businessName)
                .status(status)
                .active(active)
                .contractedBusinessTypes(contractedList)
                .canManageHotel(canHotel)
                .canManageRestaurant(canRest)
                .canManageTour(canTour)
                .managedBusinesses(managed)
                .build();
    }

    @Override
    @Transactional
    public void ensureOwnerProfileForUser(Users user) {
        if (user == null || user.getId() == null) return;
        Optional<BusinesssOwnerProfiles> profileOpt = businessOwnerProfileRepository.findByUsersId(user.getId());
        if (profileOpt.isEmpty()) {
            BusinesssOwnerProfiles profile = new BusinesssOwnerProfiles();
            profile.setUsers(user);
            profile.setBusinessName(user.getFullname() != null ? user.getFullname() + " Operations" : "Partner Operations");
            profile.setBusinessLicenseNo("LIC-" + System.currentTimeMillis() + "-" + user.getId());
            profile.setVerificationStatus("VERIFIED");
            profile.setVerifiedAt(java.time.LocalDate.now());
            profile.setStatus("ACTIVE");
            profile.setContractedBusinessTypes(new HashSet<>(Set.of("HOTEL")));
            businessOwnerProfileRepository.save(profile);
        } else {
            BusinesssOwnerProfiles profile = profileOpt.get();
            if (!"ACTIVE".equalsIgnoreCase(profile.getStatus())) {
                profile.setStatus("ACTIVE");
                businessOwnerProfileRepository.save(profile);
            }
        }
    }

    @Override
    @Transactional
    public void deactivateOwnerProfileForUser(Long userId) {
        if (userId == null) return;
        businessOwnerProfileRepository.findByUsersId(userId).ifPresent(profile -> {
            profile.setStatus("SUSPENDED");
            businessOwnerProfileRepository.save(profile);
        });
    }
}

