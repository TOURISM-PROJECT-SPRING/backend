package com.example.spring_boot_project_api.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.FoodOrderRequest;
import com.example.spring_boot_project_api.dto.response.FoodOrderResponse;
import com.example.spring_boot_project_api.service.FoodOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/food-orders")
@RequiredArgsConstructor
public class FoodOrderController {

    private final FoodOrderService foodOrderService;

    @GetMapping
    public ResponseEntity<List<FoodOrderResponse>> findAll() {
        return ResponseEntity.ok(foodOrderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodOrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(foodOrderService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FoodOrderResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(foodOrderService.findByUserId(userId));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<FoodOrderResponse>> findByRestaurantId(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(foodOrderService.findByRestaurantId(restaurantId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FoodOrderResponse>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(foodOrderService.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<FoodOrderResponse> placeOrder(
            @Valid @RequestBody FoodOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodOrderService.placeOrder(request));
    }

    @PostMapping("/from-cart")
    public ResponseEntity<FoodOrderResponse> placeOrderFromCart(
            @RequestParam Long userId,
            @RequestParam Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime pickupTime) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                foodOrderService.placeOrderFromCart(userId, restaurantId, pickupTime));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<FoodOrderResponse> updateStatus(@PathVariable Long id,
                                                          @RequestParam String status) {
        return ResponseEntity.ok(foodOrderService.updateStatus(id, status));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<FoodOrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(foodOrderService.cancel(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
