package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.BookingOrderRequest;
import com.example.spring_boot_project_api.dto.response.BookingFeedPageResponse;
import com.example.spring_boot_project_api.dto.response.UnifiedBookingResponse;

public interface AdminOwnerBookingService {

    /**
     * Create a new booking/order, persist it, and trigger real-time notification
     */
    UnifiedBookingResponse createBooking(BookingOrderRequest request);

    /**
     * Fetch global system-wide bookings for the Admin Dashboard with optional
     * filtering (type, status), full-text search, and pagination.
     */
    BookingFeedPageResponse getAdminBookings(String type, String status, String search,
            int page, int size);

    /**
     * Fetch business-specific bookings for an Owner Dashboard by Owner ID
     */
    List<UnifiedBookingResponse> getOwnerBookings(Long ownerId);
}
