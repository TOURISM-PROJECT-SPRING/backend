package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.RestaurantRequest;
import com.example.spring_boot_project_api.dto.response.RestaurantResponse;

public interface RestaurantService {

    List<RestaurantResponse> findAll();

    RestaurantResponse findById(Long id);

    List<RestaurantResponse> search(String keyword);

    List<RestaurantResponse> findByTourismPlaceId(Long tourismPlaceId);

    RestaurantResponse create(RestaurantRequest request);

    RestaurantResponse update(Long id, RestaurantRequest request);

    void delete(Long id);
}
