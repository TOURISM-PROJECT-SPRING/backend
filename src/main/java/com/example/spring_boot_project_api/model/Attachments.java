package com.example.spring_boot_project_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

import com.example.spring_boot_project_api.enums.AttachmentFileType;

@Entity
@Data
@Table(name = "attachments")
public class Attachments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Users uploadedBy;

    /** The original name of the uploaded file, e.g. {@code photo_2024.jpg}. */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /** Publicly reachable Cloudinary URL. The image binary is NOT stored here. */
    @Column(name = "cloudinary_url", nullable = false, length = 1000)
    private String cloudinaryUrl;

    /**
     * Cloudinary public id, e.g.
     * {@code smart-tourism/hotels/10/550e8400-e29b-41d4-a716-446655440000}.
     * Required to delete the image from Cloudinary later.
     */
    @Column(name = "cloudinary_public_id", nullable = false, length = 500)
    private String cloudinaryPublicId;

    /** Cloudinary resource type: {@code image} (default) or {@code raw} for documents. */
    @Column(name = "cloudinary_resource_type", length = 50)
    private String cloudinaryResourceType = "image";

    /** Coarse classification, e.g. IMAGE or DOCUMENT. */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 50)
    private AttachmentFileType fileType;

    /** MIME type, e.g. image/jpeg. */
    @Column(name = "mime_type", length = 255)
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
