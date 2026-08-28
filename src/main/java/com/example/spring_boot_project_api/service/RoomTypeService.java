package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.RoomTypeRequest;
import com.example.spring_boot_project_api.dto.response.RoomTypeResponse;

public interface RoomTypeService {

    List<RoomTypeResponse> findAll();

    RoomTypeResponse findById(Long id);

    List<RoomTypeResponse> searchByRoomType(String keyword);

    List<RoomTypeResponse> findByMinCapacity(Integer minCapacity);

    RoomTypeResponse create(RoomTypeRequest request);

    RoomTypeResponse update(Long id, RoomTypeRequest request);

    void delete(Long id);
}