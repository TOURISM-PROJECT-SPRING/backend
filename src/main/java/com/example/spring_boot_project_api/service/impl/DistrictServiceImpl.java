package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spring_boot_project_api.dto.request.DistrictRequest;
import com.example.spring_boot_project_api.dto.response.DistrictResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.DistrictMapper;
import com.example.spring_boot_project_api.model.Districts;
import com.example.spring_boot_project_api.model.Provinces;
import com.example.spring_boot_project_api.repository.DistrictRepository;
import com.example.spring_boot_project_api.repository.ProvinceRepository;
import com.example.spring_boot_project_api.service.DistrictService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final ProvinceRepository provinceRepository;

    @Override
    public List<DistrictResponse> findAll() {
        return DistrictMapper.toResponseList(districtRepository.findAll());
    }

    @Override
    public DistrictResponse findById(Long id) {
        Districts district = districtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("District", id));
        return DistrictMapper.toResponse(district);
    }

    @Override
    public List<DistrictResponse> findByProvinceId(Long provinceId) {
        if (!provinceRepository.existsById(provinceId)) {
            throw new ResourceNotFoundException("Province", provinceId);
        }
        return DistrictMapper.toResponseList(
                districtRepository.findByProvincesIdOrderByNameAsc(provinceId));
    }

    @Override
    public List<DistrictResponse> search(String keyword) {
        return DistrictMapper.toResponseList(
                districtRepository.findByNameContainingIgnoreCase(keyword));
    }

    @Override
    public DistrictResponse create(DistrictRequest request) {
        Provinces province = provinceRepository.findById(request.getProvinceId())
                .orElseThrow(() -> new ResourceNotFoundException("Province", request.getProvinceId()));

        if (districtRepository.existsByProvincesIdAndNameIgnoreCase(
                province.getId(), request.getName())) {
            throw new IllegalArgumentException(
                    "District already exists with name: " + request.getName()
                            + " in province: " + province.getName());
        }

        Districts district = DistrictMapper.toEntity(request, province);
        Districts saved = districtRepository.save(district);
        return DistrictMapper.toResponse(saved);
    }

    @Override
    public DistrictResponse update(Long id, DistrictRequest request) {
        Districts district = districtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("District", id));

        Provinces province = provinceRepository.findById(request.getProvinceId())
                .orElseThrow(() -> new ResourceNotFoundException("Province", request.getProvinceId()));

        district.setName(request.getName());
        district.setProvinces(province);
        Districts updated = districtRepository.save(district);
        return DistrictMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!districtRepository.existsById(id)) {
            throw new ResourceNotFoundException("District", id);
        }
        districtRepository.deleteById(id);
    }
}
