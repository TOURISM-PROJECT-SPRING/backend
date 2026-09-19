package com.example.spring_boot_project_api.repository;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Restaurants;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurants, Long> {

    List<Restaurants> findByNameContainingIgnoreCase(String keyword);

    List<Restaurants> findByTourPlacesId(Long tourismPlaceId);

    List<Restaurants> findByOwnerId(Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByName(String name);

    List<Restaurants> findByOpenTimeBeforeAndClossTimeAfter(LocalTime time, LocalTime sameTime);
}
