package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.RoomRequest;
import com.example.spring_boot_project_api.dto.response.RoomResponse;

public interface RoomService {

    List<RoomResponse> findAll();

    RoomResponse findById(Long id);

    List<RoomResponse> findByHotelId(Long hotelId);

    List<RoomResponse> findByRoomTypeId(Long roomTypeId);

    List<RoomResponse> findByHotelAndRoomType(Long hotelId, Long roomTypeId);

    RoomResponse create(RoomRequest request);

    RoomResponse update(Long id, RoomRequest request);

    void delete(Long id);
}