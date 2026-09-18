package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.TourBookingRequest;
import com.example.spring_boot_project_api.dto.response.TourBookingResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.TourBookingMapper;
import com.example.spring_boot_project_api.model.TourBookings;
import com.example.spring_boot_project_api.model.TourPackages;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.TourBookingRepository;
import com.example.spring_boot_project_api.repository.TourPackageRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.TourBookingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TourBookingServiceImpl implements TourBookingService {

    private final TourBookingRepository tourBookingRepository;
    private final TourPackageRepository tourPackageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TourBookingResponse> findAll() {
        return TourBookingMapper.toResponseList(tourBookingRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public TourBookingResponse findById(Long id) {
        TourBookings booking = tourBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour Booking", id));
        return TourBookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourBookingResponse> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return TourBookingMapper.toResponseList(
                tourBookingRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourBookingResponse> findByStatus(String status) {
        return TourBookingMapper.toResponseList(tourBookingRepository.findByStatus(status));
    }

    @Override
    public TourBookingResponse create(TourBookingRequest request) {
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        TourPackages tourPackage = tourPackageRepository.findById(request.getTourPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour Package", request.getTourPackageId()));

        TourBookings booking = TourBookingMapper.toEntity(request, user, tourPackage);
        TourBookings saved = tourBookingRepository.save(booking);
        return TourBookingMapper.toResponse(saved);
    }

    @Override
    public TourBookingResponse cancel(Long id) {
        TourBookings booking = tourBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour Booking", id));

        if (TourBookingMapper.STATUS_CANCELLED.equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        booking.setStatus(TourBookingMapper.STATUS_CANCELLED);
        TourBookings saved = tourBookingRepository.save(booking);
        return TourBookingMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!tourBookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tour Booking", id);
        }
        tourBookingRepository.deleteById(id);
    }
}
