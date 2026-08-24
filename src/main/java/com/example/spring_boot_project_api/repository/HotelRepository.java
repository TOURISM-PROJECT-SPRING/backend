package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Hotels;

@Repository
public interface HotelRepository extends JpaRepository<Hotels, Long> {

    Optional<Hotels> findByName(String name);

    boolean existsByName(String name);

    List<Hotels> findByNameContainingIgnoreCase(String keyword);

    List<Hotels> findByTourismPlacesId(Long tourismPlaceId);

    List<Hotels> findByStarRatingGreaterThanEqualOrderByStarRatingDesc(BigDecimal minStarRating);
}
