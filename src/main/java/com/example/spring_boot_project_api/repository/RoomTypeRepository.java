package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.RoomTypes;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomTypes, Long> {

    Optional<RoomTypes> findByRoomType(String roomType);

    boolean existsByRoomType(String roomType);

    List<RoomTypes> findByRoomTypeContainingIgnoreCase(String keyword);

    List<RoomTypes> findByCapacityGreaterThanEqual(Integer minCapacity);
}