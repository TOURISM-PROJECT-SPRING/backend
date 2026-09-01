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

import com.example.spring_boot_project_api.dto.request.LocationRequest;
import com.example.spring_boot_project_api.dto.response.LocationResponse;
import com.example.spring_boot_project_api.service.LocationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationResponse>> findAll() {
        return ResponseEntity.ok(locationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.findById(id));
    }

    @GetMapping("/province")
    public ResponseEntity<List<LocationResponse>> findByProvince(@RequestParam("name") String name) {
        return ResponseEntity.ok(locationService.findByProvince(name));
    }

    @GetMapping("/district")
    public ResponseEntity<List<LocationResponse>> findByDistrict(@RequestParam("name") String name) {
        return ResponseEntity.ok(locationService.findByDistrict(name));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationResponse>> search(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(locationService.search(keyword));
    }

    @PostMapping
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody LocationRequest request) {
        LocationResponse response = locationService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
