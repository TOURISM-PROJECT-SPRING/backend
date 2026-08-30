package com.example.spring_boot_project_api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "attachments")
public class Attachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private String path;

    private String mime_type; // ប្រភេទไฟล์ ឧ. image/jpeg, application/pdf

    private Long size; // ទំហំไฟล์ជា Bytes

    private LocalDateTime createdAt; // ពេលវេលាបង្កើតไฟล์

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(); // កំណត់ម៉ោងស្វ័យប្រវត្តិពេល Save
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
