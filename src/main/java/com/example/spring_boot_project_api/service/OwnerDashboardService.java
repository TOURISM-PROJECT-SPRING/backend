package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.OwnerOfferingRequest;
import com.example.spring_boot_project_api.dto.response.OwnerDashboardStatsDTO;
import com.example.spring_boot_project_api.dto.response.OwnerOfferingDTO;
import com.example.spring_boot_project_api.dto.response.UnifiedBookingResponse;

public interface OwnerDashboardService {

    OwnerDashboardStatsDTO getDashboardStats(Long ownerId);

    List<UnifiedBookingResponse> getOwnerBookings(Long ownerId, String status);

    UnifiedBookingResponse updateBookingStatus(String bookingType, Long id, String status);

    List<OwnerOfferingDTO> getOwnerOfferings(Long ownerId);

    OwnerOfferingDTO createOffering(Long ownerId, OwnerOfferingRequest request);

    OwnerOfferingDTO toggleOfferingAvailability(String offeringType, Long id, Boolean isAvailable);

    void deleteOffering(String offeringType, Long id);
}
