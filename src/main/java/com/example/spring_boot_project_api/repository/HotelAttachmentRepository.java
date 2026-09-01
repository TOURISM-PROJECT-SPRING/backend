package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.HotelAttachments;

@Repository
public interface HotelAttachmentRepository extends JpaRepository<HotelAttachments, Long> {

    List<HotelAttachments> findByHotels_Id(Long hotelId);

    boolean existsByHotels_IdAndAttachments_Id(Long hotelId, Long attachmentId);

    Optional<HotelAttachments> findByHotels_IdAndAttachments_Id(Long hotelId, Long attachmentId);

    boolean existsByAttachments_Id(Long attachmentId);

    @Transactional
    void deleteByHotels_Id(Long hotelId);
}
