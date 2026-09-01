package com.example.spring_boot_project_api.model;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@Table(name = "hotel_attachments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hotel_id", "attachments_id"}))
public class HotelAttachments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Hotels hotels;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attachments_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Attachments attachments;

    /** Semantic role, e.g. COVER, GALLERY, DOCUMENT, LOGO. */
    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
