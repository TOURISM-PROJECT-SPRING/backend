package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.spring_boot_project_api.config.GeminiConfig;
import com.example.spring_boot_project_api.dto.request.AiChatMessage;
import com.example.spring_boot_project_api.dto.request.AiChatRequest;
import com.example.spring_boot_project_api.dto.request.AiItineraryRequest;
import com.example.spring_boot_project_api.dto.request.AiRecommendationRequest;
import com.example.spring_boot_project_api.dto.response.AiChatResponse;
import com.example.spring_boot_project_api.dto.response.AiItineraryResponse;
import com.example.spring_boot_project_api.dto.response.AiRecommendationResponse;
import com.example.spring_boot_project_api.dto.response.AiStatusResponse;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Location;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.repository.FoodRepository;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.PromotionRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.TourPackageRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.service.impl.AiServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

class AiServiceImplTest {

    private final GeminiConfig geminiConfig = mock(GeminiConfig.class);
    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final TourismPlaceRepository tourismPlaceRepository = mock(TourismPlaceRepository.class);
    private final HotelRepository hotelRepository = mock(HotelRepository.class);
    private final RestaurantRepository restaurantRepository = mock(RestaurantRepository.class);
    private final FoodRepository foodRepository = mock(FoodRepository.class);
    private final TourPackageRepository tourPackageRepository = mock(TourPackageRepository.class);
    private final PromotionRepository promotionRepository = mock(PromotionRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        when(geminiConfig.isConfigured()).thenReturn(true);
        when(geminiConfig.getApiKey()).thenReturn("AQ.test-key-1234567890");
        when(geminiConfig.getModel()).thenReturn("gemini-3.6-flash");
        when(geminiConfig.getFallbackModel()).thenReturn("gemini-flash-latest");
        when(geminiConfig.getApiUrl()).thenReturn("https://generativelanguage.googleapis.com/v1beta");

        aiService = new AiServiceImpl(
                geminiConfig,
                restTemplate,
                tourismPlaceRepository,
                hotelRepository,
                restaurantRepository,
                foodRepository,
                tourPackageRepository,
                promotionRepository,
                objectMapper
        );
    }

