package com.example.spring_boot_project_api.mapper;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.ProvinceRequest;
import com.example.spring_boot_project_api.dto.response.ProvinceResponse;
import com.example.spring_boot_project_api.model.Provinces;

public class ProvinceMapper {

    private ProvinceMapper() {}

    public static ProvinceResponse toResponse(Provinces entity) {
        ProvinceResponse response = new ProvinceResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setImage(entity.getImage());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public static List<ProvinceResponse> toResponseList(List<Provinces> entities) {
        return entities.stream()
                .map(ProvinceMapper::toResponse)
                .toList();
    }

    public static Provinces toEntity(ProvinceRequest request) {
        Provinces entity = new Provinces();
        entity.setName(request.getName());
        entity.setImage(request.getImage());
        return entity;
    }
}
