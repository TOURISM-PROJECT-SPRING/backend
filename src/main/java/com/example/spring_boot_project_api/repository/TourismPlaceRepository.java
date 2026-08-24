package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.TourismPlaces;

@Repository
public interface TourismPlaceRepository extends JpaRepository<TourismPlaces, Long> {

    List<TourismPlaces> findByNameContainingIgnoreCase(String keyword);

    List<TourismPlaces> findByDistrictId(Long districtId);

    List<TourismPlaces> findByPlaceCategotyId(Long placeCategoryId);

    List<TourismPlaces> findByUserId(Long userId);

    List<TourismPlaces> findByStaus(String staus);

    List<TourismPlaces> findByRatingGreaterThanEqualOrderByRatingDesc(BigDecimal minRating);

    List<TourismPlaces> findByDistrictIdAndPlaceCategotyId(Long districtId, Long placeCategoryId);

    long countByDistrictId(Long districtId);

    long countByPlaceCategotyId(Long placeCategoryId);
}
