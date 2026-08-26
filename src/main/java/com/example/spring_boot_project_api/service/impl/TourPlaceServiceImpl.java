package com.example.spring_boot_project_api.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.TourPlaceRequestDTO;
import com.example.spring_boot_project_api.dto.response.TourPlaceResponseDTO;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.TourPlaceMapper;
import com.example.spring_boot_project_api.model.Districts;
import com.example.spring_boot_project_api.model.PlaceCategoties;
import com.example.spring_boot_project_api.model.TourismPlaces;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.DistrictRepository;
import com.example.spring_boot_project_api.repository.PlaceCategoryRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.TourPlaceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TourPlaceServiceImpl implements TourPlaceService {

    private final TourismPlaceRepository tourPlaceRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;

    @Override
    public TourPlaceResponseDTO create(TourPlaceRequestDTO request) {
        PlaceCategoties category = placeCategoryRepository.findById(request.getPlaceCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Place Category", request.getPlaceCategoryId()));

        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Districts district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", request.getDistrictId()));

        TourismPlaces place = TourPlaceMapper.toEntity(request, category, user, district);
        TourismPlaces saved = tourPlaceRepository.save(place);
        return TourPlaceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TourPlaceResponseDTO getById(Long id) {
        TourismPlaces place = tourPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourism Place", id));
        return TourPlaceMapper.toResponse(place);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getAll() {
        List<TourismPlaces> places = tourPlaceRepository.findAll();
        return TourPlaceMapper.toResponseList(places);
    }

    @Override
    public TourPlaceResponseDTO update(Long id, TourPlaceRequestDTO request) {
        TourismPlaces existing = tourPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourism Place", id));

        PlaceCategoties category = placeCategoryRepository.findById(request.getPlaceCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Place Category", request.getPlaceCategoryId()));

        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Districts district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", request.getDistrictId()));

        TourPlaceMapper.toEntity(existing, request, category, user, district);
        TourismPlaces updated = tourPlaceRepository.save(existing);
        return TourPlaceMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!tourPlaceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tourism Place", id);
        }
        tourPlaceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> searchByName(String keyword) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByNameContainingIgnoreCase(keyword));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByDistrictId(Long districtId) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByDistrictId(districtId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByCategoryId(Long categoryId) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByPlaceCategotyId(categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByUserId(Long userId) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByStatus(String status) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByStaus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByMinRating(BigDecimal minRating) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByRatingGreaterThanEqualOrderByRatingDesc(minRating));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByDistrictAndCategory(Long districtId, Long categoryId) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByDistrictIdAndPlaceCategotyId(districtId, categoryId));
    }
}
