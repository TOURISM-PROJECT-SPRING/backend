package com.example.spring_boot_project_api.controller;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.example.spring_boot_project_api.dto.request.TourPlaceRequestDTO;
import com.example.spring_boot_project_api.dto.response.TourPlaceResponseDTO;
import com.example.spring_boot_project_api.service.TourPlaceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/tour-places")
@RequiredArgsConstructor
public class TourPlaceController {

    private final TourPlaceService tourPlaceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TourPlaceResponseDTO> create(
            @Valid @ModelAttribute TourPlaceRequestDTO request,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        TourPlaceResponseDTO response = tourPlaceService.create(request, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourPlaceResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tourPlaceService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<TourPlaceResponseDTO>> getAll() {
        return ResponseEntity.ok(tourPlaceService.getAll());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TourPlaceResponseDTO> update(
            @PathVariable Long id,
            @Valid @ModelAttribute TourPlaceRequestDTO request,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        return ResponseEntity.ok(tourPlaceService.update(id, request, images));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourPlaceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TourPlaceResponseDTO> addImages(
            @PathVariable Long id,
            @RequestParam(value = "images", required = true) MultipartFile[] images) {
        return ResponseEntity.ok(tourPlaceService.addImages(id, images));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<TourPlaceResponseDTO> removeImage(@PathVariable Long id,
                                                            @PathVariable Long imageId) {
        return ResponseEntity.ok(tourPlaceService.removeImage(id, imageId));
    }

    @DeleteMapping("/{id}/images")
    public ResponseEntity<TourPlaceResponseDTO> clearImages(@PathVariable Long id) {
        return ResponseEntity.ok(tourPlaceService.clearImages(id));
    }

    @PostMapping("/{id}/images/{imageId}/primary")
    public ResponseEntity<TourPlaceResponseDTO> setPrimaryImage(@PathVariable Long id,
                                                                @PathVariable Long imageId) {
        return ResponseEntity.ok(tourPlaceService.setPrimaryImage(id, imageId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TourPlaceResponseDTO>> searchByName(@RequestParam String keyword) {
        return ResponseEntity.ok(tourPlaceService.searchByName(keyword));
    }

    @GetMapping("/district/{districtId}")
    public ResponseEntity<List<TourPlaceResponseDTO>> getByDistrict(@PathVariable Long districtId) {
        return ResponseEntity.ok(tourPlaceService.getByDistrictId(districtId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TourPlaceResponseDTO>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(tourPlaceService.getByCategoryId(categoryId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TourPlaceResponseDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(tourPlaceService.getByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TourPlaceResponseDTO>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(tourPlaceService.getByStatus(status));
    }

    @GetMapping("/rating")
    public ResponseEntity<List<TourPlaceResponseDTO>> getByMinRating(@RequestParam BigDecimal minRating) {
        return ResponseEntity.ok(tourPlaceService.getByMinRating(minRating));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TourPlaceResponseDTO>> getByDistrictAndCategory(
            @RequestParam Long districtId,
            @RequestParam Long categoryId) {
        return ResponseEntity.ok(tourPlaceService.getByDistrictAndCategory(districtId, categoryId));
    }
}
