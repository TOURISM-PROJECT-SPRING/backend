package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.CartItemRequest;
import com.example.spring_boot_project_api.dto.response.CartItemResponse;
import com.example.spring_boot_project_api.dto.response.CartResponse;

public interface CartService {

    CartResponse getOrCreateCart(Long userId, Long restaurantId);

    List<CartResponse> findCartsByUser(Long userId);

    List<CartItemResponse> findCartItems(Long cartId);

    CartResponse addItem(Long cartId, CartItemRequest request);

    CartResponse updateItemQuantity(Long cartId, Long itemId, Integer quantity);

    CartResponse removeItem(Long cartId, Long itemId);

    CartResponse clearCart(Long cartId);
}
