package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.RestaurantAttachments;

@Repository
public interface RestaurantAttachmentRepository extends JpaRepository<RestaurantAttachments, Long> {

    List<RestaurantAttachments> findByRestaurants_Id(Long restaurantId);

    boolean existsByRestaurants_IdAndAttachments_Id(Long restaurantId, Long attachmentId);

    Optional<RestaurantAttachments> findByRestaurants_IdAndAttachments_Id(Long restaurantId, Long attachmentId);

    boolean existsByAttachments_Id(Long attachmentId);

    @Transactional
    void deleteByRestaurants_Id(Long restaurantId);
}