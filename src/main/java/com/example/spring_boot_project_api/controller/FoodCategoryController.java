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

import com.example.spring_boot_project_api.dto.request.FoodCategoryRequest;
import com.example.spring_boot_project_api.dto.response.FoodCategoryResponse;
import com.example.spring_boot_project_api.service.FoodCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/food-categories")
@RequiredArgsConstructor
public class FoodCategoryController {

    private final FoodCategoryService foodCategoryService;

    @GetMapping
    public ResponseEntity<List<FoodCategoryResponse>> findAll() {
        return ResponseEntity.ok(foodCategoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodCategoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(foodCategoryService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoodCategoryResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(foodCategoryService.search(keyword));
    }

    @PostMapping
    public ResponseEntity<FoodCategoryResponse> create(@Valid @RequestBody FoodCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodCategoryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodCategoryResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody FoodCategoryRequest request) {
        return ResponseEntity.ok(foodCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
