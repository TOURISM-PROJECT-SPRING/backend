package com.example.spring_boot_project_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.TourBookingRequest;
import com.example.spring_boot_project_api.dto.response.TourBookingResponse;
import com.example.spring_boot_project_api.service.TourBookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/tour-bookings")
@RequiredArgsConstructor
public class TourBookingController {

    private final TourBookingService tourBookingService;

    @GetMapping
    public ResponseEntity<List<TourBookingResponse>> findAll() {
        return ResponseEntity.ok(tourBookingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourBookingResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tourBookingService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TourBookingResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(tourBookingService.findByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TourBookingResponse>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(tourBookingService.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<TourBookingResponse> create(@Valid @RequestBody TourBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tourBookingService.create(request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TourBookingResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(tourBookingService.cancel(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourBookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
