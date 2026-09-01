package com.example.spring_boot_project_api.mapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.response.CartItemResponse;
import com.example.spring_boot_project_api.dto.response.CartResponse;
import com.example.spring_boot_project_api.model.CartItems;
import com.example.spring_boot_project_api.model.Carts;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.Users;

public class CartMapper {

    private CartMapper() {}

    public static Carts toEntity(Users user, Restaurants restaurant) {
        Carts cart = new Carts();
        cart.setUser(user);
        cart.setRestaurants(restaurant);
        return cart;
    }

    public static CartItems toCartItem(Carts cart, Foods food, Integer quantity) {
        CartItems item = new CartItems();
        item.setCart(cart);
        item.setFoods(food);
        item.setQuantity(quantity);
        BigDecimal unitPrice = food.getPrice();
        item.setUnitPrice(unitPrice);
        item.setSubTotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    public static BigDecimal computeTotal(Carts cart) {
        if (cart.getCartItems() == null) return BigDecimal.ZERO;
        return cart.getCartItems().stream()
                .map(CartItems::getSubTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static CartItemResponse toItemResponse(CartItems item) {
        if (item == null) return null;
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setSubTotal(item.getSubTotal());
        if (item.getFoods() != null) {
            response.setFoodId(item.getFoods().getId());
            response.setFoodName(item.getFoods().getName());
            response.setFoodImage(item.getFoods().getImage());
        }
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }

    public static CartResponse toResponse(Carts cart) {
        if (cart == null) return null;
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        if (cart.getUser() != null) {
            response.setUserId(cart.getUser().getId());
            response.setUserName(cart.getUser().getFullname());
        }
        if (cart.getRestaurants() != null) {
            response.setRestaurantId(cart.getRestaurants().getId());
            response.setRestaurantName(cart.getRestaurants().getName());
        }
        List<CartItemResponse> items = cart.getCartItems() == null
                ? Collections.emptyList()
                : cart.getCartItems().stream()
                        .map(CartMapper::toItemResponse)
                        .collect(Collectors.toList());
        response.setItems(items);
        response.setTotalPrice(items.stream()
                .map(CartItemResponse::getSubTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        return response;
    }

    public static List<CartResponse> toResponseList(List<Carts> carts) {
        if (carts == null) return Collections.emptyList();
        return carts.stream()
                .map(CartMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static List<CartItemResponse> toItemResponseList(List<CartItems> items) {
        if (items == null) return Collections.emptyList();
        return items.stream()
                .map(CartMapper::toItemResponse)
                .collect(Collectors.toList());
    }
}
