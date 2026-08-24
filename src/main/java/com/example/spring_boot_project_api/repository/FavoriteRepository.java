package com.example.spring_boot_project_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.Favorites;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorites, Long> {

    List<Favorites> findByUsers_Id(Long userId);

    boolean existsByUsers_IdAndTourismPlaces_Id(Long userId, Long tourismPlaceId);

    boolean existsByUsers_IdAndHotels_Id(Long userId, Long hotelId);

    boolean existsByUsers_IdAndRestuarants_Id(Long userId, Long restaurantId);

    boolean existsByUsers_IdAndTourPackages_Id(Long userId, Long tourPackageId);

    @Transactional
    void deleteByUsers_Id(Long userId);
}
