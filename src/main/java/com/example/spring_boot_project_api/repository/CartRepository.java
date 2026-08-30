package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Carts;

@Repository
public interface CartRepository extends JpaRepository<Carts, Long> {

    Optional<Carts> findByUserIdAndRestaurantsId(Long userId, Long restaurantId);

    List<Carts> findByUserId(Long userId);

    boolean existsByUserIdAndRestaurantsId(Long userId, Long restaurantId);
}
