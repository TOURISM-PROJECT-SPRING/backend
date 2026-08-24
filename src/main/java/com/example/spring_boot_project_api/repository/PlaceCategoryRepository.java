package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.PlaceCategoties;

@Repository
public interface PlaceCategoryRepository extends JpaRepository<PlaceCategoties, Long> {

    Optional<PlaceCategoties> findByName(String name);

    boolean existsByName(String name);

    List<PlaceCategoties> findByNameContainingIgnoreCase(String keyword);
}
