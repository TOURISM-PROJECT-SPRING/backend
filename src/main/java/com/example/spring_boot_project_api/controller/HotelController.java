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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.HotelRequest;
import com.example.spring_boot_project_api.dto.response.HotelResponse;
import com.example.spring_boot_project_api.service.HotelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public ResponseEntity<List<HotelResponse>> findAll() {
        return ResponseEntity.ok(hotelService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(hotelService.searchByName(keyword));
    }

    @GetMapping("/location/{districtId}")
    public ResponseEntity<List<HotelResponse>> findByLocation(@PathVariable Long districtId) {
        return ResponseEntity.ok(hotelService.findByLocationId(districtId));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<HotelResponse>> findByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(hotelService.findByOwnerId(ownerId));
    }

    @PostMapping
    public ResponseEntity<HotelResponse> create(@Valid @RequestBody HotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody HotelRequest request) {
        return ResponseEntity.ok(hotelService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}