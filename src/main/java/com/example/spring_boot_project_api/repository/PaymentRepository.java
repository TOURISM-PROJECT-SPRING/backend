package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Payments;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, Long> {

    List<Payments> findByTransactionId(String transactionId);

    List<Payments> findByStatus(String status);

    List<Payments> findByPaymentMethod(String paymentMethod);

    List<Payments> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    List<Payments> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);

    List<Payments> findByRoomBookings_Id(Long roomBookingId);

    List<Payments> findByTicketBookings_Id(Long ticketBookingId);

    List<Payments> findByFoodOrders_Id(Long foodOrderId);

    List<Payments> findByTourBookings_Id(Long tourBookingId);
}
