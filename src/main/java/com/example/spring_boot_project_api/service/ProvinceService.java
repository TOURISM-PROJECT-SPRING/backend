package com.example.spring_boot_project_api.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile; // នាំចូល Library នេះ

import com.example.spring_boot_project_api.dto.request.ProvinceRequest;
import com.example.spring_boot_project_api.dto.response.ProvinceResponse;

public interface ProvinceService {

    List<ProvinceResponse> findAll();

    ProvinceResponse findById(Long id);

    List<ProvinceResponse> search(String keyword);

    
    ProvinceResponse create(ProvinceRequest request, MultipartFile image);

    // បន្ថែម MultipartFile image សម្រាប់ការ Update ផងដែរ
    ProvinceResponse update(Long id, ProvinceRequest request, MultipartFile image);

    void delete(Long id);
}