package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.RoomTypes;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomTypes, Long> {

    Optional<RoomTypes> findByName(String name);

    boolean existsByName(String name);

    List<RoomTypes> findByCapacityGreaterThanEqual(Integer minCapacity);
}
