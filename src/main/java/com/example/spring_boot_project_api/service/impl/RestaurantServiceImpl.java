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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.OwnerAccessControlService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final TourismPlaceRepository tourismPlaceRepository;
    private final UserRepository userRepository;
    private final OwnerAccessControlService ownerAccessControlService;

    private Users getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()))) {
            String username = auth.getName();
            return userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);
        }
        return null;
    }

    private boolean isOwner(Users u) {
        if (u == null || u.getUserRoles() == null) return false;
        boolean isAdmin = u.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole() != null && "ADMIN".equalsIgnoreCase(ur.getRole().getName()));
        if (isAdmin) return false;
        return u.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole() != null && "OWNER".equalsIgnoreCase(ur.getRole().getName()));
    }

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

        Users caller = getAuthenticatedUser();
        if (caller != null && isOwner(caller)) {
            ownerAccessControlService.requireBusinessAccess(caller.getId(), "RESTAURANT");
        }

        TourPlaces tourismPlace = tourismPlaceRepository.findById(request.getTourismPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourism Place", request.getTourismPlaceId()));

        Restaurants restaurant = RestaurantMapper.toEntity(request, tourismPlace);
        if (caller != null && isOwner(caller)) {
            restaurant.setOwner(caller);
        }
        Restaurants saved = restaurantRepository.save(restaurant);
        return RestaurantMapper.toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "restaurants", key = "#root.methodName")
    public RestaurantResponse update(Long id, RestaurantRequest request) {
        validateHours(request.getOpenTime(), request.getCloseTime());

        Restaurants restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));

        Users caller = getAuthenticatedUser();
        if (caller != null && isOwner(caller)) {
            ownerAccessControlService.requireBusinessAccess(caller.getId(), "RESTAURANT");
            if (restaurant.getOwner() != null && !restaurant.getOwner().getId().equals(caller.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("You do not own this restaurant");
            }
        }

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
        Restaurants restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));

        Users caller = getAuthenticatedUser();
        if (caller != null && isOwner(caller)) {
            ownerAccessControlService.requireBusinessAccess(caller.getId(), "RESTAURANT");
            if (restaurant.getOwner() != null && !restaurant.getOwner().getId().equals(caller.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("You do not own this restaurant");
            }
        }

        restaurantRepository.delete(restaurant);
    }

    private void validateHours(LocalTime openTime, LocalTime closeTime) {
        if (openTime != null && closeTime != null && !closeTime.isAfter(openTime)) {
            throw new IllegalArgumentException("Close time must be after open time");
        }
    }
}
