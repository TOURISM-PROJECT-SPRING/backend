package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.BusinesssOwnerProfiles;

@Repository
public interface BusinessOwnerProfileRepository extends JpaRepository<BusinesssOwnerProfiles, Long> {

    Optional<BusinesssOwnerProfiles> findByBusinessLicenseNo(String businessLicenseNo);

    Optional<BusinesssOwnerProfiles> findByUsersId(Long userId);

    boolean existsByBusinessLicenseNo(String businessLicenseNo);

    List<BusinesssOwnerProfiles> findByVerificationStatus(String verificationStatus);
}
