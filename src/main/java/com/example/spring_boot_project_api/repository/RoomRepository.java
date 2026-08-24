package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Rooms;

@Repository
public interface RoomRepository extends JpaRepository<Rooms, Long> {

    List<Rooms> findByHotelsId(Long hotelId);

    List<Rooms> findByHotelsIdOrderByPricePerNightAsc(Long hotelId);

    List<Rooms> findByHotelsIdAndStatus(Long hotelId, String status);

    List<Rooms> findByRoomTypesId(Long roomTypeId);

    List<Rooms> findByPricePerNightBetween(BigDecimal minPrice, BigDecimal maxPrice);

    boolean existsByHotelsIdAndRoomNumber(Long hotelId, String roomNumber);
}
