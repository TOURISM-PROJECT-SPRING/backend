package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.FoodCategoryRequest;
import com.example.spring_boot_project_api.dto.response.FoodCategoryResponse;

public interface FoodCategoryService {

    List<FoodCategoryResponse> findAll();

    FoodCategoryResponse findById(Long id);

    List<FoodCategoryResponse> search(String keyword);

    FoodCategoryResponse create(FoodCategoryRequest request);

    FoodCategoryResponse update(Long id, FoodCategoryRequest request);

    void delete(Long id);
}
