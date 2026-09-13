package com.example.spring_boot_project_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.response.PromotionResponse;
import com.example.spring_boot_project_api.service.ManagementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final ManagementService managementService;

    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getActivePromotions() {
        List<PromotionResponse> active = managementService.findAllPromotions().stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .toList();
        return ResponseEntity.ok(active);
    }
}