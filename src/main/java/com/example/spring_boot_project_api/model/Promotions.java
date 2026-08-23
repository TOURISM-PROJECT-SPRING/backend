package com.example.spring_boot_project_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "promotions")
public class Promotions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "discount_type", nullable = false, length = 100)
    private String discountType;

    @Column(name = "discount_value", nullable = false, length = 20)
    private BigDecimal discountValue;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "start_at", nullable = false, length = 70)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false, length = 70)
    private LocalDateTime endAt;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotal_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Hotels hotels;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restraurant_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Restaurants restraurants;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_package_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TourPackages tourPackages;

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
