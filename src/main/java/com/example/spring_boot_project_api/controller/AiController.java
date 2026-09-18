package com.example.spring_boot_project_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.AiChatRequest;
import com.example.spring_boot_project_api.dto.request.AiItineraryRequest;
import com.example.spring_boot_project_api.dto.request.AiRecommendationRequest;
import com.example.spring_boot_project_api.dto.response.AiChatResponse;
import com.example.spring_boot_project_api.dto.response.AiItineraryResponse;
import com.example.spring_boot_project_api.dto.response.AiRecommendationResponse;
import com.example.spring_boot_project_api.dto.response.AiStatusResponse;
import com.example.spring_boot_project_api.service.AiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for AI-powered Smart Tourism services.
 * Features:
 * - Conversational Smart Tourism AI Concierge
 * - Day-by-Day Travel Itinerary Generator
 * - Personalized Recommendations Engine
 * - Health and Connectivity Status
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Smart Tourism APIs", description = "Endpoints for Google Gemini AI travel assistance, itineraries, and recommendations")
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI Concierge", description = "Interactive conversation with Cambodia Smart Tourism AI assistant, grounded in database attractions, hotels, and dining.")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiService.chat(request));
    }

    @PostMapping("/itinerary")
    @Operation(summary = "Generate Travel Itinerary", description = "Generates a customized, day-by-day travel plan based on destination, duration, budget, and travel style.")
    public ResponseEntity<AiItineraryResponse> generateItinerary(@Valid @RequestBody AiItineraryRequest request) {
        return ResponseEntity.ok(aiService.generateItinerary(request));
    }

    @PostMapping("/recommendations")
    @Operation(summary = "Get Smart Recommendations", description = "Returns tailored recommendations for places, accommodations, dining, or tour packages matching traveler preferences.")
    public ResponseEntity<AiRecommendationResponse> getRecommendations(@Valid @RequestBody AiRecommendationRequest request) {
        return ResponseEntity.ok(aiService.getRecommendations(request));
    }

    @GetMapping("/status")
    @Operation(summary = "AI Service Status", description = "Checks the health and configuration status of the Google Gemini AI integration.")
    public ResponseEntity<AiStatusResponse> getStatus() {
        return ResponseEntity.ok(aiService.getStatus());
    }
}
