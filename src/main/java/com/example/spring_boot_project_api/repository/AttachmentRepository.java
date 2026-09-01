package com.example.spring_boot_project_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Attachments;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachments, Long> {
}
