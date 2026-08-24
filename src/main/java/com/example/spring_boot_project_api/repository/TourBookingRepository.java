package com.example.spring_boot_project_api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.TourBookings;

@Repository
public interface TourBookingRepository extends JpaRepository<TourBookings, Long> {

    List<TourBookings> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<TourBookings> findByTourPackages_Id(Long tourPackageId);

    List<TourBookings> findByStatus(String status);

    List<TourBookings> findByTourDateBetween(LocalDate start, LocalDate end);

    List<TourBookings> findByUserIdAndTourPackages_Id(Long userId, Long tourPackageId);
}
