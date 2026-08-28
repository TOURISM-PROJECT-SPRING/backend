package com.example.spring_boot_project_api.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.HotelRoomRequest;
import com.example.spring_boot_project_api.dto.response.HotelRoomResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.HotelRoomMapper;
import com.example.spring_boot_project_api.model.HotelRooms;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.RoomTypes;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.HotelRoomRepository;
import com.example.spring_boot_project_api.repository.RoomTypeRepository;
import com.example.spring_boot_project_api.service.HotelRoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelRoomServiceImpl implements HotelRoomService {

    private final HotelRoomRepository hotelRoomRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HotelRoomResponse> findAll() {
        return HotelRoomMapper.toResponseList(hotelRoomRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public HotelRoomResponse findById(Long id) {
        HotelRooms hotelRoom = hotelRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Room", id));
        return HotelRoomMapper.toResponse(hotelRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelRoomResponse> findByHotelId(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }
        return HotelRoomMapper.toResponseList(hotelRoomRepository.findByHotelsId(hotelId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelRoomResponse> findByRoomTypeId(Long roomTypeId) {
        if (!roomTypeRepository.existsById(roomTypeId)) {
            throw new ResourceNotFoundException("Room Type", roomTypeId);
        }
        return HotelRoomMapper.toResponseList(hotelRoomRepository.findByRoomTypesId(roomTypeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelRoomResponse> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return HotelRoomMapper.toResponseList(
                hotelRoomRepository.findByPricePerNightBetween(minPrice, maxPrice));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelRoomResponse> findByMinCapacity(Integer minCapacity) {
        return HotelRoomMapper.toResponseList(
                hotelRoomRepository.findByCapacityGreaterThanEqual(minCapacity));
    }

    @Override
    public HotelRoomResponse create(HotelRoomRequest request) {
        Hotels hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", request.getHotelId()));

        RoomTypes roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room Type", request.getRoomTypeId()));

        if (hotelRoomRepository.existsByHotelsIdAndRoomTypesId(
                hotel.getId(), roomType.getId())) {
            throw new IllegalArgumentException(
                    "Room type already configured for hotel: " + hotel.getHotelName());
        }

        HotelRooms hotelRoom = HotelRoomMapper.toEntity(request, hotel, roomType);
        HotelRooms saved = hotelRoomRepository.save(hotelRoom);
        return HotelRoomMapper.toResponse(saved);
    }

    @Override
    public HotelRoomResponse update(Long id, HotelRoomRequest request) {
        HotelRooms hotelRoom = hotelRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Room", id));

        Hotels hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", request.getHotelId()));

        RoomTypes roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room Type", request.getRoomTypeId()));

        HotelRoomMapper.toEntity(hotelRoom, request, hotel, roomType);
        HotelRooms updated = hotelRoomRepository.save(hotelRoom);
        return HotelRoomMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!hotelRoomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel Room", id);
        }
        hotelRoomRepository.deleteById(id);
    }
}