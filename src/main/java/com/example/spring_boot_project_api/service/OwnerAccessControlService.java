package com.example.spring_boot_project_api.service;

import java.util.List;
import java.util.Set;

import com.example.spring_boot_project_api.dto.response.OwnerManagedBusinessDTO;
import com.example.spring_boot_project_api.dto.response.OwnerPermissionsDTO;

public interface OwnerAccessControlService {

    /**
     * Normalized set of business verticals this owner is contracted to manage
     * (e.g. ["HOTEL", "RESTAURANT", "TOUR"]). Returns empty set if none or suspended.
     */
    Set<String> getContractedBusinesses(Long userId);

    /**
     * Whether this owner account is active (not suspended or deactivated).
     */
    boolean isOwnerActive(Long userId);

    /**
     * Whether this owner has contract permission for a specific business vertical.
     */
    boolean hasBusinessAccess(Long userId, String businessType);

    /**
     * Enforces that the owner exists and is active. Throws AccessDeniedException if suspended.
     */
    void requireActiveOwner(Long userId);

    /**
     * Enforces that the owner exists, is active, and is contracted for the given business vertical.
     * Throws AccessDeniedException if violated.
     */
    void requireBusinessAccess(Long userId, String businessType);

    /**
     * Returns all businesses (hotels, restaurants, tour places) currently owned/managed by this owner.
     */
    List<OwnerManagedBusinessDTO> getManagedBusinesses(Long userId);

    /**
     * Returns full permissions and status summary for owner console synchronization.
     */
    OwnerPermissionsDTO getOwnerPermissions(Long userId);

    /**
     * Ensures an Owner profile exists for the given user, creating a default one if necessary.
     */
    void ensureOwnerProfileForUser(com.example.spring_boot_project_api.model.Users user);

    /**
     * Deactivates or suspends the owner profile for the given user ID.
     */
    void deactivateOwnerProfileForUser(Long userId);
}

