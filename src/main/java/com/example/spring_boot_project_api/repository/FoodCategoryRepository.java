package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.FoodCategories;

@Repository
public interface FoodCategoryRepository extends JpaRepository<FoodCategories, Long> {

    Optional<FoodCategories> findByName(String name);

    boolean existsByName(String name);

    List<FoodCategories> findByNameContainingIgnoreCase(String keyword);
}
