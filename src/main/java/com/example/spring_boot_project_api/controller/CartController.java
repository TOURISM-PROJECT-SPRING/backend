package com.example.spring_boot_project_api.controller;

import java.util.List;

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

import com.example.spring_boot_project_api.dto.request.CartItemRequest;
import com.example.spring_boot_project_api.dto.response.CartItemResponse;
import com.example.spring_boot_project_api.dto.response.CartResponse;
import com.example.spring_boot_project_api.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getOrCreate(@RequestParam Long userId,
                                                    @RequestParam Long restaurantId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userId, restaurantId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartResponse>> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.findCartsByUser(userId));
    }

    @GetMapping("/{cartId}/items")
    public ResponseEntity<List<CartItemResponse>> findItems(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.findCartItems(cartId));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long cartId,
                                                @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(cartId, request));
    }

    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(@PathVariable Long cartId,
                                                           @PathVariable Long itemId,
                                                           @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(cartId, itemId, quantity));
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long cartId,
                                                   @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(cartId, itemId));
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> clearCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.clearCart(cartId));
    }
}
