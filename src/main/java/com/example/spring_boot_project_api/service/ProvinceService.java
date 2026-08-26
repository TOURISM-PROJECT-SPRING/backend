package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.ProvinceRequest;
import com.example.spring_boot_project_api.dto.response.ProvinceResponse;

public interface ProvinceService {

    List<ProvinceResponse> findAll();

    ProvinceResponse findById(Long id);

    List<ProvinceResponse> search(String keyword);

    ProvinceResponse create(ProvinceRequest request);

    ProvinceResponse update(Long id, ProvinceRequest request);

    void delete(Long id);
}
