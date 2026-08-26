package com.example.spring_boot_project_api.mapper;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.DistrictRequest;
import com.example.spring_boot_project_api.dto.response.DistrictResponse;
import com.example.spring_boot_project_api.model.Districts;
import com.example.spring_boot_project_api.model.Provinces;

public class DistrictMapper {

    private DistrictMapper() {}

    public static DistrictResponse toResponse(Districts entity) {
        DistrictResponse response = new DistrictResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        if (entity.getProvinces() != null) {
            response.setProvinceId(entity.getProvinces().getId());
            response.setProvinceName(entity.getProvinces().getName());
        }
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public static List<DistrictResponse> toResponseList(List<Districts> entities) {
        return entities.stream()
                .map(DistrictMapper::toResponse)
                .toList();
    }

    public static Districts toEntity(DistrictRequest request, Provinces province) {
        Districts entity = new Districts();
        entity.setName(request.getName());
        entity.setProvinces(province);
        return entity;
    }
}
