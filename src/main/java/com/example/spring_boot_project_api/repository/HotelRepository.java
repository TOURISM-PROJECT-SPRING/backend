package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Hotels;

@Repository
public interface HotelRepository extends JpaRepository<Hotels, Long> {

    Optional<Hotels> findByHotelName(String hotelName);

    boolean existsByHotelName(String hotelName);

    List<Hotels> findByHotelNameContainingIgnoreCase(String keyword);

    List<Hotels> findByLocationId(Long districtId);

    List<Hotels> findByOwnerId(Long ownerId);
}