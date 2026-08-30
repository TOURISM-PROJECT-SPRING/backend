package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.CartItems;

@Repository
public interface CartItemRepository extends JpaRepository<CartItems, Long> {

    List<CartItems> findByCartId(Long cartId);

    Optional<CartItems> findByCartIdAndFoodsId(Long cartId, Long foodId);

    void deleteByCartId(Long cartId);

    boolean existsByCartIdAndFoodsId(Long cartId, Long foodId);
}
