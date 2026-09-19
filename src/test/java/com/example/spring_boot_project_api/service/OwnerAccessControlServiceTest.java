package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.spring_boot_project_api.dto.response.OwnerPermissionsDTO;
import com.example.spring_boot_project_api.model.BusinesssOwnerProfiles;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.Roles;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.model.UserRoles;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.BusinessOwnerProfileRepository;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.impl.OwnerAccessControlServiceImpl;
import com.example.spring_boot_project_api.util.RoleNames;

@ExtendWith(MockitoExtension.class)
class OwnerAccessControlServiceTest {

    @Mock
    private BusinessOwnerProfileRepository businessOwnerProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private TourismPlaceRepository tourismPlaceRepository;

    @InjectMocks
    private OwnerAccessControlServiceImpl accessControlService;

    private Users testOwnerUser;
    private BusinesssOwnerProfiles testProfile;

    @BeforeEach
    void setUp() {
        testOwnerUser = new Users();
        testOwnerUser.setId(10L);
        testOwnerUser.setUsername("test_owner");
        testOwnerUser.setEmail("owner@test.com");

        Roles ownerRole = new Roles();
        ownerRole.setName(RoleNames.OWNER);

        UserRoles ur = new UserRoles();
        ur.setRole(ownerRole);
        ur.setUser(testOwnerUser);
        testOwnerUser.setUserRoles(List.of(ur));

        testProfile = new BusinesssOwnerProfiles();
        testProfile.setId(100L);
        testProfile.setUsers(testOwnerUser);
        testProfile.setBusinessName("Test Enterprises");
        testProfile.setStatus("ACTIVE");
        testProfile.setContractedBusinessTypes(new HashSet<>());
    }

