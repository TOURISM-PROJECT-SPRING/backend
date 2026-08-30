package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_project_api.dto.request.ProvinceRequest;
import com.example.spring_boot_project_api.dto.response.ProvinceResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.ProvinceMapper;
import com.example.spring_boot_project_api.model.Provinces;
import com.example.spring_boot_project_api.repository.ProvinceRepository;
import com.example.spring_boot_project_api.service.CloudinaryService;
import com.example.spring_boot_project_api.service.ProvinceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceRepository provinceRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public List<ProvinceResponse> findAll() {
        return ProvinceMapper.toResponseList(provinceRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public ProvinceResponse findById(Long id) {
        Provinces province = provinceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Province", id));
        return ProvinceMapper.toResponse(province);
    }

    @Override
    public ProvinceResponse create(ProvinceRequest request, MultipartFile image) {
        Provinces province = ProvinceMapper.toEntity(request);

        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(image);
                province.setImage(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        Provinces saved = provinceRepository.save(province);
        return ProvinceMapper.toResponse(saved);
    }

    @Override
    public ProvinceResponse update(Long id, ProvinceRequest request, MultipartFile image) {
        Provinces province = provinceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Province", id));

        province.setName(request.getName());

        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(image);
                province.setImage(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        Provinces updated = provinceRepository.save(province);
        return ProvinceMapper.toResponse(updated);
    }
    @Override
    public void delete(Long id) {
        if (!provinceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Province", id);
        }
        provinceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinceResponse> search(String keyword) {
        // ស្វែងរកខេត្តដែលមានឈ្មោះផ្ទុក keyword ផ្ដល់ឱ្យ
        List<Provinces> provinces = provinceRepository.findByNameContainingIgnoreCase(keyword);
        return ProvinceMapper.toResponseList(provinces);
    }
}