package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.FoodAttachments;

@Repository
public interface FoodAttachmentRepository extends JpaRepository<FoodAttachments, Long> {

    List<FoodAttachments> findByFoods_Id(Long foodId);

    boolean existsByFoods_IdAndAttachments_Id(Long foodId, Long attachmentId);

    Optional<FoodAttachments> findByFoods_IdAndAttachments_Id(Long foodId, Long attachmentId);

    boolean existsByAttachments_Id(Long attachmentId);

    @Transactional
    void deleteByFoods_Id(Long foodId);
}
