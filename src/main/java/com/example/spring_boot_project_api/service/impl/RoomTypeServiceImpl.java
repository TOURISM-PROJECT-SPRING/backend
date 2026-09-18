package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.RoomTypeRequest;
import com.example.spring_boot_project_api.dto.response.RoomTypeResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.RoomTypeMapper;
import com.example.spring_boot_project_api.model.RoomTypes;
import com.example.spring_boot_project_api.repository.RoomTypeRepository;
import com.example.spring_boot_project_api.service.RoomTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "roomTypes", key = "#root.methodName")
    public List<RoomTypeResponse> findAll() {
        return RoomTypeMapper.toResponseList(roomTypeRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "roomTypes", key = "#root.methodName")
    public RoomTypeResponse findById(Long id) {
        RoomTypes type = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room Type", id));
        return RoomTypeMapper.toResponse(type);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "roomTypes", key = "#root.methodName")
    public List<RoomTypeResponse> searchByRoomType(String keyword) {
        return RoomTypeMapper.toResponseList(
                roomTypeRepository.findByRoomTypeContainingIgnoreCase(keyword));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "roomTypes", key = "#root.methodName")
    public List<RoomTypeResponse> findByMinCapacity(Integer minCapacity) {
        return RoomTypeMapper.toResponseList(
                roomTypeRepository.findByCapacityGreaterThanEqual(minCapacity));
    }

    @Override
    @CacheEvict(cacheNames = "roomTypes", key = "#root.methodName")
    public RoomTypeResponse create(RoomTypeRequest request) {
        if (roomTypeRepository.existsByRoomType(request.getRoomType())) {
            throw new IllegalArgumentException(
                    "Room type already exists: " + request.getRoomType());
        }

        RoomTypes type = RoomTypeMapper.toEntity(request);
        RoomTypes saved = roomTypeRepository.save(type);
        return RoomTypeMapper.toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "roomTypes", key = "#root.methodName")
    public RoomTypeResponse update(Long id, RoomTypeRequest request) {
        RoomTypes type = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room Type", id));

        RoomTypeMapper.toEntity(type, request);
        RoomTypes updated = roomTypeRepository.save(type);
        return RoomTypeMapper.toResponse(updated);
    }

    @Override
    @CacheEvict(cacheNames = "roomTypes", key = "#root.methodName")
    public void delete(Long id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room Type", id);
        }
        roomTypeRepository.deleteById(id);
    }
}