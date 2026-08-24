package com.example.spring_boot_project_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.FoodOrderItems;

@Repository
public interface FoodOrderItemRepository extends JpaRepository<FoodOrderItems, Long> {

    List<FoodOrderItems> findByFoodOrdersId(Long foodOrderId);

    List<FoodOrderItems> findByFoodsId(Long foodId);

    long countByFoodsId(Long foodId);
}
