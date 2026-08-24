package com.example.spring_boot_project_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.PlaceImages;

@Repository
public interface PlaceImageRepository extends JpaRepository<PlaceImages, Long> {

    List<PlaceImages> findByTourismPlace_Id(Long tourismPlaceId);

    List<PlaceImages> findByTourismPlace_IdAndIsPrimaryTrue(Long tourismPlaceId);

    List<PlaceImages> findByIsPrimaryTrue();

    @Transactional
    void deleteByTourismPlace_Id(Long tourismPlaceId);
}
