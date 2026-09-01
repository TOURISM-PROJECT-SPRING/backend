package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.HotelRequest;
import com.example.spring_boot_project_api.dto.response.HotelResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.HotelMapper;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Location;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.LocationRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.HotelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> findAll() {
        return HotelMapper.toResponseList(hotelRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponse findById(Long id) {
        Hotels hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));
        return HotelMapper.toResponse(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> searchByName(String keyword) {
        return HotelMapper.toResponseList(
                hotelRepository.findByHotelNameContainingIgnoreCase(keyword));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> findByLocationId(Long districtId) {
        if (!locationRepository.existsById(districtId)) {
            throw new ResourceNotFoundException("Location", districtId);
        }
        return HotelMapper.toResponseList(hotelRepository.findByLocationId(districtId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> findByOwnerId(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new ResourceNotFoundException("User", ownerId);
        }
        return HotelMapper.toResponseList(hotelRepository.findByOwnerId(ownerId));
    }

    @Override
    public HotelResponse create(HotelRequest request) {
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", request.getLocationId()));

        Users owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getOwnerId()));

        if (hotelRepository.existsByHotelName(request.getHotelName())) {
            throw new IllegalArgumentException(
                    "Hotel already exists with name: " + request.getHotelName());
        }

        Hotels hotel = HotelMapper.toEntity(request, location, owner);
        Hotels saved = hotelRepository.save(hotel);
        return HotelMapper.toResponse(saved);
    }

    @Override
    public HotelResponse update(Long id, HotelRequest request) {
        Hotels hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", request.getLocationId()));

        Users owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getOwnerId()));

        HotelMapper.toEntity(hotel, request, location, owner);
        Hotels updated = hotelRepository.save(hotel);
        return HotelMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel", id);
        }
        hotelRepository.deleteById(id);
    }
}