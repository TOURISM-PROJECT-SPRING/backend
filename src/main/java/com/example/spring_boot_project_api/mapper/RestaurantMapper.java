package com.example.spring_boot_project_api.mapper;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.RestaurantRequest;
import com.example.spring_boot_project_api.dto.response.RestaurantResponse;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.TourismPlaces;

public class RestaurantMapper {

    private RestaurantMapper() {}

    public static Restaurants toEntity(RestaurantRequest request, TourismPlaces tourismPlace) {
        Restaurants entity = new Restaurants();
        toEntity(entity, request, tourismPlace);
        return entity;
    }

    public static void toEntity(Restaurants entity, RestaurantRequest request,
                                TourismPlaces tourismPlace) {
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setOpenTime(request.getOpenTime());
        entity.setClossTime(request.getCloseTime());
        entity.setTourismPlaces(tourismPlace);
    }

    public static RestaurantResponse toResponse(Restaurants entity) {
        if (entity == null) return null;
        RestaurantResponse response = new RestaurantResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setOpenTime(entity.getOpenTime());
        response.setCloseTime(entity.getClossTime());
        if (entity.getTourismPlaces() != null) {
            response.setTourismPlaceId(entity.getTourismPlaces().getId());
            response.setTourismPlaceName(entity.getTourismPlaces().getName());
        }
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public static List<RestaurantResponse> toResponseList(List<Restaurants> entities) {
        return entities.stream()
                .map(RestaurantMapper::toResponse)
                .toList();
    }
}
