package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.PlaceCategoryRequest;
import com.example.spring_boot_project_api.dto.response.PlaceCategoryResponse;

public interface PlaceCategoryService {

    List<PlaceCategoryResponse> findAll();

    PlaceCategoryResponse findById(Long id);

    List<PlaceCategoryResponse> search(String keyword);

    PlaceCategoryResponse create(PlaceCategoryRequest request);

    PlaceCategoryResponse update(Long id, PlaceCategoryRequest request);

    void delete(Long id);
}
