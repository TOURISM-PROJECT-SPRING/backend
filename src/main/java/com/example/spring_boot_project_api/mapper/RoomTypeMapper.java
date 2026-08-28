package com.example.spring_boot_project_api.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.RoomTypeRequest;
import com.example.spring_boot_project_api.dto.response.RoomTypeResponse;
import com.example.spring_boot_project_api.model.RoomTypes;

public class RoomTypeMapper {

    private RoomTypeMapper() {}

    public static RoomTypes toEntity(RoomTypeRequest request) {
        RoomTypes type = new RoomTypes();
        type.setRoomType(request.getRoomType());
        type.setCapacity(request.getCapacity());
        return type;
    }

    public static void toEntity(RoomTypes type, RoomTypeRequest request) {
        type.setRoomType(request.getRoomType());
        type.setCapacity(request.getCapacity());
    }

    public static RoomTypeResponse toResponse(RoomTypes type) {
        if (type == null) return null;
        return RoomTypeResponse.builder()
                .id(type.getId())
                .roomType(type.getRoomType())
                .capacity(type.getCapacity())
                .createdAt(type.getCreatedAt())
                .updatedAt(type.getUpdatedAt())
                .build();
    }

    public static List<RoomTypeResponse> toResponseList(List<RoomTypes> types) {
        if (types == null) return Collections.emptyList();
        return types.stream()
                .map(RoomTypeMapper::toResponse)
                .collect(Collectors.toList());
    }
}