    @Test
    void chat_successWithGeminiResponse() {
        String mockGeminiJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Angkor Wat is magnificent! Make sure to catch the sunrise at 5:30 AM."
                          }
                        ],
                        "role": "model"
                      }
                    }
                  ]
                }
                """;

        when(restTemplate.exchange(
                contains("gemini-3.6-flash"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockGeminiJson, HttpStatus.OK));

        AiChatRequest request = AiChatRequest.builder()
                .message("Tell me about visiting Angkor Wat.")
                .destination("Siem Reap")
                .conversationHistory(List.of(
                        AiChatMessage.builder().role("user").content("Hello!").build(),
                        AiChatMessage.builder().role("model").content("Welcome to Cambodia!").build()
                ))
                .build();

        AiChatResponse response = aiService.chat(request);

        assertThat(response).isNotNull();
        assertThat(response.getReply()).contains("Angkor Wat is magnificent");
        assertThat(response.getModelUsed()).isEqualTo("gemini-3.6-flash");
        assertThat(response.isFallbackUsed()).isFalse();
        assertThat(response.getSuggestedFollowUps()).isNotEmpty();
    }

    @Test
    void chat_withDatabaseReferencedItems() {
        String mockGeminiJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "I recommend visiting Angkor Wat and tasting Fish Amok."
                          }
                        ],
                        "role": "model"
                      }
                    }
                  ]
                }
                """;

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockGeminiJson, HttpStatus.OK));

        TourPlaces place = new TourPlaces();
        place.setId(1L);
        place.setName("Angkor Wat");
        place.setAddress("Siem Reap");
        place.setRating(new BigDecimal("4.9"));
        place.setStaus("ACTIVE");

        Foods food = new Foods();
        food.setId(5L);
        food.setName("Fish Amok");
        food.setPrice(new BigDecimal("6.50"));
        food.setIsAvailable(true);

        when(tourismPlaceRepository.findAll()).thenReturn(List.of(place));
        when(foodRepository.findByIsAvailableTrue()).thenReturn(List.of(food));

        AiChatRequest request = AiChatRequest.builder()
                .message("What should I see and eat?")
                .build();

        AiChatResponse response = aiService.chat(request);

        assertThat(response.getReferencedItems()).isNotEmpty();
        assertThat(response.getReferencedItems())
                .anyMatch(item -> item.getName().equalsIgnoreCase("Angkor Wat") && "TOUR_PLACE".equals(item.getType()));
        assertThat(response.getReferencedItems())
                .anyMatch(item -> item.getName().equalsIgnoreCase("Fish Amok") && "FOOD".equals(item.getType()));
    }

    @Test
    void chat_gracefulFallbackWhenGeminiFails() {
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenThrow(new RestClientException("Connection timeout"));

        AiChatRequest request = AiChatRequest.builder()
                .message("Hello")
                .destination("Siem Reap")
                .build();

        AiChatResponse response = aiService.chat(request);

        assertThat(response).isNotNull();
        assertThat(response.isFallbackUsed()).isTrue();
        assertThat(response.getReply()).contains("Welcome to Siem Reap");
        assertThat(response.getSuggestedFollowUps()).isNotEmpty();
    }

    @Test
    void generateItinerary_parsesJsonAndMarkdown() throws Exception {
        String mockResponse = """
                ```json
                [
                  {
                    "dayNumber": 1,
                    "theme": "Angkor Sunrise & Classic Wonders",
                    "morningActivity": "Sunrise at Angkor Wat and visit the central sanctuary",
                    "lunchRecommendation": "Khmer traditional lunch near the temple complex",
                    "afternoonActivity": "Explore the stone faces of Bayon and Ta Prohm root temple",
                    "eveningActivity": "Sunset at Phnom Bakheng and stroll along Pub Street",
                    "dinnerRecommendation": "Amok Restaurant in Siem Reap old market area",
                    "highlights": ["Angkor Wat Sunrise", "Bayon Faces", "Ta Prohm"]
                  }
                ]
                ```
                
                ### Overview
                An unforgettable cultural journey through ancient Angkor and modern Khmer warmth.
                
                ### Tips
                - Modest clothing is mandatory for temple entry.
                - Use Bakong KHQR for fast cashless payments.
                """;

        String mockGeminiJson = String.format("""
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": %s
                          }
                        ],
                        "role": "model"
                      }
                    }
                  ]
                }
                """, objectMapper.writeValueAsString(mockResponse));

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockGeminiJson, HttpStatus.OK));

        AiItineraryRequest request = AiItineraryRequest.builder()
                .destination("Siem Reap")
                .numberOfDays(1)
                .budget("MODERATE")
                .travelStyle("CULTURE_HERITAGE")
                .build();

        AiItineraryResponse response = aiService.generateItinerary(request);

        assertThat(response).isNotNull();
        assertThat(response.getDestination()).isEqualTo("Siem Reap");
        assertThat(response.getDays()).hasSize(1);
        assertThat(response.getDays().get(0).getTheme()).isEqualTo("Angkor Sunrise & Classic Wonders");
        assertThat(response.getPracticalTips()).isNotEmpty();
        assertThat(response.getEstimatedTotalBudget()).isNotEmpty();
    }

    @Test
    void getRecommendations_returnsMatches() {
        String mockGeminiJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "For boutique stays, look for heritage boutique properties in Siem Reap."
                          }
                        ],
                        "role": "model"
                      }
                    }
                  ]
                }
                """;

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockGeminiJson, HttpStatus.OK));

        Location loc = new Location();
        loc.setProvince("Siem Reap");
        loc.setDistrict("Svay Dangkum");

        Hotels hotel = new Hotels();
        hotel.setId(10L);
        hotel.setHotelName("Heritage Boutique Resort");
        hotel.setLocation(loc);

        when(hotelRepository.findAll()).thenReturn(List.of(hotel));

        AiRecommendationRequest request = AiRecommendationRequest.builder()
                .category("HOTELS")
                .preference("quiet boutique with pool")
                .destination("Siem Reap")
                .build();

        AiRecommendationResponse response = aiService.getRecommendations(request);

        assertThat(response).isNotNull();
        assertThat(response.getCategory()).isEqualTo("HOTELS");
        assertThat(response.getAiAnalysis()).contains("boutique");
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getRecommendations().get(0).getName()).isEqualTo("Heritage Boutique Resort");
    }

    @Test
    void getStatus_configuredAndUnconfigured() {
        AiStatusResponse status = aiService.getStatus();
        assertThat(status.isConfigured()).isTrue();
        assertThat(status.getStatus()).isEqualTo("UP");
        assertThat(status.getMaskedApiKey()).contains("...");

        when(geminiConfig.isConfigured()).thenReturn(false);
        when(geminiConfig.getApiKey()).thenReturn("");

        AiStatusResponse unconfig = aiService.getStatus();
        assertThat(unconfig.isConfigured()).isFalse();
        assertThat(unconfig.getStatus()).isEqualTo("UNCONFIGURED");
    }
}
