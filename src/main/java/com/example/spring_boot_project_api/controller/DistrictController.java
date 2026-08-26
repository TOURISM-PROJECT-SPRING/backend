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

import com.example.spring_boot_project_api.dto.request.DistrictRequest;
import com.example.spring_boot_project_api.dto.response.DistrictResponse;
import com.example.spring_boot_project_api.service.DistrictService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @GetMapping
    public ResponseEntity<List<DistrictResponse>> findAll() {
        return ResponseEntity.ok(districtService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DistrictResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(districtService.findById(id));
    }

    @GetMapping("/province/{provinceId}")
    public ResponseEntity<List<DistrictResponse>> findByProvinceId(@PathVariable Long provinceId) {
        return ResponseEntity.ok(districtService.findByProvinceId(provinceId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DistrictResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(districtService.search(keyword));
    }

    @PostMapping
    public ResponseEntity<DistrictResponse> create(@Valid @RequestBody DistrictRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(districtService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DistrictResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody DistrictRequest request) {
        return ResponseEntity.ok(districtService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        districtService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
