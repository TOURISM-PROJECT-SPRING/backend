package com.example.spring_boot_project_api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.spring_boot_project_api.enums.GenderEnum;
import com.example.spring_boot_project_api.enums.UserEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class Users {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fullname", nullable = false, length = 200)
    private String fullname;

    @Column(name = "username", nullable = false, unique = true, length = 200)
    private String username;

    @Column(name = "email",nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender",nullable = false, length = 30)
    private GenderEnum gender = GenderEnum.Male;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserEnum status = UserEnum.Online;

    @Column(name = "profile", length = 255)
    private String profile;

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
