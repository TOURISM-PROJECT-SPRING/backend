package com.example.spring_boot_project_api.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_project_api.dto.request.FoodRequest;
import com.example.spring_boot_project_api.dto.response.FoodResponse;

public interface FoodService {

    List<FoodResponse> findAll();

    FoodResponse findById(Long id);

    List<FoodResponse> search(String keyword);

    List<FoodResponse> findByRestaurantId(Long restaurantId);

    List<FoodResponse> findByFoodCategoryId(Long foodCategoryId);

    FoodResponse create(FoodRequest request, MultipartFile image);

    FoodResponse update(Long id, FoodRequest request, MultipartFile image);

    void delete(Long id);
}
