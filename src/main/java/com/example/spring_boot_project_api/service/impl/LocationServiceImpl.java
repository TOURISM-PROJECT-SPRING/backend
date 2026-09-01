package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.LocationRequest;
import com.example.spring_boot_project_api.dto.response.LocationResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.LocationMapper;
import com.example.spring_boot_project_api.model.Location;
import com.example.spring_boot_project_api.repository.LocationRepository;
import com.example.spring_boot_project_api.service.LocationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> findAll() {
        return LocationMapper.toResponseList(locationRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse findById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));
        return LocationMapper.toResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> findByProvince(String province) {
        return LocationMapper.toResponseList(
                locationRepository.findByProvinceContainingIgnoreCase(province));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> findByDistrict(String district) {
        return LocationMapper.toResponseList(
                locationRepository.findByDistrictContainingIgnoreCase(district));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> search(String keyword) {
        return LocationMapper.toResponseList(
                locationRepository.findByProvinceContainingIgnoreCaseOrDistrictContainingIgnoreCase(keyword, keyword));
    }

    @Override
    public LocationResponse create(LocationRequest request) {
        if (locationRepository.existsByProvinceAndDistrict(request.getProvince(), request.getDistrict())) {
            throw new IllegalArgumentException(
                    "Location already exists: " + request.getDistrict() + ", " + request.getProvince());
        }

        Location location = LocationMapper.toEntity(request);
        Location saved = locationRepository.save(location);
        return LocationMapper.toResponse(saved);
    }

    @Override
    public LocationResponse update(Long id, LocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));

        LocationMapper.toEntity(location, request);
        Location updated = locationRepository.save(location);
        return LocationMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location", id);
        }
        locationRepository.deleteById(id);
    }
}
