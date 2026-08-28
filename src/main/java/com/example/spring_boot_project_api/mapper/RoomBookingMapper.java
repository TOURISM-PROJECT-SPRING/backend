package com.example.spring_boot_project_api.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.RoomBookingRequest;
import com.example.spring_boot_project_api.dto.response.RoomBookingResponse;
import com.example.spring_boot_project_api.model.RoomBookings;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.Users;

public class RoomBookingMapper {

    private RoomBookingMapper() {}

    public static RoomBookings toEntity(RoomBookingRequest request, Users user, Rooms room) {
        RoomBookings booking = new RoomBookings();
        booking.setUsers(user);
        booking.setRooms(room);
        booking.setNumGuest(request.getNumGuest());
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setAmount(request.getAmount());
        booking.setStatus(request.getStatus());
        return booking;
    }

    public static void toEntity(RoomBookings booking, RoomBookingRequest request, Users user, Rooms room) {
        booking.setUsers(user);
        booking.setRooms(room);
        booking.setNumGuest(request.getNumGuest());
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setAmount(request.getAmount());
        booking.setStatus(request.getStatus());
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
                .amount(booking.getAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public static List<RoomBookingResponse> toResponseList(List<RoomBookings> bookings) {
        if (bookings == null) return Collections.emptyList();
        return bookings.stream()
                .map(RoomBookingMapper::toResponse)
                .collect(Collectors.toList());
    }
}