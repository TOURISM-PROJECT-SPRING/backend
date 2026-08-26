package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.DistrictRequest;
import com.example.spring_boot_project_api.dto.response.DistrictResponse;

public interface DistrictService {

    List<DistrictResponse> findAll();

    DistrictResponse findById(Long id);

    List<DistrictResponse> findByProvinceId(Long provinceId);

    List<DistrictResponse> search(String keyword);

    DistrictResponse create(DistrictRequest request);

    DistrictResponse update(Long id, DistrictRequest request);

    void delete(Long id);
}
