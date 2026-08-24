package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.TourPackages;

@Repository
public interface TourPackageRepository extends JpaRepository<TourPackages, Long> {

    List<TourPackages> findByNameContainingIgnoreCase(String keyword);

    List<TourPackages> findByProvincesId(Long provinceId);

    List<TourPackages> findByToureGuidesId(Long tourGuideId);

    List<TourPackages> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<TourPackages> findByDurationDaysLessThanEqual(Integer maxDays);

    List<TourPackages> findByMaxPeopleGreaterThanEqual(Integer minPeople);
}
