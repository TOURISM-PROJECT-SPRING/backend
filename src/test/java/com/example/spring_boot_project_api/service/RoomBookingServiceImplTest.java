package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.spring_boot_project_api.dto.request.RoomBookingRequest;
import com.example.spring_boot_project_api.dto.response.RoomBookingResponse;
import com.example.spring_boot_project_api.mapper.RoomBookingMapper;
import com.example.spring_boot_project_api.model.HotelRooms;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.RoomBookings;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.RoomTypes;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.HotelRoomRepository;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.RoomRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.impl.RoomBookingServiceImpl;

@ExtendWith(MockitoExtension.class)
class RoomBookingServiceImplTest {

    private static final BigDecimal PRICE_PER_NIGHT = new BigDecimal("100.00");

    @Mock
    private RoomBookingRepository roomBookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private HotelRoomRepository hotelRoomRepository;

    @InjectMocks
    private RoomBookingServiceImpl roomBookingService;

    private Users user() {
        Users user = new Users();
        user.setId(1L);
        user.setFullname("John Doe");
        return user;
    }

    private Rooms room() {
        Hotels hotel = new Hotels();
        hotel.setId(10L);
        hotel.setHotelName("Grand Hotel");

        RoomTypes roomType = new RoomTypes();
        roomType.setId(20L);
        roomType.setRoomType("Deluxe");

        Rooms room = new Rooms();
        room.setId(30L);
        room.setHotels(hotel);
        room.setRoomTypes(roomType);
        return room;
    }

    private HotelRooms hotelRoom() {
        HotelRooms hotelRoom = new HotelRooms();
        hotelRoom.setId(40L);
        hotelRoom.setHotels(room().getHotels());
        hotelRoom.setRoomTypes(room().getRoomTypes());
        hotelRoom.setPricePerNight(PRICE_PER_NIGHT);
        return hotelRoom;
    }

    private RoomBookingRequest request(LocalDate checkIn, LocalDate checkOut) {
        return RoomBookingRequest.builder()
                .userId(1L)
                .roomId(30L)
                .numGuest(2)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .paymentMethod("CASH")
                .build();
    }

    @Test
    void create_calculatesAmountAndDefaultsStatusToConfirmed() {
        Rooms room = room();
        Users user = user();
        BigDecimal expectedAmount = new BigDecimal("300.00");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(30L)).thenReturn(Optional.of(room));
        when(hotelRoomRepository.findByHotelsIdAndRoomTypesId(10L, 20L))
                .thenReturn(Optional.of(hotelRoom()));
        when(roomBookingRepository.findByRooms_IdAndCheckInLessThanEqualAndCheckOutGreaterThanEqual(
                any(), any(), any())).thenReturn(Collections.emptyList());

        RoomBookings booking = new RoomBookings();
        booking.setId(99L);
        booking.setUsers(user);
        booking.setRooms(room);
        when(roomBookingRepository.save(any(RoomBookings.class))).thenAnswer(inv -> {
            RoomBookings b = inv.getArgument(0);
            b.setId(99L);
            return b;
        });

        RoomBookingResponse response = roomBookingService.create(
                request(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4)));

        assertThat(response.getAmount()).isEqualByComparingTo(expectedAmount);
        assertThat(response.getStatus()).isEqualTo(RoomBookingMapper.STATUS_CONFIRMED);
        assertThat(response.getPricePerNight()).isEqualByComparingTo(PRICE_PER_NIGHT);
    }

    @Test
    void create_rejectsOverlappingConfirmedBooking() {
        Rooms room = room();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(roomRepository.findById(30L)).thenReturn(Optional.of(room));
        when(hotelRoomRepository.findByHotelsIdAndRoomTypesId(10L, 20L))
                .thenReturn(Optional.of(hotelRoom()));

        RoomBookings existing = new RoomBookings();
        existing.setId(1L);
        existing.setStatus(RoomBookingMapper.STATUS_CONFIRMED);
        when(roomBookingRepository.findByRooms_IdAndCheckInLessThanEqualAndCheckOutGreaterThanEqual(
                any(), any(), any())).thenReturn(List.of(existing));

        assertThatThrownBy(() -> roomBookingService.create(
                request(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unavailable");

        verify(roomBookingRepository, never()).save(any());
    }

    @Test
    void create_allowsOverlapWithCancelledBooking() {
        Rooms room = room();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(roomRepository.findById(30L)).thenReturn(Optional.of(room));
        when(hotelRoomRepository.findByHotelsIdAndRoomTypesId(10L, 20L))
                .thenReturn(Optional.of(hotelRoom()));

        RoomBookings cancelled = new RoomBookings();
        cancelled.setId(1L);
        cancelled.setStatus(RoomBookingMapper.STATUS_CANCELLED);
        when(roomBookingRepository.findByRooms_IdAndCheckInLessThanEqualAndCheckOutGreaterThanEqual(
                any(), any(), any())).thenReturn(List.of(cancelled));

        when(roomBookingRepository.save(any(RoomBookings.class))).thenAnswer(inv -> {
            RoomBookings b = inv.getArgument(0);
            b.setId(99L);
            return b;
        });

        RoomBookingResponse response = roomBookingService.create(
                request(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4)));

        assertThat(response.getStatus()).isEqualTo(RoomBookingMapper.STATUS_CONFIRMED);
    }

    @Test
    void create_rejectsInvalidDates() {
        Users user = user();
        Rooms room = room();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(30L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomBookingService.create(
                request(LocalDate.of(2026, 9, 4), LocalDate.of(2026, 9, 1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Check-out");

        verify(roomBookingRepository, never()).save(any());
    }

    @Test
    void cancel_setsStatusToCancelled() {
        RoomBookings booking = new RoomBookings();
        booking.setId(1L);
        booking.setStatus(RoomBookingMapper.STATUS_CONFIRMED);
        booking.setUsers(user());
        booking.setRooms(room());
        booking.setAmount(new BigDecimal("100.00"));
        booking.setCheckIn(LocalDate.of(2026, 9, 1));
        booking.setCheckOut(LocalDate.of(2026, 9, 2));

        when(roomBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        when(roomBookingRepository.save(any(RoomBookings.class))).thenAnswer(inv -> inv.getArgument(0));

        RoomBookingResponse response = roomBookingService.cancel(1L);

        assertThat(response.getStatus()).isEqualTo(RoomBookingMapper.STATUS_CANCELLED);
        assertThat(booking.getStatus()).isEqualTo(RoomBookingMapper.STATUS_CANCELLED);
    }

    @Test
    void cancel_throwsWhenAlreadyCancelled() {
        RoomBookings booking = new RoomBookings();
        booking.setId(1L);
        booking.setStatus(RoomBookingMapper.STATUS_CANCELLED);

        when(roomBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> roomBookingService.cancel(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already cancelled");

        verify(roomBookingRepository, never()).save(any());
    }
}
