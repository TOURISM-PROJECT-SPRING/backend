package com.example.spring_boot_project_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.RoomBookingRequest;
import com.example.spring_boot_project_api.dto.response.RoomBookingResponse;
import com.example.spring_boot_project_api.service.RoomBookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/room-bookings")
@RequiredArgsConstructor
public class RoomBookingController {

    private final RoomBookingService roomBookingService;

    @GetMapping
    public ResponseEntity<List<RoomBookingResponse>> findAll() {
        return ResponseEntity.ok(roomBookingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomBookingResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(roomBookingService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RoomBookingResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(roomBookingService.findByUserId(userId));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<RoomBookingResponse>> findByRoomId(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomBookingService.findByRoomId(roomId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RoomBookingResponse>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(roomBookingService.findByStatus(status));
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<RoomBookingResponse>> findByUserIdAndStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        return ResponseEntity.ok(roomBookingService.findByUserIdAndStatus(userId, status));
    }

    @PostMapping
    public ResponseEntity<RoomBookingResponse> create(@Valid @RequestBody RoomBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomBookingService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomBookingResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody RoomBookingRequest request) {
        return ResponseEntity.ok(roomBookingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomBookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}