package com.example.spring_boot_project_api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.PlaceCategoryRequest;
import com.example.spring_boot_project_api.dto.response.PlaceCategoryResponse;
import com.example.spring_boot_project_api.model.PlaceCategoties;

public class PlaceCategoryMapper {

    private PlaceCategoryMapper() {}

    public static PlaceCategoryResponse toResponse(PlaceCategoties entity) {
        PlaceCategoryResponse response = new PlaceCategoryResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setImage(entity.getImage());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public static List<PlaceCategoryResponse> toResponseList(List<PlaceCategoties> entities) {
        return entities.stream()
                .map(PlaceCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static PlaceCategoties toEntity(PlaceCategoryRequest request) {
        PlaceCategoties entity = new PlaceCategoties();
        entity.setName(request.getName());
        entity.setImage(request.getImage());
        return entity;
    }
}
