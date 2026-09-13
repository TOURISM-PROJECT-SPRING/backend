package com.example.spring_boot_project_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.ContactMessages;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessages, Long> {

    List<ContactMessages> findByOrderByCreatedAtDesc();

    List<ContactMessages> findByIsRead(Boolean isRead);
}