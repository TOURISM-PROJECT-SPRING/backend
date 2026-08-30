package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.FoodOrderRequest;
import com.example.spring_boot_project_api.dto.response.FoodOrderResponse;

public interface FoodOrderService {

    List<FoodOrderResponse> findAll();

    FoodOrderResponse findById(Long id);

    List<FoodOrderResponse> findByUserId(Long userId);

    List<FoodOrderResponse> findByRestaurantId(Long restaurantId);

    List<FoodOrderResponse> findByStatus(String status);

    FoodOrderResponse placeOrder(FoodOrderRequest request);

    FoodOrderResponse placeOrderFromCart(Long userId, Long restaurantId,
                                         java.time.LocalDateTime pickupTime);

    FoodOrderResponse updateStatus(Long id, String status);

    FoodOrderResponse cancel(Long id);

    void delete(Long id);
}
