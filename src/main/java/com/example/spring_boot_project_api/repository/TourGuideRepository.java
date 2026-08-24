package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.TourGuides;

@Repository
public interface TourGuideRepository extends JpaRepository<TourGuides, Long> {

    Optional<TourGuides> findByUserId(Long userId);

    List<TourGuides> findByLanguageSpokenContainingIgnoreCase(String language);

    List<TourGuides> findByExperienceYearGreaterThanEqual(Integer minYears);

    List<TourGuides> findByRatePerDayLessThanEqualOrderByRatePerDayAsc(BigDecimal maxRatePerDay);
}
