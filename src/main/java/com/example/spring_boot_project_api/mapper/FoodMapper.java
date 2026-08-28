package com.example.spring_boot_project_api.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.FoodRequest;
import com.example.spring_boot_project_api.dto.response.FoodResponse;
import com.example.spring_boot_project_api.model.FoodCategories;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.Restaurants;

public class FoodMapper {

    private FoodMapper() {}

    public static Foods toEntity(FoodRequest request, Restaurants restaurant,
                                 FoodCategories foodCategory) {
        Foods entity = new Foods();
        toEntity(entity, request, restaurant, foodCategory);
        return entity;
    }

    public static void toEntity(Foods entity, FoodRequest request, Restaurants restaurant,
                                FoodCategories foodCategory) {
        entity.setName(request.getName());
        entity.setPrice(request.getPrice());
        entity.setImage(request.getImage());
        entity.setIsAvailable(request.getIsAvailable());
        entity.setRestaurants(restaurant);
        entity.setFoodCategories(foodCategory);
    }

    public static FoodResponse toResponse(Foods entity) {
        if (entity == null) return null;
        FoodResponse response = new FoodResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setPrice(entity.getPrice());
        response.setImage(entity.getImage());
        response.setIsAvailable(entity.getIsAvailable());
        response.setRestaurantId(entity.getRestaurants() != null
                ? entity.getRestaurants().getId() : null);
        response.setRestaurantName(entity.getRestaurants() != null
                ? entity.getRestaurants().getName() : null);
        response.setFoodCategoryId(entity.getFoodCategories() != null
                ? entity.getFoodCategories().getId() : null);
        response.setFoodCategoryName(entity.getFoodCategories() != null
                ? entity.getFoodCategories().getName() : null);
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public static List<FoodResponse> toResponseList(List<Foods> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
                .map(FoodMapper::toResponse)
                .collect(Collectors.toList());
    }
}
