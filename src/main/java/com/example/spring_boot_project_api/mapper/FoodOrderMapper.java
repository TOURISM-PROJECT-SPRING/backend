package com.example.spring_boot_project_api.mapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.response.FoodOrderItemResponse;
import com.example.spring_boot_project_api.dto.response.FoodOrderResponse;
import com.example.spring_boot_project_api.enums.OrderStatusEnum;
import com.example.spring_boot_project_api.model.FoodOrderItems;
import com.example.spring_boot_project_api.model.FoodOrders;
import com.example.spring_boot_project_api.model.Foods;
// import com.example.spring_boot_project_api.model.Restaurants;
// import com.example.spring_boot_project_api.model.Users;

public class FoodOrderMapper {

    private FoodOrderMapper() {}

    public static FoodOrderItems toOrderItem(FoodOrders order, Foods food, Integer quantity) {
        FoodOrderItems item = new FoodOrderItems();
        item.setFoodOrders(order);
        item.setFoods(food);
        item.setQuantity(quantity);
        BigDecimal unitPrice = food.getPrice();
        item.setUnitPrice(unitPrice);
        item.setSubTotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    public static FoodOrderItemResponse toItemResponse(FoodOrderItems item) {
        if (item == null) return null;
        FoodOrderItemResponse response = new FoodOrderItemResponse();
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

    public static FoodOrderResponse toResponse(FoodOrders order) {
        if (order == null) return null;
        FoodOrderResponse response = new FoodOrderResponse();
        response.setId(order.getId());
        if (order.getUser() != null) {
            response.setUserId(order.getUser().getId());
            response.setUserName(order.getUser().getFullname());
        }
        if (order.getRestuarants() != null) {
            response.setRestaurantId(order.getRestuarants().getId());
            response.setRestaurantName(order.getRestuarants().getName());
        }
        response.setTotalPrice(order.getTotalPrice());
        response.setPickupTime(order.getPickupTime());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        List<FoodOrderItemResponse> items = order.getFoodOrderItems() == null
                ? Collections.emptyList()
                : order.getFoodOrderItems().stream()
                        .map(FoodOrderMapper::toItemResponse)
                        .collect(Collectors.toList());
        response.setItems(items);
        return response;
    }

    public static List<FoodOrderResponse> toResponseList(List<FoodOrders> orders) {
        if (orders == null) return Collections.emptyList();
        return orders.stream()
                .map(FoodOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static List<FoodOrderItemResponse> toItemResponseList(List<FoodOrderItems> items) {
        if (items == null) return Collections.emptyList();
        return items.stream()
                .map(FoodOrderMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    public static String normalizeStatus(String status) {
        return status == null ? null : status.toUpperCase();
    }

    public static boolean isCancelled(FoodOrders order) {
        return order != null
                && OrderStatusEnum.CANCELLED.name().equalsIgnoreCase(order.getStatus());
    }
}
