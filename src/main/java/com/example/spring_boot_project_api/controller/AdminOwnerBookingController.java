package com.example.spring_boot_project_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.BookingOrderRequest;
import com.example.spring_boot_project_api.dto.response.UnifiedBookingResponse;
import com.example.spring_boot_project_api.service.AdminOwnerBookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Realtime Bookings & Dashboards", description = "Endpoints for booking checkout, Admin global orders, and Owner business orders")
public class AdminOwnerBookingController {

    private final AdminOwnerBookingService bookingService;

    @Operation(summary = "Submit booking or order checkout and trigger real-time notification")
    @PostMapping("/bookings")
    public ResponseEntity<UnifiedBookingResponse> createBooking(
            @Valid @RequestBody BookingOrderRequest request) {
        UnifiedBookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Fetch global bookings for Admin Dashboard")
    @GetMapping("/admin/bookings")
    public ResponseEntity<List<UnifiedBookingResponse>> getAdminBookings() {
        return ResponseEntity.ok(bookingService.getAdminBookings());
    }

    @Operation(summary = "Fetch business-specific bookings for Owner Dashboard by Owner ID")
    @GetMapping("/owner/bookings/{ownerId}")
    public ResponseEntity<List<UnifiedBookingResponse>> getOwnerBookings(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(bookingService.getOwnerBookings(ownerId));
    }
}