    @Test
    @DisplayName("Case 1: 1 business contract (Hotel only) allows Hotel and blocks Restaurant & Tour")
    void testCase1_OneBusinessContract_HotelOnly() {
        testProfile.setContractedBusinessTypes(new HashSet<>(Set.of("HOTEL")));
        when(businessOwnerProfileRepository.findByUsersId(10L)).thenReturn(Optional.of(testProfile));
        when(userRepository.findById(10L)).thenReturn(Optional.of(testOwnerUser));

        // Allowed
        assertThat(accessControlService.hasBusinessAccess(10L, "HOTEL")).isTrue();
        assertThat(accessControlService.hasBusinessAccess(10L, "ROOM")).isTrue();
        assertThatCode(() -> accessControlService.requireBusinessAccess(10L, "HOTEL"))
                .doesNotThrowAnyException();

        // Blocked
        assertThat(accessControlService.hasBusinessAccess(10L, "RESTAURANT")).isFalse();
        assertThat(accessControlService.hasBusinessAccess(10L, "FOOD")).isFalse();
        assertThat(accessControlService.hasBusinessAccess(10L, "TOUR")).isFalse();

        assertThatThrownBy(() -> accessControlService.requireBusinessAccess(10L, "RESTAURANT"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("RESTAURANT");

        assertThatThrownBy(() -> accessControlService.requireBusinessAccess(10L, "TOUR"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("TOUR");
    }

    @Test
    @DisplayName("Case 2: 2 businesses contract (Hotel + Restaurant) allows both and blocks Tour")
    void testCase2_TwoBusinessesContract_HotelAndRestaurant() {
        testProfile.setContractedBusinessTypes(new HashSet<>(Set.of("HOTEL", "RESTAURANT")));
        when(businessOwnerProfileRepository.findByUsersId(10L)).thenReturn(Optional.of(testProfile));
        when(userRepository.findById(10L)).thenReturn(Optional.of(testOwnerUser));

        // Allowed
        assertThat(accessControlService.hasBusinessAccess(10L, "HOTEL")).isTrue();
        assertThat(accessControlService.hasBusinessAccess(10L, "RESTAURANT")).isTrue();
        assertThat(accessControlService.hasBusinessAccess(10L, "FOOD")).isTrue();
        assertThatCode(() -> accessControlService.requireBusinessAccess(10L, "HOTEL"))
                .doesNotThrowAnyException();
        assertThatCode(() -> accessControlService.requireBusinessAccess(10L, "FOOD"))
                .doesNotThrowAnyException();

        // Blocked
        assertThat(accessControlService.hasBusinessAccess(10L, "TOUR")).isFalse();
        assertThatThrownBy(() -> accessControlService.requireBusinessAccess(10L, "TOUR"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Case 3: 3 businesses contract (Hotel + Restaurant + Tour) allows all three verticals")
    void testCase3_ThreeBusinessesContract_AllThree() {
        testProfile.setContractedBusinessTypes(new HashSet<>(Set.of("HOTEL", "RESTAURANT", "TOUR")));
        when(businessOwnerProfileRepository.findByUsersId(10L)).thenReturn(Optional.of(testProfile));
        when(userRepository.findById(10L)).thenReturn(Optional.of(testOwnerUser));

        assertThat(accessControlService.hasBusinessAccess(10L, "HOTEL")).isTrue();
        assertThat(accessControlService.hasBusinessAccess(10L, "RESTAURANT")).isTrue();
        assertThat(accessControlService.hasBusinessAccess(10L, "TOUR")).isTrue();

        assertThatCode(() -> accessControlService.requireBusinessAccess(10L, "HOTEL"))
                .doesNotThrowAnyException();
        assertThatCode(() -> accessControlService.requireBusinessAccess(10L, "RESTAURANT"))
                .doesNotThrowAnyException();
        assertThatCode(() -> accessControlService.requireBusinessAccess(10L, "TOUR"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Case 4: Contract modification while active immediately updates access without re-login")
    void testCase4_ContractModifiedWhileActive() {
        // Initial state: HOTEL only
        testProfile.setContractedBusinessTypes(new HashSet<>(Set.of("HOTEL")));
        when(businessOwnerProfileRepository.findByUsersId(10L)).thenReturn(Optional.of(testProfile));
        when(userRepository.findById(10L)).thenReturn(Optional.of(testOwnerUser));

        assertThat(accessControlService.hasBusinessAccess(10L, "TOUR")).isFalse();

        // Admin updates contract in DB: adds TOUR
        testProfile.getContractedBusinessTypes().add("TOUR");

        assertThat(accessControlService.hasBusinessAccess(10L, "TOUR")).isTrue();
        assertThat(accessControlService.getContractedBusinesses(10L)).contains("HOTEL", "TOUR");

        // Admin revokes HOTEL
        testProfile.getContractedBusinessTypes().remove("HOTEL");

        assertThat(accessControlService.hasBusinessAccess(10L, "HOTEL")).isFalse();
        assertThatThrownBy(() -> accessControlService.requireBusinessAccess(10L, "HOTEL"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Case 5: All access revoked / Account suspended immediately blocks all operations")
    void testCase5_AccountSuspended_BlocksAllAccess() {
        testProfile.setContractedBusinessTypes(new HashSet<>(Set.of("HOTEL", "RESTAURANT", "TOUR")));
        testProfile.setStatus("SUSPENDED");
        when(businessOwnerProfileRepository.findByUsersId(10L)).thenReturn(Optional.of(testProfile));
        when(userRepository.findById(10L)).thenReturn(Optional.of(testOwnerUser));

        assertThat(accessControlService.isOwnerActive(10L)).isFalse();
        assertThat(accessControlService.hasBusinessAccess(10L, "HOTEL")).isFalse();
        assertThat(accessControlService.hasBusinessAccess(10L, "RESTAURANT")).isFalse();
        assertThat(accessControlService.hasBusinessAccess(10L, "TOUR")).isFalse();

        assertThatThrownBy(() -> accessControlService.requireBusinessAccess(10L, "HOTEL"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("suspended");

        // Contracted businesses list should be empty when suspended
        assertThat(accessControlService.getContractedBusinesses(10L)).isEmpty();
    }

    @Test
    @DisplayName("Admin bypass: Super Admin has full permissions across all business verticals")
    void testAdminBypass_AdminHasUniversalAccess() {
        Users adminUser = new Users();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        Roles adminRole = new Roles();
        adminRole.setName(RoleNames.ADMIN);
        UserRoles ur = new UserRoles();
        ur.setRole(adminRole);
        ur.setUser(adminUser);
        adminUser.setUserRoles(List.of(ur));

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThat(accessControlService.isOwnerActive(1L)).isTrue();
        assertThat(accessControlService.hasBusinessAccess(1L, "HOTEL")).isTrue();
        assertThat(accessControlService.hasBusinessAccess(1L, "RESTAURANT")).isTrue();
        assertThat(accessControlService.hasBusinessAccess(1L, "TOUR")).isTrue();
        assertThat(accessControlService.getContractedBusinesses(1L)).containsExactlyInAnyOrder("HOTEL", "RESTAURANT", "TOUR");
        assertThatCode(() -> accessControlService.requireBusinessAccess(1L, "HOTEL")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Permissions DTO aggregates status, business types, and owned entity counts")
    void testOwnerPermissionsDTO_Aggregation() {
        testProfile.setContractedBusinessTypes(new HashSet<>(Set.of("HOTEL", "RESTAURANT")));
        testProfile.setStatus("ACTIVE");

        when(businessOwnerProfileRepository.findByUsersId(10L)).thenReturn(Optional.of(testProfile));
        when(userRepository.findById(10L)).thenReturn(Optional.of(testOwnerUser));

        Hotels h = new Hotels();
        h.setId(101L);
        when(hotelRepository.findByOwnerId(10L)).thenReturn(List.of(h));

        Restaurants r = new Restaurants();
        r.setId(201L);
        when(restaurantRepository.findByOwnerId(10L)).thenReturn(List.of(r));

        when(tourismPlaceRepository.findByUserId(10L)).thenReturn(Collections.emptyList());

        OwnerPermissionsDTO permissions = accessControlService.getOwnerPermissions(10L);

        assertThat(permissions.getOwnerId()).isEqualTo(10L);
        assertThat(permissions.getStatus()).isEqualTo("ACTIVE");
        assertThat(permissions.getContractedBusinessTypes()).containsExactlyInAnyOrder("hotel", "restaurant");
        assertThat(permissions.getManagedBusinesses()).hasSize(2);
        assertThat(permissions.isCanManageHotel()).isTrue();
        assertThat(permissions.isCanManageRestaurant()).isTrue();
        assertThat(permissions.isCanManageTour()).isFalse();
    }
}
