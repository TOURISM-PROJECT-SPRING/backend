package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.spring_boot_project_api.dto.request.BookingOrderRequest;
import com.example.spring_boot_project_api.dto.response.BakongQrResponse;
import com.example.spring_boot_project_api.dto.response.BookingFeedPageResponse;
import com.example.spring_boot_project_api.dto.response.UnifiedBookingResponse;
import com.example.spring_boot_project_api.model.FoodOrders;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.RoomBookings;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.model.Tickets;
import com.example.spring_boot_project_api.model.TourBookings;
import com.example.spring_boot_project_api.model.TourPackages;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.RoomRepository;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TicketRepository;
import com.example.spring_boot_project_api.repository.TourBookingRepository;
import com.example.spring_boot_project_api.repository.TourPackageRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.impl.AdminOwnerBookingServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminOwnerBookingServiceImplTest {

    @Mock
    private RoomBookingRepository roomBookingRepository;
    @Mock
    private TicketBookingRepository ticketBookingRepository;
    @Mock
    private FoodOrderRepository foodOrderRepository;
    @Mock
    private TourBookingRepository tourBookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private TourPackageRepository tourPackageRepository;
    @Mock
    private BookingNotificationService notificationService;
    @Mock
    private BakongPaymentService bakongPaymentService;

    @InjectMocks
    private AdminOwnerBookingServiceImpl bookingService;

    private RoomBookings room;
    private TicketBookings ticket;
    private FoodOrders food;
    private TourBookings tour;

    @BeforeEach
    void setUp() {
        Users john = new Users();
        john.setId(1L);
        john.setFullname("John Doe");
        john.setEmail("john@example.com");

        Hotels hotel = new Hotels();
        hotel.setId(10L);
        hotel.setHotelName("Angkor Paradise");
        Users owner = new Users();
        owner.setId(2L);
        owner.setFullname("Hotel Owner");
        hotel.setOwner(owner);

        Rooms roomEntity = new Rooms();
        roomEntity.setId(30L);
        roomEntity.setHotels(hotel);

        room = new RoomBookings();
        room.setId(1L);
        room.setUsers(john);
        room.setRooms(roomEntity);
        room.setAmount(new BigDecimal("120.00"));
        room.setStatus("CONFIRMED");
        room.setCreatedAt(LocalDateTime.of(2026, 9, 17, 12, 0));

        TourPlaces angkor = new TourPlaces();
        angkor.setId(40L);
        angkor.setName("Angkor Wat");
        Tickets ticketEntity = new Tickets();
        ticketEntity.setId(50L);
        ticketEntity.setTourPlaces(angkor);

        ticket = new TicketBookings();
        ticket.setId(2L);
        ticket.setUser(john);
        ticket.setTickets(ticketEntity);
        ticket.setTotalPrice(new BigDecimal("45.00"));
        ticket.setStatus("PENDING");
        ticket.setCreatedAt(LocalDateTime.of(2026, 9, 17, 13, 0));

        Restaurants restaurant = new Restaurants();
        restaurant.setId(60L);
        restaurant.setName("Chanrey Tree");

        food = new FoodOrders();
        food.setId(3L);
        food.setUser(john);
        food.setRestuarants(restaurant);
        food.setTotalPrice(new BigDecimal("18.00"));
        food.setStatus("PENDING");
        food.setCreatedAt(LocalDateTime.of(2026, 9, 17, 14, 0));

        TourPackages pkg = new TourPackages();
        pkg.setId(70L);
        pkg.setName("Angkor Sunrise Tour");

        tour = new TourBookings();
        tour.setId(4L);
        tour.setUser(john);
        tour.setTourPackages(pkg);
        tour.setTotalPrice(new BigDecimal("150.00"));
        tour.setStatus("CONFIRMED");
        tour.setCreatedAt(LocalDateTime.of(2026, 9, 17, 15, 0));
    }

    @Test
    void getAdminBookings_mergesAllTypesSortedByCreatedAtDesc() {
        stubFindAll();

        BookingFeedPageResponse page = bookingService.getAdminBookings(null, null, null, 0, 10);

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent()).extracting("id")
                .containsExactly("TOB-4", "FO-3", "TB-2", "HB-1");
        assertThat(page.getContent()).extracting("bookingType")
                .containsExactly("TOUR", "FOOD_ORDER", "TICKET", "ROOM");
    }

    @Test
    void getAdminBookings_filtersByType() {
        stubFindAll();
        BookingFeedPageResponse page = bookingService.getAdminBookings("ROOM", null, null, 0, 10);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo("HB-1");
    }

    @Test
    void getAdminBookings_filtersByStatusIgnoreCase() {
        stubFindAll();
        BookingFeedPageResponse page = bookingService.getAdminBookings(null, "pending", null, 0, 10);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting("id").containsExactly("FO-3", "TB-2");
    }

    @Test
    void getAdminBookings_searchesByCustomerEmail() {
        stubFindAll();
        BookingFeedPageResponse page = bookingService.getAdminBookings(null, null, "JOHN", 0, 10);
        assertThat(page.getTotalElements()).isEqualTo(4);
    }

    @Test
    void getAdminBookings_paginates() {
        stubFindAll();
        BookingFeedPageResponse page0 = bookingService.getAdminBookings(null, null, null, 0, 2);
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(4);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page0.getContent()).extracting("id").containsExactly("TOB-4", "FO-3");

        BookingFeedPageResponse page1 = bookingService.getAdminBookings(null, null, null, 1, 2);
        assertThat(page1.getContent()).extracting("id").containsExactly("TB-2", "HB-1");
    }

    private void stubFindAll() {
        lenient().when(roomBookingRepository.findAll()).thenReturn(List.of(room));
        lenient().when(ticketBookingRepository.findAll()).thenReturn(List.of(ticket));
        lenient().when(foodOrderRepository.findAll()).thenReturn(List.of(food));
        lenient().when(tourBookingRepository.findAll()).thenReturn(List.of(tour));
    }

    @Test
    void createBooking_tour_persistsTourBookingAndAttachesBakongQr() {
        Users john = new Users();
        john.setId(1L);
        john.setFullname("John Doe");
        john.setEmail("john@example.com");
        when(userRepository.findAll()).thenReturn(List.of(john));

        TourPackages pkg = new TourPackages();
        pkg.setId(70L);
        pkg.setName("Angkor Sunrise Tour");
        when(tourPackageRepository.findById(70L)).thenReturn(java.util.Optional.of(pkg));

        TourBookings saved = new TourBookings();
        saved.setId(9L);
        saved.setUser(john);
        saved.setTourPackages(pkg);
        saved.setNumPeople(2);
        saved.setTotalPrice(new BigDecimal("300.00"));
        saved.setStatus("PENDING");
        saved.setCreatedAt(LocalDateTime.now());
        when(tourBookingRepository.save(any(TourBookings.class))).thenReturn(saved);

        BakongQrResponse qr = BakongQrResponse.builder()
                .qrImage("data:image/png;base64,xxxx")
                .md5("abcd1234")
                .billNumber("BK-1")
                .amount(new BigDecimal("300.00"))
                .bookingId(9L)
                .bookingType("TOUR")
                .build();
        when(bakongPaymentService.generateKhqr(any())).thenReturn(qr);

        BookingOrderRequest request = BookingOrderRequest.builder()
                .customerName("John Doe")
                .bookingType("TOUR")
                .referenceId(70L)
                .quantity(2)
                .totalAmount(new BigDecimal("300.00"))
                .paymentMethod("BAKONG_KHQR")
                .build();

        UnifiedBookingResponse result = bookingService.createBooking(request);

        assertThat(result.getBookingType()).isEqualTo("TOUR");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getBakongQr()).isNotNull();
        assertThat(result.getBakongQr().getMd5()).isEqualTo("abcd1234");
        assertThat(result.getBakongQr().getQrImage()).startsWith("data:image/png;base64,");
    }

    @Test
    void createBooking_cardPayment_doesNotAttachQr() {
        Users john = new Users();
        john.setId(1L);
        john.setFullname("John Doe");
        john.setEmail("john@example.com");
        when(userRepository.findAll()).thenReturn(List.of(john));

        Rooms roomEntity = new Rooms();
        roomEntity.setId(30L);
        when(roomRepository.findById(30L)).thenReturn(java.util.Optional.of(roomEntity));

        RoomBookings saved = room;
        saved.setStatus("PENDING");
        saved.setPaymentMethod("CARD");
        when(roomBookingRepository.save(any(RoomBookings.class))).thenReturn(saved);

        BookingOrderRequest request = BookingOrderRequest.builder()
                .customerName("John Doe")
                .bookingType("ROOM")
                .referenceId(30L)
                .totalAmount(new BigDecimal("120.00"))
                .paymentMethod("CARD")
                .build();

        UnifiedBookingResponse result = bookingService.createBooking(request);

        assertThat(result.getBakongQr()).isNull();
        verify(bakongPaymentService, never()).generateKhqr(any());
    }
}