package com.example.spring_boot_project_api.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.spring_boot_project_api.dto.request.HotelRoomRequest;
import com.example.spring_boot_project_api.dto.response.HotelRoomResponse;

public interface HotelRoomService {

    List<HotelRoomResponse> findAll();

    HotelRoomResponse findById(Long id);

    List<HotelRoomResponse> findByHotelId(Long hotelId);

    List<HotelRoomResponse> findByRoomTypeId(Long roomTypeId);

    List<HotelRoomResponse> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<HotelRoomResponse> findByMinCapacity(Integer minCapacity);

    HotelRoomResponse create(HotelRoomRequest request);

    HotelRoomResponse update(Long id, HotelRoomRequest request);

    void delete(Long id);
}