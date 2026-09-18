package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.TourBookingRequest;
import com.example.spring_boot_project_api.dto.response.TourBookingResponse;

public interface TourBookingService {

    List<TourBookingResponse> findAll();

    TourBookingResponse findById(Long id);

    List<TourBookingResponse> findByUserId(Long userId);

    List<TourBookingResponse> findByStatus(String status);

    TourBookingResponse create(TourBookingRequest request);

    TourBookingResponse cancel(Long id);

    void delete(Long id);
}
