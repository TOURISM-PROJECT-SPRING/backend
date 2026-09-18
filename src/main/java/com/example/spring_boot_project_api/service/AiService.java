package com.example.spring_boot_project_api.service;

import com.example.spring_boot_project_api.dto.request.AiChatRequest;
import com.example.spring_boot_project_api.dto.request.AiItineraryRequest;
import com.example.spring_boot_project_api.dto.request.AiRecommendationRequest;
import com.example.spring_boot_project_api.dto.response.AiChatResponse;
import com.example.spring_boot_project_api.dto.response.AiItineraryResponse;
import com.example.spring_boot_project_api.dto.response.AiRecommendationResponse;
import com.example.spring_boot_project_api.dto.response.AiStatusResponse;

public interface AiService {

    /**
     * Interactive multi-turn chat with the Cambodian Smart Tourism AI Concierge.
     * Integrates database context for authentic, local recommendations.
     */
    AiChatResponse chat(AiChatRequest request);

    /**
     * Generates a tailored day-by-day travel itinerary based on destination,
     * duration, budget, travel style, and special requests.
     */
    AiItineraryResponse generateItinerary(AiItineraryRequest request);

    /**
     * Returns targeted smart recommendations for places, accommodations, dining,
     * or tours matching traveler preferences.
     */
    AiRecommendationResponse getRecommendations(AiRecommendationRequest request);

    /**
     * Health and configuration status of the AI service and Google Gemini API.
     */
    AiStatusResponse getStatus();
}
