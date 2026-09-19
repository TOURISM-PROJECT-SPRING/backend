package com.example.spring_boot_project_api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@Table(name = "business_owner_profiles")
public class BusinesssOwnerProfiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "business_license_no", nullable = false)
    private String businessLicenseNo;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus;

    @Column(name = "verified_at")
    private LocalDate verifiedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "owner_contracted_businesses", joinColumns = @JoinColumn(name = "owner_profile_id"))
    @Column(name = "business_type", length = 50)
    private Set<String> contractedBusinessTypes = new HashSet<>();

    @Column(name = "status", length = 30)
    private String status = "ACTIVE";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Users users;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean hasBusinessAccess(String type) {
        if (type == null) return false;
        String normalized = type.trim().toUpperCase();
        if ("TOURIST".equals(normalized)) normalized = "TOUR";
        return contractedBusinessTypes.contains(normalized);
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    @PrePersist
    void onCreate() {
        if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        }
        if (this.contractedBusinessTypes == null) {
            this.contractedBusinessTypes = new HashSet<>();
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        }
        updatedAt = LocalDateTime.now();
    }
}
