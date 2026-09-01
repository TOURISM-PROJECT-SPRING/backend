package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.TourPlaceAttachments;

@Repository
public interface TourPlaceAttachmentRepository extends JpaRepository<TourPlaceAttachments, Long> {

    List<TourPlaceAttachments> findByTourPlaces_Id(Long tourPlaceId);

    List<TourPlaceAttachments> findByAttachments_Id(Long attachmentId);

    boolean existsByTourPlaces_IdAndAttachments_Id(Long tourPlaceId, Long attachmentId);

    Optional<TourPlaceAttachments> findByTourPlaces_IdAndAttachments_Id(Long tourPlaceId, Long attachmentId);

    boolean existsByAttachments_Id(Long attachmentId);

    @Transactional
    void deleteByTourPlaces_Id(Long tourPlaceId);
}
