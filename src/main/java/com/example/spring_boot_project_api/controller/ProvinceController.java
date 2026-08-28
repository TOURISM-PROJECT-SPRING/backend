package com.example.spring_boot_project_api.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_project_api.dto.request.ProvinceRequest;
import com.example.spring_boot_project_api.dto.response.ProvinceResponse;
import com.example.spring_boot_project_api.service.CloudinaryService;
import com.example.spring_boot_project_api.service.ProvinceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/provinces")
@RequiredArgsConstructor
public class ProvinceController {

    private final ProvinceService provinceService;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<List<ProvinceResponse>> findAll() {
        return ResponseEntity.ok(provinceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProvinceResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(provinceService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProvinceResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(provinceService.search(keyword));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProvinceResponse> create(
            @Valid @ModelAttribute ProvinceRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            request.setImage(cloudinaryService.uploadImage(image));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(provinceService.create(request));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ProvinceResponse> update(
            @PathVariable Long id,
            @Valid @ModelAttribute ProvinceRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            request.setImage(cloudinaryService.uploadImage(image));
        }
        return ResponseEntity.ok(provinceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        provinceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
