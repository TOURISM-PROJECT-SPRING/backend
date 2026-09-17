package com.example.spring_boot_project_api.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.BookingOrderRequest;
import com.example.spring_boot_project_api.dto.response.BookingFeedPageResponse;
import com.example.spring_boot_project_api.dto.response.UnifiedBookingResponse;
import com.example.spring_boot_project_api.model.FoodOrders;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.RoomBookings;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.model.Tickets;
import com.example.spring_boot_project_api.model.TourBookings;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.RoomRepository;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TicketRepository;
import com.example.spring_boot_project_api.repository.TourBookingRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.AdminOwnerBookingService;
import com.example.spring_boot_project_api.service.BookingNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOwnerBookingServiceImpl implements AdminOwnerBookingService {

    private final RoomBookingRepository roomBookingRepository;
    private final TicketBookingRepository ticketBookingRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final TourBookingRepository tourBookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final TicketRepository ticketRepository;
    private final RestaurantRepository restaurantRepository;
    private final BookingNotificationService notificationService;

    @Override
    @Transactional
    public UnifiedBookingResponse createBooking(BookingOrderRequest request) {
        log.info("Processing unified booking request: type={}, customer={}, amount={}",
                request.getBookingType(), request.getCustomerName(), request.getTotalAmount());

        Users customer = resolveUser(request);
        UnifiedBookingResponse response;

        String type = request.getBookingType().toUpperCase();
        switch (type) {
            case "ROOM" -> {
                Rooms room = resolveRoom(request.getReferenceId());
                RoomBookings rb = new RoomBookings();
                rb.setUsers(customer);
                rb.setRooms(room);
                rb.setNumGuest(request.getQuantity() != null ? request.getQuantity() : 1);
                rb.setCheckIn(parseDate(request.getBookingDate()));
                rb.setCheckOut(parseDate(request.getBookingDate()).plusDays(1));
                rb.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "BAKONG_KHQR");
                rb.setAmount(request.getTotalAmount());
                rb.setStatus("PENDING");
                RoomBookings saved = roomBookingRepository.save(rb);
                response = mapRoomBooking(saved);
            }
            case "TICKET" -> {
                Tickets ticket = resolveTicket(request.getReferenceId());
                TicketBookings tb = new TicketBookings();
                tb.setUser(customer);
                tb.setTickets(ticket);
                tb.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
                tb.setVisiDate(parseDate(request.getBookingDate()));
                tb.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "BAKONG_KHQR");
                tb.setTotalPrice(request.getTotalAmount());
                tb.setStatus("PENDING");
                TicketBookings saved = ticketBookingRepository.save(tb);
                response = mapTicketBooking(saved);
            }
            case "FOOD_ORDER" -> {
                Restaurants restaurant = resolveRestaurant(request.getReferenceId());
                FoodOrders fo = new FoodOrders();
                fo.setUser(customer);
                fo.setRestuarants(restaurant);
                fo.setTotalPrice(request.getTotalAmount());
                fo.setPickupTime(LocalDateTime.now().plusHours(1));
                fo.setStatus("PENDING");
                FoodOrders saved = foodOrderRepository.save(fo);
                response = mapFoodOrder(saved);
            }
            default -> {
                response = UnifiedBookingResponse.builder()
                        .id("ORD-" + System.currentTimeMillis())
                        .bookingType(request.getBookingType())
                        .customerName(request.getCustomerName())
                        .customerEmail(request.getCustomerEmail())
                        .customerPhone(request.getCustomerPhone())
                        .serviceName(request.getServiceName() != null ? request.getServiceName() : "Tourism Service")
                        .ownerId(request.getOwnerId() != null ? request.getOwnerId() : 1L)
                        .totalAmount(request.getTotalAmount())
                        .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                        .status("PENDING")
                        .paymentMethod(request.getPaymentMethod())
                        .bookingDate(request.getBookingDate())
                        .createdAt(LocalDateTime.now())
                        .items(request.getItems())
                        .build();
            }
        }

        // Trigger real-time WebSocket notifications to Admin and Owner
        notificationService.notifyNewBooking(response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingFeedPageResponse getAdminBookings(String type, String status, String search,
            int page, int size) {
        List<UnifiedBookingResponse> all = new ArrayList<>();
        roomBookingRepository.findAll().forEach(rb -> all.add(mapRoomBooking(rb)));
        ticketBookingRepository.findAll().forEach(tb -> all.add(mapTicketBooking(tb)));
        foodOrderRepository.findAll().forEach(fo -> all.add(mapFoodOrder(fo)));
        tourBookingRepository.findAll().forEach(tb -> all.add(mapTourBooking(tb)));

        List<UnifiedBookingResponse> list = new ArrayList<>(all);

        // Optional booking-type filter
        if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
            list = list.stream()
                    .filter(b -> type.equalsIgnoreCase(b.getBookingType()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Optional status filter
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            list = list.stream()
                    .filter(b -> status.equalsIgnoreCase(b.getStatus()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Optional full-text search over customer / service / booking id
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            list = list.stream()
                    .filter(b -> containsIgnoreCase(b.getCustomerName(), q)
                            || containsIgnoreCase(b.getCustomerEmail(), q)
                            || containsIgnoreCase(b.getServiceName(), q)
                            || containsIgnoreCase(b.getId(), q)
                            || containsIgnoreCase(b.getOwnerName(), q))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        list.sort(Comparator.comparing(UnifiedBookingResponse::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        int total = list.size();
        int totalPages = (int) Math.ceil((double) total / safeSize);
        int from = Math.min(safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);
        List<UnifiedBookingResponse> content = (from >= total) ? List.of() : list.subList(from, to);

        return BookingFeedPageResponse.builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnifiedBookingResponse> getOwnerBookings(Long ownerId) {
        List<UnifiedBookingResponse> list = new ArrayList<>();

        // 1. Hotel room bookings where hotel owner matches ownerId
        roomBookingRepository.findAll().stream()
                .filter(rb -> rb.getRooms() != null
                        && rb.getRooms().getHotels() != null
                        && rb.getRooms().getHotels().getOwner() != null
                        && rb.getRooms().getHotels().getOwner().getId().equals(ownerId))
                .forEach(rb -> list.add(mapRoomBooking(rb)));

        // 2. Restaurant food orders where restaurant matches owner
        foodOrderRepository.findAll().stream()
                .filter(fo -> fo.getRestuarants() != null
                        && fo.getRestuarants().getId().equals(ownerId))
                .forEach(fo -> list.add(mapFoodOrder(fo)));

        // 3. Fallback: if list is empty, return matching or sample domain bookings
        if (list.isEmpty()) {
            roomBookingRepository.findAll().stream().limit(10).forEach(rb -> list.add(mapRoomBooking(rb)));
        }

        list.sort(Comparator.comparing(UnifiedBookingResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    private UnifiedBookingResponse mapRoomBooking(RoomBookings rb) {
        Long ownerId = null;
        String ownerName = null;
        String hotelName = "Hotel Stay";

        if (rb.getRooms() != null && rb.getRooms().getHotels() != null) {
            hotelName = rb.getRooms().getHotels().getHotelName();
            if (rb.getRooms().getHotels().getOwner() != null) {
                ownerId = rb.getRooms().getHotels().getOwner().getId();
                ownerName = rb.getRooms().getHotels().getOwner().getFullname();
            }
        }

        return UnifiedBookingResponse.builder()
                .id("HB-" + rb.getId())
                .rawId(rb.getId())
                .bookingType("ROOM")
                .customerName(rb.getUsers() != null ? rb.getUsers().getFullname() : "Guest")
                .customerEmail(rb.getUsers() != null ? rb.getUsers().getEmail() : null)
                .customerPhone(null)
                .serviceName(hotelName)
                .ownerId(ownerId)
                .ownerName(ownerName)
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
                .customerPhone(null)
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

        List<String> items = new ArrayList<>();
        if (fo.getFoodOrderItems() != null) {
            fo.getFoodOrderItems().forEach(i -> {
                if (i.getFoods() != null) items.add(i.getFoods().getName());
            });
        }

        return UnifiedBookingResponse.builder()
                .id("FO-" + fo.getId())
                .rawId(fo.getId())
                .bookingType("FOOD_ORDER")
                .customerName(fo.getUser() != null ? fo.getUser().getFullname() : "Guest")
                .customerEmail(fo.getUser() != null ? fo.getUser().getEmail() : null)
                .customerPhone(null)
                .serviceName(restName)
                .ownerId(ownerId)
                .totalAmount(fo.getTotalPrice())
                .quantity(items.isEmpty() ? 1 : items.size())
                .status(fo.getStatus())
                .createdAt(fo.getCreatedAt() != null ? fo.getCreatedAt() : LocalDateTime.now())
                .items(items)
                .build();
    }

    private UnifiedBookingResponse mapTourBooking(TourBookings tb) {
        String pkgName = "Tour Package";
        if (tb.getTourPackages() != null) {
            pkgName = tb.getTourPackages().getName();
        }

        return UnifiedBookingResponse.builder()
                .id("TOB-" + tb.getId())
                .rawId(tb.getId())
                .bookingType("TOUR")
                .customerName(tb.getUser() != null ? tb.getUser().getFullname() : "Guest")
                .customerEmail(tb.getUser() != null ? tb.getUser().getEmail() : null)
                .customerPhone(null)
                .serviceName(pkgName)
                .totalAmount(tb.getTotalPrice())
                .quantity(tb.getNumPeople())
                .status(tb.getStatus())
                .bookingDate(tb.getTourDate() != null ? tb.getTourDate().toString() : null)
                .createdAt(tb.getCreatedAt() != null ? tb.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private Users resolveUser(BookingOrderRequest request) {
        if (request.getUserId() != null) {
            return userRepository.findById(request.getUserId())
                    .orElseGet(this::getFallbackUser);
        }
        return getFallbackUser();
    }

    private Users getFallbackUser() {
        return userRepository.findAll().stream().findFirst().orElseGet(() -> {
            Users u = new Users();
            u.setUsername("guest_tourist");
            u.setFullname("Guest Customer");
            u.setEmail("customer@tourism.gov.kh");
            u.setPassword("password");
            return userRepository.save(u);
        });
    }

    private Rooms resolveRoom(Long refId) {
        if (refId != null) {
            return roomRepository.findById(refId).orElseGet(this::getFirstRoom);
        }
        return getFirstRoom();
    }

    private Rooms getFirstRoom() {
        return roomRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No rooms available in system"));
    }

    private Tickets resolveTicket(Long refId) {
        if (refId != null) {
            return ticketRepository.findById(refId).orElseGet(this::getFirstTicket);
        }
        return getFirstTicket();
    }

    private Tickets getFirstTicket() {
        return ticketRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No tickets available in system"));
    }

    private Restaurants resolveRestaurant(Long refId) {
        if (refId != null) {
            return restaurantRepository.findById(refId).orElseGet(this::getFirstRestaurant);
        }
        return getFirstRestaurant();
    }

    private Restaurants getFirstRestaurant() {
        return restaurantRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No restaurants available in system"));
    }

    private LocalDate parseDate(String d) {
        if (d == null || d.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(d.substring(0, 10));
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}
