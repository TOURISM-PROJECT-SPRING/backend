package com.example.spring_boot_project_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.spring_boot_project_api.enums.PaymentMethod;
import com.example.spring_boot_project_api.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payment_reference", columnList = "payment_reference"),
        @Index(name = "idx_payment_transaction", columnList = "transaction_id"),
        @Index(name = "idx_payment_qr_md5", columnList = "qr_md5"),
        @Index(name = "idx_payment_status", columnList = "status")
    }
)
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 50)
    private String paymentReference;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "qr_md5", length = 100)
    private String qrMd5;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_booking_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RoomBookings roomBookings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_booking_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TicketBookings ticketBookings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_order_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FoodOrders foodOrders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_booking_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TourBookings tourBookings;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}