package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spring_boot_project_api.dto.request.ProvinceRequest;
import com.example.spring_boot_project_api.dto.response.ProvinceResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.ProvinceMapper;
import com.example.spring_boot_project_api.model.Provinces;
import com.example.spring_boot_project_api.repository.ProvinceRepository;
import com.example.spring_boot_project_api.service.ProvinceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceRepository provinceRepository;

    @Override
    public List<ProvinceResponse> findAll() {
        return ProvinceMapper.toResponseList(provinceRepository.findAll());
    }

    @Override
    public ProvinceResponse findById(Long id) {
        Provinces province = provinceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Province", id));
        return ProvinceMapper.toResponse(province);
    }

    @Override
    public List<ProvinceResponse> search(String keyword) {
        return ProvinceMapper.toResponseList(
                provinceRepository.findByNameContainingIgnoreCase(keyword));
    }

    @Override
    public ProvinceResponse create(ProvinceRequest request) {
        if (provinceRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Province already exists with name: " + request.getName());
        }
        Provinces province = ProvinceMapper.toEntity(request);
        Provinces saved = provinceRepository.save(province);
        return ProvinceMapper.toResponse(saved);
    }

    @Override
    public ProvinceResponse update(Long id, ProvinceRequest request) {
        Provinces province = provinceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Province", id));
        province.setName(request.getName());
        province.setImage(request.getImage());
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
}
