package com.example.spring_boot_project_api.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.spring_boot_project_api.dto.request.TourPlaceRequestDTO;
import com.example.spring_boot_project_api.dto.response.TourPlaceResponseDTO;

public interface TourPlaceService {

    TourPlaceResponseDTO create(TourPlaceRequestDTO request);

    TourPlaceResponseDTO getById(Long id);

    List<TourPlaceResponseDTO> getAll();

    TourPlaceResponseDTO update(Long id, TourPlaceRequestDTO request);

    void delete(Long id);

    List<TourPlaceResponseDTO> searchByName(String keyword);

    List<TourPlaceResponseDTO> getByDistrictId(Long districtId);

    List<TourPlaceResponseDTO> getByCategoryId(Long categoryId);

    List<TourPlaceResponseDTO> getByUserId(Long userId);

    List<TourPlaceResponseDTO> getByStatus(String status);

    List<TourPlaceResponseDTO> getByMinRating(BigDecimal minRating);

    List<TourPlaceResponseDTO> getByDistrictAndCategory(Long districtId, Long categoryId);
}