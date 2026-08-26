package com.example.spring_boot_project_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.Notifications;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Long> {

    List<Notifications> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Notifications> findByUser_IdAndIsReadFalse(Long userId);

    long countByUser_IdAndIsReadFalse(Long userId);

    List<Notifications> findByType(String type);

    @Transactional
    void deleteByUser_Id(Long userId);
}
