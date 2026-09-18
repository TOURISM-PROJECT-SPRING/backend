package com.example.spring_boot_project_api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.OwnerOfferingRequest;
import com.example.spring_boot_project_api.dto.response.OwnerDashboardStatsDTO;
import com.example.spring_boot_project_api.dto.response.OwnerOfferingDTO;
import com.example.spring_boot_project_api.dto.response.UnifiedBookingResponse;
import com.example.spring_boot_project_api.model.FoodOrders;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.RoomBookings;
// import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.FoodRepository;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.RoomRepository;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.BookingNotificationService;
import com.example.spring_boot_project_api.service.OwnerDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerDashboardServiceImpl implements OwnerDashboardService {

    private final RoomBookingRepository roomBookingRepository;
    private final TicketBookingRepository ticketBookingRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final TourismPlaceRepository tourismPlaceRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final BookingNotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public OwnerDashboardStatsDTO getDashboardStats(Long ownerId) {
        Long targetOwnerId = (ownerId != null) ? ownerId : 1L;
        List<UnifiedBookingResponse> bookings = getOwnerBookings(targetOwnerId, null);

        long totalBookings = bookings.size();
        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(b.getStatus()))
                .map(UnifiedBookingResponse::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingOrders = bookings.stream()
                .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                .count();

        List<OwnerOfferingDTO> offerings = getOwnerOfferings(targetOwnerId);
        long activeServices = offerings.size();

        List<Map<String, Object>> trend = buildRevenueTrend(bookings, 6);

        String businessName = userRepository.findById(targetOwnerId)
                .map(Users::getFullname)
                .orElse("Business Operations");

        return OwnerDashboardStatsDTO.builder()
                .ownerId(targetOwnerId)
                .businessName(businessName)
                .totalBookings(totalBookings)
                .activeServices(activeServices)
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .revenueTrend(trend)
                .recentBookings(bookings.stream().limit(6).toList())
                .build();
    }

    /**
     * Aggregate real non-cancelled revenue for the last {@code months} calendar
     * months from this owner's bookings, filling missing months with zero.
     */
    private List<Map<String, Object>> buildRevenueTrend(List<UnifiedBookingResponse> bookings,
            int months) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMonths(months - 1L)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        Map<YearMonth, BigDecimal> byMonth = new TreeMap<>();
        for (UnifiedBookingResponse b : bookings) {
            if (b.getCreatedAt() == null || b.getTotalAmount() == null) continue;
            if ("CANCELLED".equalsIgnoreCase(b.getStatus())) continue;
            if (b.getCreatedAt().isBefore(start) || b.getCreatedAt().isAfter(end)) continue;
            byMonth.merge(YearMonth.from(b.getCreatedAt()), b.getTotalAmount(), BigDecimal::add);
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        YearMonth ymStart = YearMonth.from(start);
        YearMonth ymEnd = YearMonth.from(end);
        for (YearMonth ym = ymStart; !ym.isAfter(ymEnd); ym = ym.plusMonths(1)) {
            Map<String, Object> m = new HashMap<>();
            m.put("label", ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            m.put("revenue", byMonth.getOrDefault(ym, BigDecimal.ZERO));
            trend.add(m);
        }
        return trend;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnifiedBookingResponse> getOwnerBookings(Long ownerId, String status) {
        Long targetOwnerId = (ownerId != null) ? ownerId : 1L;
        List<UnifiedBookingResponse> list = new ArrayList<>();

        // 1. Hotel room bookings
        roomBookingRepository.findAll().stream()
                .filter(rb -> rb.getRooms() != null
                        && rb.getRooms().getHotels() != null
                        && rb.getRooms().getHotels().getOwner() != null
                        && rb.getRooms().getHotels().getOwner().getId().equals(targetOwnerId))
                .forEach(rb -> list.add(mapRoomBooking(rb)));

        // 2. Restaurant food orders
        foodOrderRepository.findAll().stream()
                .filter(fo -> fo.getRestuarants() != null
                        && fo.getRestuarants().getId().equals(targetOwnerId))
                .forEach(fo -> list.add(mapFoodOrder(fo)));

        // Apply status filter if specified
        List<UnifiedBookingResponse> filtered = list;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            filtered = list.stream()
                    .filter(b -> status.equalsIgnoreCase(b.getStatus()))
                    .toList();
        }

        List<UnifiedBookingResponse> result = new ArrayList<>(filtered);
        result.sort(Comparator.comparing(UnifiedBookingResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    @Override
    @Transactional
    public UnifiedBookingResponse updateBookingStatus(String bookingType, Long id, String status) {
        String newStatus = status.toUpperCase();
        UnifiedBookingResponse updatedResponse = null;

        String type = bookingType.toUpperCase();
        switch (type) {
            case "ROOM", "HB" -> {
                RoomBookings rb = roomBookingRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Room booking not found: " + id));
                rb.setStatus(newStatus);
                RoomBookings saved = roomBookingRepository.save(rb);
                updatedResponse = mapRoomBooking(saved);
            }
            case "TICKET", "TB" -> {
                TicketBookings tb = ticketBookingRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Ticket booking not found: " + id));
                tb.setStatus(newStatus);
                TicketBookings saved = ticketBookingRepository.save(tb);
                updatedResponse = mapTicketBooking(saved);
            }
            case "FOOD_ORDER", "FO" -> {
                FoodOrders fo = foodOrderRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Food order not found: " + id));
                fo.setStatus(newStatus);
                FoodOrders saved = foodOrderRepository.save(fo);
                updatedResponse = mapFoodOrder(saved);
            }
            default -> throw new IllegalArgumentException("Unsupported booking type: " + bookingType);
        }

        // Broadcast real-time status update to Admin and Owner WebSocket topics
        notificationService.notifyStatusChange(updatedResponse, newStatus);

        return updatedResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnerOfferingDTO> getOwnerOfferings(Long ownerId) {
        Long targetOwnerId = (ownerId != null) ? ownerId : 1L;
        List<OwnerOfferingDTO> list = new ArrayList<>();

        // 1. Hotels & Rooms
        List<Hotels> hotels = hotelRepository.findByOwnerId(targetOwnerId);
        if (hotels.isEmpty()) {
            hotels = hotelRepository.findAll().stream().limit(3).toList();
        }
        for (Hotels h : hotels) {
            list.add(OwnerOfferingDTO.builder()
                    .id(h.getId())
                    .name(h.getHotelName())
                    .offeringType("HOTEL")
                    .category("Accommodation")
                    .price(new BigDecimal("120"))
                    .description(h.getPhoneContact() != null ? "Contact: " + h.getPhoneContact() : "Luxury Hotel Stay")
                    .imageUrl(null)
                    .isAvailable(true)
                    .referenceId(h.getId())
                    .status("Active")
                    .build());
        }

        // 2. Tour Places
        List<TourPlaces> tours = tourismPlaceRepository.findByUserId(targetOwnerId);
        if (tours.isEmpty()) {
            tours = tourismPlaceRepository.findAll().stream().limit(3).toList();
        }
        for (TourPlaces t : tours) {
            list.add(OwnerOfferingDTO.builder()
                    .id(t.getId())
                    .name(t.getName())
                    .offeringType("TOUR")
                    .category("Experience")
                    .price(new BigDecimal("45"))
                    .description(t.getDescription())
                    .imageUrl(null)
                    .isAvailable(true)
                    .referenceId(t.getId())
                    .status(t.getStaus() != null ? t.getStaus() : "Active")
                    .build());
        }

        // 3. Foods / Menu Items
        List<Foods> foods = foodRepository.findAll().stream().limit(6).toList();
        for (Foods f : foods) {
            list.add(OwnerOfferingDTO.builder()
                    .id(f.getId())
                    .name(f.getName())
                    .offeringType("FOOD")
                    .category(f.getFoodCategories() != null ? f.getFoodCategories().getName() : "Menu Item")
                    .price(f.getPrice())
                    .description(null)
                    .imageUrl(f.getImage())
                    .isAvailable(f.getIsAvailable() != null ? f.getIsAvailable() : true)
                    .referenceId(f.getId())
                    .status(Boolean.TRUE.equals(f.getIsAvailable()) ? "Available" : "Unavailable")
                    .build());
        }

        return list;
    }

    @Override
    @Transactional
    public OwnerOfferingDTO createOffering(Long ownerId, OwnerOfferingRequest request) {
        String type = request.getOfferingType().toUpperCase();
        switch (type) {
            case "FOOD" -> {
                Foods food = new Foods();
                food.setName(request.getName());
                food.setPrice(request.getPrice());
                food.setImage(request.getImageUrl() != null ? request.getImageUrl() : "default.jpg");
                food.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
                Restaurants rest = restaurantRepository.findAll().stream().findFirst().orElse(null);
                food.setRestaurants(rest);
                Foods saved = foodRepository.save(food);
                return OwnerOfferingDTO.builder()
                        .id(saved.getId())
                        .name(saved.getName())
                        .offeringType("FOOD")
                        .category(request.getCategory() != null ? request.getCategory() : "Main Dish")
                        .price(saved.getPrice())
                        .description(request.getDescription())
                        .imageUrl(saved.getImage())
                        .isAvailable(saved.getIsAvailable())
                        .status("Available")
                        .build();
            }
            default -> {
                return OwnerOfferingDTO.builder()
                        .id(System.currentTimeMillis())
                        .name(request.getName())
                        .offeringType(request.getOfferingType())
                        .category(request.getCategory() != null ? request.getCategory() : "General")
                        .price(request.getPrice())
                        .description(request.getDescription())
                        .imageUrl(request.getImageUrl())
                        .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                        .status("Active")
                        .build();
            }
        }
    }

    @Override
    @Transactional
    public OwnerOfferingDTO toggleOfferingAvailability(String offeringType, Long id, Boolean isAvailable) {
        boolean available = (isAvailable != null) ? isAvailable : true;
        if ("FOOD".equalsIgnoreCase(offeringType)) {
            foodRepository.findById(id).ifPresent(f -> {
                f.setIsAvailable(available);
                foodRepository.save(f);
            });
        }
        return OwnerOfferingDTO.builder()
                .id(id)
                .offeringType(offeringType)
                .isAvailable(available)
                .status(available ? "Available" : "Unavailable")
                .build();
    }

    @Override
    @Transactional
    public void deleteOffering(String offeringType, Long id) {
        if ("FOOD".equalsIgnoreCase(offeringType)) {
            foodRepository.deleteById(id);
        } else if ("ROOM".equalsIgnoreCase(offeringType)) {
            roomRepository.deleteById(id);
        } else if ("TOUR".equalsIgnoreCase(offeringType)) {
            tourismPlaceRepository.deleteById(id);
        }
    }

    private UnifiedBookingResponse mapRoomBooking(RoomBookings rb) {
        Long ownerId = null;
        String hotelName = "Hotel Stay";
        if (rb.getRooms() != null && rb.getRooms().getHotels() != null) {
            hotelName = rb.getRooms().getHotels().getHotelName();
            if (rb.getRooms().getHotels().getOwner() != null) {
                ownerId = rb.getRooms().getHotels().getOwner().getId();
            }
        }
        return UnifiedBookingResponse.builder()
                .id("HB-" + rb.getId())
                .rawId(rb.getId())
                .bookingType("ROOM")
                .customerName(rb.getUsers() != null ? rb.getUsers().getFullname() : "Guest")
                .customerEmail(rb.getUsers() != null ? rb.getUsers().getEmail() : null)
                .serviceName(hotelName)
                .ownerId(ownerId)
                .totalAmount(rb.getAmount())
                .quantity(rb.getNumGuest())
                .status(rb.getStatus())
                .paymentMethod(rb.getPaymentMethod())
                .bookingDate(rb.getCheckIn() != null ? rb.getCheckIn().toString() : null)
                .createdAt(rb.getCreatedAt() != null ? rb.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    private UnifiedBookingResponse mapTicketBooking(TicketBookings tb) {
        String placeName = "Tour Experience";
        if (tb.getTickets() != null && tb.getTickets().getTourPlaces() != null) {
            placeName = tb.getTickets().getTourPlaces().getName();
        }
        return UnifiedBookingResponse.builder()
                .id("TB-" + tb.getId())
                .rawId(tb.getId())
                .bookingType("TICKET")
                .customerName(tb.getUser() != null ? tb.getUser().getFullname() : "Guest")
                .customerEmail(tb.getUser() != null ? tb.getUser().getEmail() : null)
                .serviceName(placeName)
                .ownerId(1L)
                .totalAmount(tb.getTotalPrice())
                .quantity(tb.getQuantity())
                .status(tb.getStatus())
                .paymentMethod(tb.getPaymentMethod())
                .bookingDate(tb.getVisiDate() != null ? tb.getVisiDate().toString() : null)
                .createdAt(tb.getCreatedAt() != null ? tb.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    private UnifiedBookingResponse mapFoodOrder(FoodOrders fo) {
        String restName = "Restaurant Order";
        Long ownerId = null;
        if (fo.getRestuarants() != null) {
            restName = fo.getRestuarants().getName();
            ownerId = fo.getRestuarants().getId();
        }
        return UnifiedBookingResponse.builder()
                .id("FO-" + fo.getId())
                .rawId(fo.getId())
                .bookingType("FOOD_ORDER")
                .customerName(fo.getUser() != null ? fo.getUser().getFullname() : "Guest")
                .customerEmail(fo.getUser() != null ? fo.getUser().getEmail() : null)
                .serviceName(restName)
                .ownerId(ownerId)
                .totalAmount(fo.getTotalPrice())
                .quantity(fo.getFoodOrderItems() != null ? fo.getFoodOrderItems().size() : 1)
                .status(fo.getStatus())
                .createdAt(fo.getCreatedAt() != null ? fo.getCreatedAt() : LocalDateTime.now())
                .build();
    }
}
