package com.example.spring_boot_project_api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.FoodOrders;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrders, Long> {

    List<FoodOrders> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<FoodOrders> findByRestuarantsId(Long restaurantId);

    List<FoodOrders> findByStatus(String status);

    List<FoodOrders> findByPickupTimeBetween(LocalDateTime start, LocalDateTime end);
}
