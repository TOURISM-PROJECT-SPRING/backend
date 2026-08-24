package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Foods;

@Repository
public interface FoodRepository extends JpaRepository<Foods, Long> {

    List<Foods> findByNameContainingIgnoreCase(String keyword);

    List<Foods> findByRestaurantsId(Long restaurantId);

    List<Foods> findByRestaurantsIdAndIsAvailableTrue(Long restaurantId);

    List<Foods> findByIsAvailableTrue();

    List<Foods> findByFoodCategoriesId(Long foodCategoryId);

    List<Foods> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
