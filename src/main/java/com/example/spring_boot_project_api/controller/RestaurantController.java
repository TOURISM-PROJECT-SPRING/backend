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

import com.example.spring_boot_project_api.dto.request.RestaurantRequest;
import com.example.spring_boot_project_api.dto.response.RestaurantResponse;
import com.example.spring_boot_project_api.service.RestaurantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> findAll() {
        return ResponseEntity.ok(restaurantService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(restaurantService.search(keyword));
    }

    @GetMapping("/tourism-place/{tourismPlaceId}")
    public ResponseEntity<List<RestaurantResponse>> findByTourismPlaceId(
            @PathVariable Long tourismPlaceId) {
        return ResponseEntity.ok(restaurantService.findByTourismPlaceId(tourismPlaceId));
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        restaurantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
