package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.HotelRequest;
import com.example.spring_boot_project_api.dto.response.HotelResponse;

public interface HotelService {

    List<HotelResponse> findAll();

    HotelResponse findById(Long id);

    List<HotelResponse> searchByName(String keyword);

    List<HotelResponse> findByLocationId(Long districtId);

    List<HotelResponse> findByOwnerId(Long ownerId);

    HotelResponse create(HotelRequest request);

    HotelResponse update(Long id, HotelRequest request);

    void delete(Long id);
}