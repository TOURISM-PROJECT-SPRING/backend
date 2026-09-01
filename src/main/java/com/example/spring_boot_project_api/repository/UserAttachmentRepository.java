package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.UserAttachments;

@Repository
public interface UserAttachmentRepository extends JpaRepository<UserAttachments, Long> {

    List<UserAttachments> findByUsers_Id(Long userId);

    boolean existsByUsers_IdAndAttachments_Id(Long userId, Long attachmentId);

    Optional<UserAttachments> findByUsers_IdAndAttachments_Id(Long userId, Long attachmentId);

    boolean existsByAttachments_Id(Long attachmentId);

    @Transactional
    void deleteByUsers_Id(Long userId);
}