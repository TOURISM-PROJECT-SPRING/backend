package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.LocationRequest;
import com.example.spring_boot_project_api.dto.response.LocationResponse;

public interface LocationService {

    List<LocationResponse> findAll();

    LocationResponse findById(Long id);

    List<LocationResponse> findByProvince(String province);

    List<LocationResponse> findByDistrict(String district);

    List<LocationResponse> search(String keyword);

    LocationResponse create(LocationRequest request);

    LocationResponse update(Long id, LocationRequest request);

    void delete(Long id);
}
