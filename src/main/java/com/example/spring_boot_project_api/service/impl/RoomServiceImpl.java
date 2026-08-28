package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.RoomRequest;
import com.example.spring_boot_project_api.dto.response.RoomResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.RoomMapper;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.RoomTypes;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.RoomRepository;
import com.example.spring_boot_project_api.repository.RoomTypeRepository;
import com.example.spring_boot_project_api.service.RoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findAll() {
        return RoomMapper.toResponseList(roomRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse findById(Long id) {
        Rooms room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
        return RoomMapper.toResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findByHotelId(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }
        return RoomMapper.toResponseList(roomRepository.findByHotelsId(hotelId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findByRoomTypeId(Long roomTypeId) {
        if (!roomTypeRepository.existsById(roomTypeId)) {
            throw new ResourceNotFoundException("Room Type", roomTypeId);
        }
        return RoomMapper.toResponseList(roomRepository.findByRoomTypesId(roomTypeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findByHotelAndRoomType(Long hotelId, Long roomTypeId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }
        if (!roomTypeRepository.existsById(roomTypeId)) {
            throw new ResourceNotFoundException("Room Type", roomTypeId);
        }
        return RoomMapper.toResponseList(
                roomRepository.findByHotelsIdAndRoomTypesId(hotelId, roomTypeId));
    }

    @Override
    public RoomResponse create(RoomRequest request) {
        Hotels hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", request.getHotelId()));

        RoomTypes roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room Type", request.getRoomTypeId()));

        Rooms room = RoomMapper.toEntity(request, hotel, roomType);
        Rooms saved = roomRepository.save(room);
        return RoomMapper.toResponse(saved);
    }

    @Override
    public RoomResponse update(Long id, RoomRequest request) {
        Rooms room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));

        Hotels hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", request.getHotelId()));

        RoomTypes roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room Type", request.getRoomTypeId()));

        RoomMapper.toEntity(room, request, hotel, roomType);
        Rooms updated = roomRepository.save(room);
        return RoomMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room", id);
        }
        roomRepository.deleteById(id);
    }
}