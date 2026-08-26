package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Districts;

@Repository
public interface DistrictRepository extends JpaRepository<Districts, Long> {

    Optional<Districts> findByName(String name);

    List<Districts> findByProvincesIdOrderByNameAsc(Long provinceId);

    boolean existsByProvincesIdAndNameIgnoreCase(Long provinceId, String name);

    List<Districts> findByNameContainingIgnoreCase(String keyword);
}
