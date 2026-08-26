package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spring_boot_project_api.dto.request.FoodCategoryRequest;
import com.example.spring_boot_project_api.dto.response.FoodCategoryResponse;
import com.example.spring_boot_project_api.mapper.FoodCategoryMapper;
import com.example.spring_boot_project_api.model.FoodCategories;
import com.example.spring_boot_project_api.repository.FoodCategoryRepository;
import com.example.spring_boot_project_api.service.FoodCategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodCategoryServiceImpl implements FoodCategoryService {

    private final FoodCategoryRepository foodCategoryRepository;

    @Override
    public List<FoodCategoryResponse> findAll() {
        return FoodCategoryMapper.toResponseList(foodCategoryRepository.findAll());
    }

    @Override
    public FoodCategoryResponse findById(Long id) {
        FoodCategories category = foodCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food category not found with id: " + id));
        return FoodCategoryMapper.toResponse(category);
    }

    @Override
    public List<FoodCategoryResponse> search(String keyword) {
        return FoodCategoryMapper.toResponseList(
                foodCategoryRepository.findByNameContainingIgnoreCase(keyword));
    }

    @Override
    public FoodCategoryResponse create(FoodCategoryRequest request) {
        if (foodCategoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Food category already exists with name: " + request.getName());
        }
        FoodCategories category = FoodCategoryMapper.toEntity(request);
        FoodCategories saved = foodCategoryRepository.save(category);
        return FoodCategoryMapper.toResponse(saved);
    }

    @Override
    public FoodCategoryResponse update(Long id, FoodCategoryRequest request) {
        FoodCategories category = foodCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food category not found with id: " + id));
        category.setName(request.getName());
        FoodCategories updated = foodCategoryRepository.save(category);
        return FoodCategoryMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!foodCategoryRepository.existsById(id)) {
            throw new RuntimeException("Food category not found with id: " + id);
        }
        foodCategoryRepository.deleteById(id);
    }
}
