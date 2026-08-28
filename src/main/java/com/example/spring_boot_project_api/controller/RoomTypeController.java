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

import com.example.spring_boot_project_api.dto.request.RoomTypeRequest;
import com.example.spring_boot_project_api.dto.response.RoomTypeResponse;
import com.example.spring_boot_project_api.service.RoomTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    public ResponseEntity<List<RoomTypeResponse>> findAll() {
        return ResponseEntity.ok(roomTypeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(roomTypeService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RoomTypeResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(roomTypeService.searchByRoomType(keyword));
    }

    @GetMapping("/min-capacity")
    public ResponseEntity<List<RoomTypeResponse>> findByMinCapacity(
            @RequestParam Integer minCapacity) {
        return ResponseEntity.ok(roomTypeService.findByMinCapacity(minCapacity));
    }

    @PostMapping
    public ResponseEntity<RoomTypeResponse> create(@Valid @RequestBody RoomTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomTypeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomTypeResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody RoomTypeRequest request) {
        return ResponseEntity.ok(roomTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}