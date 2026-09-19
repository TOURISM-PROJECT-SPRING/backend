package com.example.spring_boot_project_api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.FoodCategoryRequest;
import com.example.spring_boot_project_api.dto.response.FoodCategoryResponse;
import com.example.spring_boot_project_api.model.FoodCategories;

public class FoodCategoryMapper {

    private FoodCategoryMapper() {}

    public static FoodCategoryResponse toResponse(FoodCategories entity) {
        FoodCategoryResponse response = new FoodCategoryResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public static List<FoodCategoryResponse> toResponseList(List<FoodCategories> entities) {
        return entities.stream()
                .map(FoodCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static FoodCategories toEntity(FoodCategoryRequest request) {
        FoodCategories entity = new FoodCategories();
        entity.setName(request.getName());
        return entity;
    }
}
