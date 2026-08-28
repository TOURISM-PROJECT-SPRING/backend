package com.example.spring_boot_project_api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.RoomBookings;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBookings, Long> {

    List<RoomBookings> findByUsersIdOrderByCheckInDesc(Long userId);

    List<RoomBookings> findByRoomsId(Long roomId);

    List<RoomBookings> findByStatus(String status);

    List<RoomBookings> findByUsersIdAndStatus(Long userId, String status);

    // bookings of a room overlapping [checkIn, checkOut] — for availability checks
    List<RoomBookings> findByRooms_IdAndCheckInLessThanEqualAndCheckOutGreaterThanEqual(
            Long roomId, LocalDate checkIn, LocalDate checkOut);
}