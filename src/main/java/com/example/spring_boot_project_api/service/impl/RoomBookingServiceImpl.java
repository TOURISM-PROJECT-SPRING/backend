package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.RoomBookingRequest;
import com.example.spring_boot_project_api.dto.response.RoomBookingResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.RoomBookingMapper;
import com.example.spring_boot_project_api.model.RoomBookings;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.RoomRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.RoomBookingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomBookingServiceImpl implements RoomBookingService {

    private final RoomBookingRepository roomBookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoomBookingResponse> findAll() {
        return RoomBookingMapper.toResponseList(roomBookingRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomBookingResponse findById(Long id) {
        RoomBookings booking = roomBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room Booking", id));
        return RoomBookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomBookingResponse> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return RoomBookingMapper.toResponseList(
                roomBookingRepository.findByUsersIdOrderByCheckInDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomBookingResponse> findByRoomId(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room", roomId);
        }
        return RoomBookingMapper.toResponseList(roomBookingRepository.findByRoomsId(roomId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomBookingResponse> findByStatus(String status) {
        return RoomBookingMapper.toResponseList(roomBookingRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomBookingResponse> findByUserIdAndStatus(Long userId, String status) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return RoomBookingMapper.toResponseList(
                roomBookingRepository.findByUsersIdAndStatus(userId, status));
    }

    @Override
    public RoomBookingResponse create(RoomBookingRequest request) {
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Rooms room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", request.getRoomId()));

        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        RoomBookings booking = RoomBookingMapper.toEntity(request, user, room);
        RoomBookings saved = roomBookingRepository.save(booking);
        return RoomBookingMapper.toResponse(saved);
    }

    @Override
    public RoomBookingResponse update(Long id, RoomBookingRequest request) {
        RoomBookings booking = roomBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room Booking", id));

        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Rooms room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", request.getRoomId()));

        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        RoomBookingMapper.toEntity(booking, request, user, room);
        RoomBookings updated = roomBookingRepository.save(booking);
        return RoomBookingMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!roomBookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room Booking", id);
        }
        roomBookingRepository.deleteById(id);
    }
}