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

import com.example.spring_boot_project_api.dto.request.PlaceCategoryRequest;
import com.example.spring_boot_project_api.dto.response.PlaceCategoryResponse;
import com.example.spring_boot_project_api.service.CloudinaryService;
import com.example.spring_boot_project_api.service.PlaceCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/place-categories")
@RequiredArgsConstructor
public class PlaceCategoryController {

    private final PlaceCategoryService placeCategoryService;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<List<PlaceCategoryResponse>> findAll() {
        return ResponseEntity.ok(placeCategoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceCategoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(placeCategoryService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlaceCategoryResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(placeCategoryService.search(keyword));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PlaceCategoryResponse> create(
            @Valid @ModelAttribute PlaceCategoryRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            request.setImage(cloudinaryService.uploadImage(image));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(placeCategoryService.create(request));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<PlaceCategoryResponse> update(
            @PathVariable Long id,
            @Valid @ModelAttribute PlaceCategoryRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            request.setImage(cloudinaryService.uploadImage(image));
        }
        return ResponseEntity.ok(placeCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        placeCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
