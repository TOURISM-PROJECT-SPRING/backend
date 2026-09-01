package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.TourPlaces;

@Repository
public interface TourismPlaceRepository extends JpaRepository<TourPlaces, Long> {

    List<TourPlaces> findByNameContainingIgnoreCase(String keyword);

    List<TourPlaces> findByDistrictId(Long districtId);

    List<TourPlaces> findByPlaceCategotyId(Long placeCategoryId);

    List<TourPlaces> findByUserId(Long userId);

    List<TourPlaces> findByStaus(String staus);

    List<TourPlaces> findByRatingGreaterThanEqualOrderByRatingDesc(BigDecimal minRating);

    List<TourPlaces> findByDistrictIdAndPlaceCategotyId(Long districtId, Long placeCategoryId);

    long countByDistrictId(Long districtId);

    long countByPlaceCategotyId(Long placeCategoryId);
}
