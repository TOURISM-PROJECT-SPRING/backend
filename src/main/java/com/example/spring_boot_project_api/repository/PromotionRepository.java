package com.example.spring_boot_project_api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Promotions;

@Repository
public interface PromotionRepository extends JpaRepository<Promotions, Long> {

    Optional<Promotions> findByCode(String code);

    boolean existsByCode(String code);

    List<Promotions> findByStatus(String status);

    List<Promotions> findByStartAtBeforeAndEndAtAfter(LocalDateTime now, LocalDateTime sameNow);

    List<Promotions> findByHotelsId(Long hotelId);

    List<Promotions> findByRestraurantsId(Long restaurantId);

    List<Promotions> findByTourPackagesId(Long tourPackageId);
}
