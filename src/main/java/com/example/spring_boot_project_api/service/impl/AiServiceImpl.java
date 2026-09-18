package com.example.spring_boot_project_api.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.example.spring_boot_project_api.config.GeminiConfig;
import com.example.spring_boot_project_api.dto.request.AiChatMessage;
import com.example.spring_boot_project_api.dto.request.AiChatRequest;
import com.example.spring_boot_project_api.dto.request.AiItineraryRequest;
import com.example.spring_boot_project_api.dto.request.AiRecommendationRequest;
import com.example.spring_boot_project_api.dto.response.AiChatResponse;
import com.example.spring_boot_project_api.dto.response.AiDayPlanDto;
import com.example.spring_boot_project_api.dto.response.AiItineraryResponse;
import com.example.spring_boot_project_api.dto.response.AiRecommendationResponse;
import com.example.spring_boot_project_api.dto.response.AiStatusResponse;
import com.example.spring_boot_project_api.dto.response.ReferencedItemDto;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Promotions;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.TourPackages;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.repository.FoodRepository;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.PromotionRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.TourPackageRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.service.AiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final TourismPlaceRepository tourismPlaceRepository;
    private final HotelRepository hotelRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;
    private final TourPackageRepository tourPackageRepository;
    private final PromotionRepository promotionRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AiServiceImpl(
            GeminiConfig geminiConfig,
            @Qualifier("geminiRestTemplate") RestTemplate restTemplate,
            TourismPlaceRepository tourismPlaceRepository,
            HotelRepository hotelRepository,
            RestaurantRepository restaurantRepository,
            FoodRepository foodRepository,
            TourPackageRepository tourPackageRepository,
            PromotionRepository promotionRepository) {
        this(geminiConfig, restTemplate, tourismPlaceRepository, hotelRepository,
                restaurantRepository, foodRepository, tourPackageRepository, promotionRepository,
                new ObjectMapper());
    }

    public AiServiceImpl(
            GeminiConfig geminiConfig,
            RestTemplate restTemplate,
            TourismPlaceRepository tourismPlaceRepository,
            HotelRepository hotelRepository,
            RestaurantRepository restaurantRepository,
            FoodRepository foodRepository,
            TourPackageRepository tourPackageRepository,
            PromotionRepository promotionRepository,
            ObjectMapper objectMapper) {
        this.geminiConfig = geminiConfig;
        this.restTemplate = restTemplate;
        this.tourismPlaceRepository = tourismPlaceRepository;
        this.hotelRepository = hotelRepository;
        this.restaurantRepository = restaurantRepository;
        this.foodRepository = foodRepository;
        this.tourPackageRepository = tourPackageRepository;
        this.promotionRepository = promotionRepository;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        String userMessage = request.getMessage().trim();
        String destination = request.getDestination();

        // 1. Build context from live database
        String databaseGrounding = buildDatabaseGroundingContext(destination, userMessage);

        // 2. Prepare system prompt
        String systemPrompt = buildSystemPrompt(databaseGrounding);

        // 3. Prepare message turns for Gemini
        List<Map<String, Object>> contents = new ArrayList<>();

        if (request.getConversationHistory() != null) {
            for (AiChatMessage turn : request.getConversationHistory()) {
                if (turn.getContent() != null && !turn.getContent().trim().isEmpty()) {
                    String role = normalizeRole(turn.getRole());
                    contents.add(createContentNode(role, turn.getContent().trim()));
                }
            }
        }
        contents.add(createContentNode("user", userMessage));

        // 4. Call Gemini API (with fallback model support)
        GeminiExecutionResult result = callGeminiWithFallback(systemPrompt, contents);

        String replyText = result.text();
        boolean fallback = result.fallbackUsed();
        String modelUsed = result.modelUsed();

        if (replyText == null || replyText.trim().isEmpty()) {
            replyText = generateFallbackChatReply(userMessage, destination);
            fallback = true;
        }

        // 5. Detect referenced database items
        List<ReferencedItemDto> referenced = matchReferencedItems(replyText, userMessage, destination);

        // 6. Generate dynamic follow-up suggestions
        List<String> followUps = generateFollowUps(destination, userMessage);

        return AiChatResponse.builder()
                .reply(replyText)
                .suggestedFollowUps(followUps)
                .referencedItems(referenced)
                .modelUsed(modelUsed)
                .fallbackUsed(fallback)
                .build();
    }

    @Override
    public AiItineraryResponse generateItinerary(AiItineraryRequest request) {
        String destination = request.getDestination().trim();
        int days = request.getNumberOfDays() != null ? Math.max(1, Math.min(14, request.getNumberOfDays())) : 3;
        String budget = request.getBudget() != null ? request.getBudget() : "MODERATE";
        String style = request.getTravelStyle() != null ? request.getTravelStyle() : "CULTURE_HERITAGE";
        String group = request.getGroupType() != null ? request.getGroupType() : "COUPLE";
        String specialRequests = request.getSpecialRequests() != null ? request.getSpecialRequests() : "None";

        String databaseGrounding = buildDatabaseGroundingContext(destination, "");

        String systemPrompt = buildItinerarySystemPrompt(databaseGrounding);

        String promptText = String.format(
                "Create a comprehensive %d-day itinerary for %s, Cambodia.\n" +
                "Travel details:\n" +
                "- Budget Level: %s\n" +
                "- Travel Style: %s\n" +
                "- Group Type: %s\n" +
                "- Special Requests / Notes: %s\n\n" +
                "Format your response with two parts:\n" +
                "1. A JSON block enclosed in ```json ... ``` representing an array of days:\n" +
                "[\n" +
                "  {\n" +
                "    \"dayNumber\": 1,\n" +
                "    \"theme\": \"Day title or theme\",\n" +
                "    \"morningActivity\": \"Details for morning\",\n" +
                "    \"lunchRecommendation\": \"Where or what to eat for lunch\",\n" +
                "    \"afternoonActivity\": \"Details for afternoon\",\n" +
                "    \"eveningActivity\": \"Details for evening/sunset/nightlife\",\n" +
                "    \"dinnerRecommendation\": \"Where or what to eat for dinner\",\n" +
                "    \"highlights\": [\"Highlight 1\", \"Highlight 2\"]\n" +
                "  }\n" +
                "]\n\n" +
                "2. A detailed, beautifully formatted Markdown guide following the JSON block with Trip Overview, Daily Highlights, Local Food Picks, and Practical Tips (dress code, currency/Bakong KHQR payment tips, transport).",
                days, destination, budget, style, group, specialRequests
        );

        List<Map<String, Object>> contents = Collections.singletonList(createContentNode("user", promptText));
        GeminiExecutionResult result = callGeminiWithFallback(systemPrompt, contents);

        String rawResponse = result.text();
        boolean fallback = result.fallbackUsed();
        String modelUsed = result.modelUsed();

        List<AiDayPlanDto> dayPlans = extractDayPlansFromJson(rawResponse);
        String overview = extractSection(rawResponse, "Overview");
        if (overview.isEmpty()) {
            overview = String.format("A handcrafted %d-day %s journey through %s, Cambodia.", days, style.toLowerCase().replace('_', ' '), destination);
        }

        List<String> tips = extractPracticalTips(rawResponse, destination);
        List<ReferencedItemDto> recommendedItems = matchReferencedItems(rawResponse, destination, destination);

        if (dayPlans.isEmpty()) {
            dayPlans = generateFallbackDayPlans(destination, days);
        }

        return AiItineraryResponse.builder()
                .destination(destination)
                .title(String.format("%d-Day Ultimate %s Adventure", days, destination))
                .overview(overview)
                .numberOfDays(days)
                .budgetLevel(budget)
                .estimatedTotalBudget(estimateBudget(budget, days))
                .days(dayPlans)
                .practicalTips(tips)
                .recommendedDatabaseItems(recommendedItems)
                .fullMarkdownItinerary(rawResponse)
                .modelUsed(modelUsed)
                .fallbackUsed(fallback)
                .build();
    }

    @Override
    public AiRecommendationResponse getRecommendations(AiRecommendationRequest request) {
        String category = request.getCategory() != null ? request.getCategory().toUpperCase() : "ALL";
        String preference = request.getPreference().trim();
        String destination = request.getDestination();

        String databaseGrounding = buildDatabaseGroundingContext(destination, preference);

        String prompt = String.format(
                "Based on the traveler's preference: \"%s\" (Category: %s, Destination: %s),\n" +
                "analyze our platform's offerings and provide top recommendations.\n" +
                "Highlight why each match suits their taste, estimated pricing, and insider tips.",
                preference, category, destination != null ? destination : "Cambodia"
        );

        String systemPrompt = "You are Sovann, the Smart Tourism AI Assistant. Provide accurate, insightful, and appealing recommendations for visitors in Cambodia. Emphasize authentic local experiences.";

        List<Map<String, Object>> contents = Collections.singletonList(createContentNode("user", prompt));
        GeminiExecutionResult result = callGeminiWithFallback(systemPrompt, contents);

        String analysis = result.text();
        if (analysis == null || analysis.trim().isEmpty()) {
            analysis = "Here are our top curated recommendations matching your preferences:";
        }

        List<ReferencedItemDto> matches = matchRecommendationsFromDatabase(category, preference, destination);

        return AiRecommendationResponse.builder()
                .category(category)
                .userPreference(preference)
                .destination(destination)
                .aiAnalysis(analysis)
                .recommendations(matches)
                .modelUsed(result.modelUsed())
                .fallbackUsed(result.fallbackUsed())
                .build();
    }

    @Override
    public AiStatusResponse getStatus() {
        boolean configured = geminiConfig.isConfigured();
        String maskedKey = maskApiKey(geminiConfig.getApiKey());
        String model = geminiConfig.getModel();
        String fallbackModel = geminiConfig.getFallbackModel();

        if (!configured) {
            return AiStatusResponse.builder()
                    .configured(false)
                    .model(model)
                    .fallbackModel(fallbackModel)
                    .status("UNCONFIGURED")
                    .message("Gemini API key is not configured in .env (GERMINI_API_KEY / GEMINI_API_KEY).")
                    .maskedApiKey(null)
                    .build();
        }

        return AiStatusResponse.builder()
                .configured(true)
                .model(model)
                .fallbackModel(fallbackModel)
                .status("UP")
                .message("Gemini AI service is online and ready.")
                .maskedApiKey(maskedKey)
                .build();
    }

    // ==========================================
    // Internal Gemini Invocation & Resilience
    // ==========================================

    private record GeminiExecutionResult(String text, String modelUsed, boolean fallbackUsed) {}

    private GeminiExecutionResult callGeminiWithFallback(String systemInstruction, List<Map<String, Object>> contents) {
        if (!geminiConfig.isConfigured()) {
            log.warn("Gemini API key is not configured. Falling back to local generation.");
            return new GeminiExecutionResult(null, "local-fallback", true);
        }

        String primaryModel = geminiConfig.getModel();
        String fallbackModel = geminiConfig.getFallbackModel();

        // 1. Try primary model
        try {
            String response = executeGeminiCall(primaryModel, systemInstruction, contents);
            if (response != null && !response.trim().isEmpty()) {
                return new GeminiExecutionResult(response, primaryModel, false);
            }
        } catch (Exception e) {
            log.warn("Gemini primary model ({}) failed: {}. Trying fallback model ({})", primaryModel, e.getMessage(), fallbackModel);
        }

        // 2. Try fallback model if different
        if (fallbackModel != null && !fallbackModel.equalsIgnoreCase(primaryModel)) {
            try {
                String response = executeGeminiCall(fallbackModel, systemInstruction, contents);
                if (response != null && !response.trim().isEmpty()) {
                    log.info("Successfully executed with fallback model: {}", fallbackModel);
                    return new GeminiExecutionResult(response, fallbackModel, true);
                }
            } catch (Exception e) {
                log.error("Gemini fallback model ({}) also failed: {}", fallbackModel, e.getMessage());
            }
        }

        return new GeminiExecutionResult(null, "local-fallback", true);
    }

    private String executeGeminiCall(String model, String systemInstruction, List<Map<String, Object>> contents) {
        String url = String.format("%s/models/%s:generateContent?key=%s",
                geminiConfig.getApiUrl(), model, geminiConfig.getApiKey());

        Map<String, Object> payload = new HashMap<>();

        if (systemInstruction != null && !systemInstruction.trim().isEmpty()) {
            Map<String, Object> sysInstruction = new HashMap<>();
            sysInstruction.put("parts", Collections.singletonList(Collections.singletonMap("text", systemInstruction)));
            payload.put("systemInstruction", sysInstruction);
        }

        payload.put("contents", contents);

        Map<String, Object> genConfig = new HashMap<>();
        genConfig.put("temperature", 0.7);
        genConfig.put("maxOutputTokens", 2500);
        payload.put("generationConfig", genConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return extractTextFromGeminiResponse(response.getBody());
        }

        return null;
    }

    private String extractTextFromGeminiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response JSON: {}", e.getMessage());
        }
        return null;
    }

    // ==========================================
    // Context Grounding & Prompts
    // ==========================================

    private String buildDatabaseGroundingContext(String destination, String keyword) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LIVE DATABASE CATALOG ===\n");

        try {
            // Tour Places
            List<TourPlaces> places = tourismPlaceRepository.findAll();
            if (!places.isEmpty()) {
                sb.append("Top Attractions / Tour Places in our system:\n");
                places.stream().limit(10).forEach(p -> {
                    sb.append(String.format("- [Attraction ID:%d] %s (Address: %s, Rating: %s, Status: %s)\n",
                            p.getId(), p.getName(), p.getAddress(), p.getRating(), p.getStaus()));
                });
            }

            // Hotels
            List<Hotels> hotels = hotelRepository.findAll();
            if (!hotels.isEmpty()) {
                sb.append("\nTop Hotels:\n");
                hotels.stream().limit(8).forEach(h -> {
                    String loc = h.getLocation() != null ? (h.getLocation().getDistrict() + ", " + h.getLocation().getProvince()) : "Cambodia";
                    sb.append(String.format("- [Hotel ID:%d] %s (Location: %s, Phone: %s)\n",
                            h.getId(), h.getHotelName(), loc, h.getPhoneContact()));
                });
            }

            // Restaurants
            List<Restaurants> restaurants = restaurantRepository.findAll();
            if (!restaurants.isEmpty()) {
                sb.append("\nTop Restaurants:\n");
                restaurants.stream().limit(8).forEach(r -> {
                    sb.append(String.format("- [Restaurant ID:%d] %s (Hours: %s - %s)\n",
                            r.getId(), r.getName(), r.getOpenTime(), r.getClossTime()));
                });
            }

            // Foods
            List<Foods> foods = foodRepository.findByIsAvailableTrue();
            if (!foods.isEmpty()) {
                sb.append("\nPopular Dishes & Foods:\n");
                foods.stream().limit(10).forEach(f -> {
                    sb.append(String.format("- [Food ID:%d] %s ($%s)\n",
                            f.getId(), f.getName(), f.getPrice()));
                });
            }

            // Tour Packages
            List<TourPackages> packages = tourPackageRepository.findAll();
            if (!packages.isEmpty()) {
                sb.append("\nAvailable Tour Packages:\n");
                packages.stream().limit(6).forEach(pkg -> {
                    sb.append(String.format("- [TourPackage ID:%d] %s (%d days, $%s, max %d people)\n",
                            pkg.getId(), pkg.getName(), pkg.getDurationDays(), pkg.getPrice(), pkg.getMaxPeople()));
                });
            }

            // Active Promotions
            List<Promotions> promotions = promotionRepository.findAll();
            if (!promotions.isEmpty()) {
                sb.append("\nActive Promotions & Deals:\n");
                promotions.stream().limit(5).forEach(promo -> {
                    sb.append(String.format("- [Promo Code:%s] %s (%s off %s)\n",
                            promo.getCode(), promo.getName(), promo.getDiscountValue(), promo.getDiscountType()));
                });
            }
        } catch (Exception e) {
            log.warn("Could not load all database context for grounding: {}", e.getMessage());
        }

        sb.append("=============================\n");
        return sb.toString();
    }

    private String buildSystemPrompt(String databaseGrounding) {
        return "You are 'Sovann', an expert Cambodian Smart Tourism AI Concierge.\n" +
                "If user say Hi, Hello, Hey, Yo, what's up etc. answer Hello i am Sovann from cambodia, How can i help you?" +
                "Your mission is to provide delightful, culturally rich, and highly accurate travel advice for visitors exploring Cambodia (Siem Reap, Phnom Penh, Battambang, Kampot, Kep, Koh Rong, Mondulkiri, etc.) but only answer when they're asking.\n\n" +
                "GUIDELINES:\n" +
                "1. Be warm, welcoming, polite, and enthusiastic in short not too long (Khmer hospitality).\n" +
                "2. Response short! only (2-5 sentences) per response" +
                "3. Only answer with with what user asks"+
                "4. If user ask about Tourists, only answer tourist" +
                "5. If user ask about Hotels, only answer hotel" +
                "6. If user ask about restaurant, only answer about restaurant & food" +
                "7. If user ask about food, answer about food & restaurant" +
                "8. If user ask how to booking, tell them how to booking" +
                "9. Reference the real attractions, hotels, restaurants, foods, tour packages, and active promotions from our platform's database whenever relevant.\n" +
                "10. Provide practical local travel tips:\n" +
                "   - Temple dress code: Modest attire covering shoulders and knees (strictly required for Angkor Wat and Royal Palace).\n" +
                "   - Currency: USD and Cambodian Riel (KHR) are widely accepted. Mention that our platform supports instant NBC Bakong KHQR digital payment!\n" +
                "   - Local transport: Recommend PassApp or Grab for reliable tuk-tuks and taxis.\n" +
                "   - Weather: Advise on hydration and sun protection.\n" +
                "11. Structure your responses with clear Markdown headings, bullet points, and emojis for high readability.\n\n" +
                databaseGrounding;
    }

    private String buildItinerarySystemPrompt(String databaseGrounding) {
        return "You are 'Sovann', an elite Cambodian Travel Architect.\n" +
                "You design optimal, stress-free day-by-day itineraries tailored to traveler pace, budget, and interests.\n" +
                "Ground your recommendations with the real entities in our database where applicable.\n" +
                "Ensure logical geographical flow so travelers don't spend unnecessary time commuting.\n\n" +
                databaseGrounding;
    }

    // ==========================================
    // Extraction & Helpers
    // ==========================================

    private Map<String, Object> createContentNode(String role, String text) {
        Map<String, Object> node = new HashMap<>();
        node.put("role", role);
        node.put("parts", Collections.singletonList(Collections.singletonMap("text", text)));
        return node;
    }

    private String normalizeRole(String role) {
        if (role == null) return "user";
        String r = role.trim().toLowerCase();
        if (r.equals("model") || r.equals("assistant") || r.equals("bot") || r.equals("ai")) {
            return "model";
        }
        return "user";
    }

    private List<AiDayPlanDto> extractDayPlansFromJson(String raw) {
        if (raw == null) return Collections.emptyList();
        Pattern jsonPattern = Pattern.compile("```json\\s*(\\[.*?\\])\\s*```", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(raw);
        if (matcher.find()) {
            String jsonArray = matcher.group(1);
            try {
                return objectMapper.readValue(jsonArray, new TypeReference<List<AiDayPlanDto>>() {});
            } catch (Exception e) {
                log.warn("Could not parse extracted JSON array to List<AiDayPlanDto>: {}", e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    private String extractSection(String text, String sectionName) {
        if (text == null) return "";
        Pattern pattern = Pattern.compile("(?i)(?:###|##|#)?\\s*" + Pattern.quote(sectionName) + "[:\\s]*([\\s\\S]*?)(?=(?:###|##|#)|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private List<String> extractPracticalTips(String text, String destination) {
        List<String> tips = new ArrayList<>();
        tips.add("Dress Code: Cover shoulders and knees when visiting temples and sacred sites.");
        tips.add("Payments: Cash (USD/KHR) and NBC Bakong KHQR are widely accepted on our platform and across Cambodia.");
        tips.add("Getting Around: Use PassApp or Grab for quick, transparent tuk-tuk fares.");
        tips.add("Hydration: Keep a reusable water bottle handy; tropical heat is strong between 11 AM and 3 PM.");
        return tips;
    }

    private String estimateBudget(String budgetLevel, int days) {
        int daily = switch (budgetLevel.toUpperCase()) {
            case "BUDGET" -> 35;
            case "LUXURY" -> 250;
            default -> 85;
        };
        int total = daily * days;
        return String.format("$%d - $%d USD", total, (int) (total * 1.3));
    }

    private List<AiDayPlanDto> generateFallbackDayPlans(String destination, int days) {
        List<AiDayPlanDto> list = new ArrayList<>();
        for (int i = 1; i <= days; i++) {
            list.add(AiDayPlanDto.builder()
                    .dayNumber(i)
                    .theme(i == 1 ? "Iconic Landmarks & Welcome Tour" : (i == 2 ? "Cultural Discovery & Local Flavors" : "Hidden Gems & Leisure"))
                    .morningActivity(String.format("Explore top landmarks and scenic sights in %s.", destination))
                    .lunchRecommendation("Savor traditional Khmer dishes like Fish Amok or Beef Lok Lak.")
                    .afternoonActivity(String.format("Visit local artisan workshops, markets, or cultural centers in %s.", destination))
                    .eveningActivity("Catch a breathtaking sunset, followed by a stroll through the bustling night market.")
                    .dinnerRecommendation("Enjoy authentic dining with local hospitality.")
                    .highlights(List.of("Scenic Discovery", "Khmer Culinary Delights"))
                    .build());
        }
        return list;
    }

    private List<ReferencedItemDto> matchReferencedItems(String text, String userMessage, String destination) {
        List<ReferencedItemDto> result = new ArrayList<>();
        String combined = (text != null ? text : "") + " " + (userMessage != null ? userMessage : "");
        String combinedLower = combined.toLowerCase();

        try {
            // Check places
            List<TourPlaces> places = tourismPlaceRepository.findAll();
            for (TourPlaces p : places) {
                if (p.getName() != null && combinedLower.contains(p.getName().toLowerCase())) {
                    result.add(ReferencedItemDto.builder()
                            .id(p.getId())
                            .type("TOUR_PLACE")
                            .name(p.getName())
                            .description(p.getDescription())
                            .location(p.getAddress())
                            .rating(p.getRating())
                            .build());
                    if (result.size() >= 5) break;
                }
            }

            // Check foods
            List<Foods> foods = foodRepository.findByIsAvailableTrue();
            for (Foods f : foods) {
                if (f.getName() != null && combinedLower.contains(f.getName().toLowerCase())) {
                    result.add(ReferencedItemDto.builder()
                            .id(f.getId())
                            .type("FOOD")
                            .name(f.getName())
                            .price(f.getPrice())
                            .imageUrl(f.getImage())
                            .build());
                    if (result.size() >= 8) break;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to match referenced database items: {}", e.getMessage());
        }

        return result;
    }

    private List<ReferencedItemDto> matchRecommendationsFromDatabase(String category, String preference, String destination) {
        List<ReferencedItemDto> items = new ArrayList<>();
        String pref = preference.toLowerCase();

        try {
            if ("PLACES".equalsIgnoreCase(category) || "ALL".equalsIgnoreCase(category)) {
                List<TourPlaces> places = tourismPlaceRepository.findAll();
                places.stream()
                        .filter(p -> p.getName() != null && (p.getName().toLowerCase().contains(pref) || pref.contains(p.getName().toLowerCase()) || destination != null))
                        .limit(4)
                        .forEach(p -> items.add(ReferencedItemDto.builder()
                                .id(p.getId())
                                .type("TOUR_PLACE")
                                .name(p.getName())
                                .description(p.getDescription())
                                .location(p.getAddress())
                                .rating(p.getRating())
                                .build()));
            }

            if ("HOTELS".equalsIgnoreCase(category) || "ALL".equalsIgnoreCase(category)) {
                List<Hotels> hotels = hotelRepository.findAll();
                hotels.stream().limit(4).forEach(h -> items.add(ReferencedItemDto.builder()
                        .id(h.getId())
                        .type("HOTEL")
                        .name(h.getHotelName())
                        .location(h.getLocation() != null ? (h.getLocation().getDistrict() + ", " + h.getLocation().getProvince()) : null)
                        .description("Boutique hospitality with top-rated comforts.")
                        .build()));
            }

            if ("FOODS".equalsIgnoreCase(category) || "RESTAURANTS".equalsIgnoreCase(category) || "ALL".equalsIgnoreCase(category)) {
                List<Foods> foods = foodRepository.findByIsAvailableTrue();
                foods.stream().limit(4).forEach(f -> items.add(ReferencedItemDto.builder()
                        .id(f.getId())
                        .type("FOOD")
                        .name(f.getName())
                        .price(f.getPrice())
                        .imageUrl(f.getImage())
                        .build()));
            }
        } catch (Exception e) {
            log.warn("Could not query recommendations: {}", e.getMessage());
        }

        return items;
    }

    private List<String> generateFollowUps(String destination, String message) {
        List<String> list = new ArrayList<>();
        String loc = (destination != null && !destination.trim().isEmpty()) ? destination : "Cambodia";
        list.add(String.format("What are the best boutique hotels in %s?", loc));
        list.add(String.format("Recommend must-try street foods and restaurants in %s", loc));
        list.add(String.format("How do I pay with Bakong KHQR while traveling in %s?", loc));
        list.add(String.format("Plan a 3-day cultural itinerary for %s", loc));
        return list;
    }

    private String generateFallbackChatReply(String userMessage, String destination) {
        String loc = destination != null ? destination : "Cambodia";
        return String.format(
                "### Welcome to %s! 🇰🇭\n\n" +
                "I am **Sovann**, your Smart Tourism AI Concierge. While I am currently connecting to live AI telemetry, here is some great advice for your inquiry:\n\n" +
                "- **Must-See Highlights:** Don't miss exploring Angkor Archaeological Park, the Royal Palace, and vibrant riverside promenades.\n" +
                "- **Culinary Delights:** Savor authentic **Fish Amok**, **Beef Lok Lak**, and iced Khmer coffee.\n" +
                "- **Practical Advice:** Dress respectfully (covering shoulders and knees) when touring temples, and take advantage of instant **Bakong KHQR** QR payments across local merchants and our booking platform!\n\n" +
                "How else may I help tailor your dream Cambodia trip?", loc);
    }

    private String maskApiKey(String key) {
        if (key == null || key.length() < 8) return "********";
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}
