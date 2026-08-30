package com.example.spring_boot_project_api.controller;

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

import com.example.spring_boot_project_api.dto.request.FoodRequest;
import com.example.spring_boot_project_api.dto.response.FoodResponse;
import com.example.spring_boot_project_api.service.FoodService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<FoodResponse>> findAll() {
        return ResponseEntity.ok(foodService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoodResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(foodService.search(keyword));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<FoodResponse>> findByRestaurantId(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(foodService.findByRestaurantId(restaurantId));
    }

    @GetMapping("/category/{foodCategoryId}")
    public ResponseEntity<List<FoodResponse>> findByFoodCategoryId(@PathVariable Long foodCategoryId) {
        return ResponseEntity.ok(foodService.findByFoodCategoryId(foodCategoryId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodResponse> create(
            @Valid @ModelAttribute FoodRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodService.create(request, image));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodResponse> update(
            @PathVariable Long id,
            @Valid @ModelAttribute FoodRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(foodService.update(id, request, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
