package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.TourBookings;

@Repository
public interface TourBookingRepository extends JpaRepository<TourBookings, Long> {

    List<TourBookings> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<TourBookings> findByTourPackages_Id(Long tourPackageId);

    List<TourBookings> findByStatus(String status);

    List<TourBookings> findByTourDateBetween(LocalDate start, LocalDate end);

    List<TourBookings> findByUserIdAndTourPackages_Id(Long userId, Long tourPackageId);

    // --- Admin dashboard aggregates ---

    @Query("select coalesce(sum(tb.totalPrice), 0) from TourBookings tb where lower(tb.status) <> 'cancelled'")
    BigDecimal sumNonCancelledRevenue();

    long countByStatusIgnoreCase(String status);

    List<TourBookings> findByCreatedAtAfter(LocalDateTime since);
}
