package com.example.spring_boot_project_api.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@Table(name = "tour_bookings")
public class TourBookings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numPeople;
    private LocalDate tourDate;
    private BigDecimal totalPrice;
    private String status;

    @Column(name = "payment_method", length = 70)
    private String paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_package_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TourPackages tourPackages;

    @OneToMany(mappedBy = "tourBookings", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Payments> payments = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // use for auto time(create and update)
    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
