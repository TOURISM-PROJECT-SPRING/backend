package com.example.spring_boot_project_api.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.LocationRequest;
import com.example.spring_boot_project_api.dto.response.LocationResponse;
import com.example.spring_boot_project_api.model.Location;

public class LocationMapper {

    private LocationMapper() {}

    public static Location toEntity(LocationRequest request) {
        Location entity = new Location();
        entity.setProvince(request.getProvince());
        entity.setDistrict(request.getDistrict());
        return entity;
    }

    public static void toEntity(Location entity, LocationRequest request) {
        entity.setProvince(request.getProvince());
        entity.setDistrict(request.getDistrict());
    }

    public static LocationResponse toResponse(Location entity) {
        if (entity == null) return null;
        LocationResponse response = new LocationResponse();
        response.setId(entity.getId());
        response.setProvince(entity.getProvince());
        response.setDistrict(entity.getDistrict());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public static List<LocationResponse> toResponseList(List<Location> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
                .map(LocationMapper::toResponse)
                .collect(Collectors.toList());
    }
}
