package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Provinces;

@Repository
public interface ProvinceRepository extends JpaRepository<Provinces, Long> {

    Optional<Provinces> findByName(String name);

    boolean existsByName(String name);

    List<Provinces> findByNameContainingIgnoreCase(String keyword);
}
