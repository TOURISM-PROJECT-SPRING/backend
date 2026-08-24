package com.example.spring_boot_project_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.TourPackageStops;

@Repository
public interface TourPackageStopRepository extends JpaRepository<TourPackageStops, Long> {

    List<TourPackageStops> findByTourPackages_IdOrderByDayNumberAsc(Long tourPackageId);

    List<TourPackageStops> findByTourismPlaces_Id(Long tourismPlaceId);

    @Transactional
    void deleteByTourPackages_Id(Long tourPackageId);
}
