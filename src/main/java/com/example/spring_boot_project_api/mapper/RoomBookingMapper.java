package com.example.spring_boot_project_api.mapper;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.RoomBookingRequest;
import com.example.spring_boot_project_api.dto.response.RoomBookingResponse;
import com.example.spring_boot_project_api.model.RoomBookings;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.Users;

public class RoomBookingMapper {

    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private RoomBookingMapper() {}

    public static BigDecimal calculateAmount(BigDecimal pricePerNight,
                                             java.time.LocalDate checkIn,
                                             java.time.LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return pricePerNight.multiply(BigDecimal.valueOf(nights));
    }

    public static RoomBookings toEntity(RoomBookingRequest request, Users user, Rooms room,
                                        BigDecimal pricePerNight) {
        RoomBookings booking = new RoomBookings();
        toEntity(booking, request, user, room, pricePerNight);
        booking.setStatus(STATUS_CONFIRMED);
        return booking;
    }

    public static void toEntity(RoomBookings booking, RoomBookingRequest request, Users user, Rooms room,
                                BigDecimal pricePerNight) {
        booking.setUsers(user);
        booking.setRooms(room);
        booking.setNumGuest(request.getNumGuest());
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setAmount(calculateAmount(pricePerNight, request.getCheckIn(), request.getCheckOut()));
    }

    public static RoomBookingResponse toResponse(RoomBookings booking) {
        if (booking == null) return null;
        return RoomBookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUsers() != null ? booking.getUsers().getId() : null)
                .userName(booking.getUsers() != null ? booking.getUsers().getFullname() : null)
                .roomId(booking.getRooms() != null ? booking.getRooms().getId() : null)
                .hotelId(booking.getRooms() != null && booking.getRooms().getHotels() != null
                        ? booking.getRooms().getHotels().getId() : null)
                .hotelName(booking.getRooms() != null && booking.getRooms().getHotels() != null
                        ? booking.getRooms().getHotels().getHotelName() : null)
                .roomType(booking.getRooms() != null && booking.getRooms().getRoomTypes() != null
                        ? booking.getRooms().getRoomTypes().getRoomType() : null)
                .numGuest(booking.getNumGuest())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .paymentMethod(booking.getPaymentMethod())
                .pricePerNight(resolvePricePerNight(booking))
                .amount(booking.getAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private static BigDecimal resolvePricePerNight(RoomBookings booking) {
        if (booking == null || booking.getCheckIn() == null
                || booking.getCheckOut() == null || booking.getAmount() == null) {
            return null;
        }
        long nights = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
        if (nights <= 0) {
            return null;
        }
        return booking.getAmount().divide(BigDecimal.valueOf(nights), 2, java.math.RoundingMode.HALF_UP);
    }

    public static List<RoomBookingResponse> toResponseList(List<RoomBookings> bookings) {
        if (bookings == null) return Collections.emptyList();
        return bookings.stream()
                .map(RoomBookingMapper::toResponse)
                .collect(Collectors.toList());
    }
}