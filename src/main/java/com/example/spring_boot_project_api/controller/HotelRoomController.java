package com.example.spring_boot_project_api.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.HotelRoomRequest;
import com.example.spring_boot_project_api.dto.response.HotelRoomResponse;
import com.example.spring_boot_project_api.service.HotelRoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/hotel-rooms")
@RequiredArgsConstructor
public class HotelRoomController {

    private final HotelRoomService hotelRoomService;

    @GetMapping
    public ResponseEntity<List<HotelRoomResponse>> findAll() {
        return ResponseEntity.ok(hotelRoomService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelRoomResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelRoomService.findById(id));
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<HotelRoomResponse>> findByHotelId(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelRoomService.findByHotelId(hotelId));
    }

    @GetMapping("/room-type/{roomTypeId}")
    public ResponseEntity<List<HotelRoomResponse>> findByRoomTypeId(@PathVariable Long roomTypeId) {
        return ResponseEntity.ok(hotelRoomService.findByRoomTypeId(roomTypeId));
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<HotelRoomResponse>> findByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(hotelRoomService.findByPriceRange(minPrice, maxPrice));
    }

    @GetMapping("/min-capacity")
    public ResponseEntity<List<HotelRoomResponse>> findByMinCapacity(
            @RequestParam Integer minCapacity) {
        return ResponseEntity.ok(hotelRoomService.findByMinCapacity(minCapacity));
    }

    @PostMapping
    public ResponseEntity<HotelRoomResponse> create(@Valid @RequestBody HotelRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelRoomService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelRoomResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody HotelRoomRequest request) {
        return ResponseEntity.ok(hotelRoomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelRoomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}