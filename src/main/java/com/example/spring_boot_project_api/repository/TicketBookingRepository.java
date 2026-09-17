package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.TicketBookings;

@Repository
public interface TicketBookingRepository extends JpaRepository<TicketBookings, Long> {

    Optional<TicketBookings> findByQrCode(String qrCode);

    List<TicketBookings> findByUserIdOrderByVisiDateDesc(Long userId);

    List<TicketBookings> findByStatus(String status);

    List<TicketBookings> findByTicketsId(Long ticketId);

    List<TicketBookings> findByVisiDate(LocalDate visitDate);

    long countByTicketsIdAndVisiDate(Long ticketId, LocalDate visiDate);

    // --- Admin dashboard aggregates ---

    @Query("select coalesce(sum(tb.totalPrice), 0) from TicketBookings tb where lower(tb.status) <> 'cancelled'")
    BigDecimal sumNonCancelledRevenue();

    long countByStatusIgnoreCase(String status);

    List<TicketBookings> findByCreatedAtAfter(LocalDateTime since);
}
