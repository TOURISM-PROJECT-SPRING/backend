package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.HotelRooms;

@Repository
public interface HotelRoomRepository extends JpaRepository<HotelRooms, Long> {

    List<HotelRooms> findByHotelsId(Long hotelId);

    List<HotelRooms> findByRoomTypesId(Long roomTypeId);

    Optional<HotelRooms> findByHotelsIdAndRoomTypesId(Long hotelId, Long roomTypeId);

    boolean existsByHotelsIdAndRoomTypesId(Long hotelId, Long roomTypeId);

    List<HotelRooms> findByPricePerNightBetween(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);

    List<HotelRooms> findByCapacityGreaterThanEqual(Integer minCapacity);
}