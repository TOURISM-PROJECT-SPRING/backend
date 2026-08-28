package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.RoomBookingRequest;
import com.example.spring_boot_project_api.dto.response.RoomBookingResponse;

public interface RoomBookingService {

    List<RoomBookingResponse> findAll();

    RoomBookingResponse findById(Long id);

    List<RoomBookingResponse> findByUserId(Long userId);

    List<RoomBookingResponse> findByRoomId(Long roomId);

    List<RoomBookingResponse> findByStatus(String status);

    List<RoomBookingResponse> findByUserIdAndStatus(Long userId, String status);

    RoomBookingResponse create(RoomBookingRequest request);

    RoomBookingResponse update(Long id, RoomBookingRequest request);

    RoomBookingResponse cancel(Long id);

    void delete(Long id);
}