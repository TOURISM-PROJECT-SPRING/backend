package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByProvinceAndDistrict(String province, String district);

    boolean existsByProvinceAndDistrict(String province, String district);

    List<Location> findByProvinceContainingIgnoreCase(String province);

    List<Location> findByDistrictContainingIgnoreCase(String district);

    List<Location> findByProvinceContainingIgnoreCaseOrDistrictContainingIgnoreCase(String province, String district);
}
