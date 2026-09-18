package com.example.spring_boot_project_api.service.impl;

import java.time.LocalTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.RestaurantRequest;
import com.example.spring_boot_project_api.dto.response.RestaurantResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.RestaurantMapper;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.service.RestaurantService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final TourismPlaceRepository tourismPlaceRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "restaurants", key = "#root.methodName")
    public List<RestaurantResponse> findAll() {
        return RestaurantMapper.toResponseList(restaurantRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "restaurants", key = "#root.methodName")
    public RestaurantResponse findById(Long id) {
        Restaurants restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));
        return RestaurantMapper.toResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "restaurants", key = "#root.methodName")
    public List<RestaurantResponse> search(String keyword) {
        return RestaurantMapper.toResponseList(
                restaurantRepository.findByNameContainingIgnoreCase(keyword));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "restaurants", key = "#root.methodName")
    public List<RestaurantResponse> findByTourismPlaceId(Long tourismPlaceId) {
        if (!tourismPlaceRepository.existsById(tourismPlaceId)) {
            throw new ResourceNotFoundException("Tourism Place", tourismPlaceId);
        }
        return RestaurantMapper.toResponseList(
                restaurantRepository.findByTourPlacesId(tourismPlaceId));
    }

    @Override
    @CacheEvict(cacheNames = "restaurants", key = "#root.methodName")
    public RestaurantResponse create(RestaurantRequest request) {
        validateHours(request.getOpenTime(), request.getCloseTime());

        TourPlaces tourismPlace = tourismPlaceRepository.findById(request.getTourismPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourism Place", request.getTourismPlaceId()));

        Restaurants restaurant = RestaurantMapper.toEntity(request, tourismPlace);
        Restaurants saved = restaurantRepository.save(restaurant);
        return RestaurantMapper.toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "restaurants", key = "#root.methodName")
    public RestaurantResponse update(Long id, RestaurantRequest request) {
        validateHours(request.getOpenTime(), request.getCloseTime());

        Restaurants restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));

        TourPlaces tourismPlace = tourismPlaceRepository.findById(request.getTourismPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourism Place", request.getTourismPlaceId()));

        RestaurantMapper.toEntity(restaurant, request, tourismPlace);
        Restaurants updated = restaurantRepository.save(restaurant);
        return RestaurantMapper.toResponse(updated);
    }

    @Override
    @CacheEvict(cacheNames = "restaurants", key = "#root.methodName")
    public void delete(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Restaurant", id);
        }
        restaurantRepository.deleteById(id);
    }

    private void validateHours(LocalTime openTime, LocalTime closeTime) {
        if (openTime != null && closeTime != null && !closeTime.isAfter(openTime)) {
            throw new IllegalArgumentException("Close time must be after open time");
        }
    }
}
