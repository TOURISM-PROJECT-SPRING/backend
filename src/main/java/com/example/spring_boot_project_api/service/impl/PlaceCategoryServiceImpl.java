package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spring_boot_project_api.dto.request.PlaceCategoryRequest;
import com.example.spring_boot_project_api.dto.response.PlaceCategoryResponse;
import com.example.spring_boot_project_api.mapper.PlaceCategoryMapper;
import com.example.spring_boot_project_api.model.PlaceCategoties;
import com.example.spring_boot_project_api.repository.PlaceCategoryRepository;
import com.example.spring_boot_project_api.service.PlaceCategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceCategoryServiceImpl implements PlaceCategoryService {

    private final PlaceCategoryRepository placeCategoryRepository;

    @Override
    public List<PlaceCategoryResponse> findAll() {
        return PlaceCategoryMapper.toResponseList(placeCategoryRepository.findAll());
    }

    @Override
    public PlaceCategoryResponse findById(Long id) {
        PlaceCategoties category = placeCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Place category not found with id: " + id));
        return PlaceCategoryMapper.toResponse(category);
    }

    @Override
    public List<PlaceCategoryResponse> search(String keyword) {
        return PlaceCategoryMapper.toResponseList(
                placeCategoryRepository.findByNameContainingIgnoreCase(keyword));
    }

    @Override
    public PlaceCategoryResponse create(PlaceCategoryRequest request) {
        if (placeCategoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Place category already exists with name: " + request.getName());
        }
        PlaceCategoties category = PlaceCategoryMapper.toEntity(request);
        PlaceCategoties saved = placeCategoryRepository.save(category);
        return PlaceCategoryMapper.toResponse(saved);
    }

    @Override
    public PlaceCategoryResponse update(Long id, PlaceCategoryRequest request) {
        PlaceCategoties category = placeCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Place category not found with id: " + id));
        category.setName(request.getName());
        category.setImage(request.getImage());
        PlaceCategoties updated = placeCategoryRepository.save(category);
        return PlaceCategoryMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!placeCategoryRepository.existsById(id)) {
            throw new RuntimeException("Place category not found with id: " + id);
        }
        placeCategoryRepository.deleteById(id);
    }
}
