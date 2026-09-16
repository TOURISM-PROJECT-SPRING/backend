package com.example.spring_boot_project_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.OwnerOfferingRequest;
import com.example.spring_boot_project_api.dto.response.OwnerDashboardStatsDTO;
import com.example.spring_boot_project_api.dto.response.OwnerOfferingDTO;
import com.example.spring_boot_project_api.dto.response.UnifiedBookingResponse;
import com.example.spring_boot_project_api.service.OwnerDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@Tag(name = "Owner Dashboard APIs", description = "Endpoints for business owners to manage operations, bookings, stats and offerings")
public class OwnerDashboardController {

    private final OwnerDashboardService ownerDashboardService;

    @Operation(summary = "Get owner dashboard metrics & overview stats")
    @GetMapping("/dashboard-stats")
    public ResponseEntity<OwnerDashboardStatsDTO> getDashboardStats(
            @RequestParam(required = false, defaultValue = "1") Long ownerId) {
        return ResponseEntity.ok(ownerDashboardService.getDashboardStats(ownerId));
    }

    @Operation(summary = "Get bookings specific to this owner's business, with optional status filtering")
    @GetMapping("/bookings")
    public ResponseEntity<List<UnifiedBookingResponse>> getOwnerBookings(
            @RequestParam(required = false, defaultValue = "1") Long ownerId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ownerDashboardService.getOwnerBookings(ownerId, status));
    }

    @Operation(summary = "Update booking status (Confirmed, Pending, Cancelled) and broadcast real-time update")
    @PutMapping("/bookings/{bookingType}/{id}/status")
    public ResponseEntity<UnifiedBookingResponse> updateBookingStatus(
            @PathVariable String bookingType,
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ownerDashboardService.updateBookingStatus(bookingType, id, status));
    }

    @Operation(summary = "Get all services/offerings (hotel rooms, tours, food menu items) belonging to this owner")
    @GetMapping("/services")
    public ResponseEntity<List<OwnerOfferingDTO>> getOwnerServices(
            @RequestParam(required = false, defaultValue = "1") Long ownerId) {
        return ResponseEntity.ok(ownerDashboardService.getOwnerOfferings(ownerId));
    }

    @Operation(summary = "Create a new service or offering")
    @PostMapping("/services")
    public ResponseEntity<OwnerOfferingDTO> createService(
            @RequestParam(required = false, defaultValue = "1") Long ownerId,
            @Valid @RequestBody OwnerOfferingRequest request) {
        OwnerOfferingDTO created = ownerDashboardService.createOffering(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Toggle service or menu item availability")
    @PatchMapping("/services/{offeringType}/{id}/availability")
    public ResponseEntity<OwnerOfferingDTO> toggleAvailability(
            @PathVariable String offeringType,
            @PathVariable Long id,
            @RequestParam Boolean isAvailable) {
        return ResponseEntity.ok(ownerDashboardService.toggleOfferingAvailability(offeringType, id, isAvailable));
    }

    @Operation(summary = "Delete an offering by type and ID")
    @DeleteMapping("/services/{offeringType}/{id}")
    public ResponseEntity<Void> deleteOffering(
            @PathVariable String offeringType,
            @PathVariable Long id) {
        ownerDashboardService.deleteOffering(offeringType, id);
        return ResponseEntity.noContent().build();
    }
}
