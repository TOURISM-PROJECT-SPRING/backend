package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_project_api.dto.request.FoodRequest;
import com.example.spring_boot_project_api.dto.response.FoodResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.FoodMapper;
import com.example.spring_boot_project_api.model.FoodCategories;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.repository.FoodCategoryRepository;
import com.example.spring_boot_project_api.repository.FoodRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.service.CloudinaryService;
import com.example.spring_boot_project_api.service.FoodService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "foods", key = "#root.methodName")
    public List<FoodResponse> findAll() {
        return FoodMapper.toResponseList(foodRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "foods", key = "#root.methodName")
    public FoodResponse findById(Long id) {
        Foods food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food", id));
        return FoodMapper.toResponse(food);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "foods", key = "#root.methodName")
    public List<FoodResponse> search(String keyword) {
        return FoodMapper.toResponseList(foodRepository.findByNameContainingIgnoreCase(keyword));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "foods", key = "#root.methodName")
    public List<FoodResponse> findByRestaurantId(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", restaurantId);
        }
        return FoodMapper.toResponseList(foodRepository.findByRestaurantsId(restaurantId));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "foods", key = "#root.methodName")
    public List<FoodResponse> findByFoodCategoryId(Long foodCategoryId) {
        if (!foodCategoryRepository.existsById(foodCategoryId)) {
            throw new ResourceNotFoundException("Food Category", foodCategoryId);
        }
        return FoodMapper.toResponseList(foodRepository.findByFoodCategoriesId(foodCategoryId));
    }

    @Override
    @CacheEvict(cacheNames = "foods", key = "#root.methodName")
    public FoodResponse create(FoodRequest request, MultipartFile image) {
        Restaurants restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", request.getRestaurantId()));

        FoodCategories foodCategory = foodCategoryRepository.findById(request.getFoodCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Food Category", request.getFoodCategoryId()));

        Foods food = FoodMapper.toEntity(request, restaurant, foodCategory);
        setImageUrl(food, image);
        Foods saved = foodRepository.save(food);
        return FoodMapper.toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "foods", key = "#root.methodName")
    public FoodResponse update(Long id, FoodRequest request, MultipartFile image) {
        Foods food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food", id));

        Restaurants restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", request.getRestaurantId()));

        FoodCategories foodCategory = foodCategoryRepository.findById(request.getFoodCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Food Category", request.getFoodCategoryId()));

        FoodMapper.toEntity(food, request, restaurant, foodCategory);
        setImageUrl(food, image);
        Foods updated = foodRepository.save(food);
        return FoodMapper.toResponse(updated);
    }

    @Override
    @CacheEvict(cacheNames = "foods", key = "#root.methodName")
    public void delete(Long id) {
        if (!foodRepository.existsById(id)) {
            throw new ResourceNotFoundException("Food", id);
        }
        foodRepository.deleteById(id);
    }

    private void setImageUrl(Foods food, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            try {
                food.setImage(cloudinaryService.uploadImage(image));
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }
    }
}
