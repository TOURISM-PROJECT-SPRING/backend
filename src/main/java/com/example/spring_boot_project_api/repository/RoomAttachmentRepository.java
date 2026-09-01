package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.RoomAttachments;

@Repository
public interface RoomAttachmentRepository extends JpaRepository<RoomAttachments, Long> {

    List<RoomAttachments> findByRooms_Id(Long roomId);

    boolean existsByRooms_IdAndAttachments_Id(Long roomId, Long attachmentId);

    Optional<RoomAttachments> findByRooms_IdAndAttachments_Id(Long roomId, Long attachmentId);

    boolean existsByAttachments_Id(Long attachmentId);

    @Transactional
    void deleteByRooms_Id(Long roomId);
}
