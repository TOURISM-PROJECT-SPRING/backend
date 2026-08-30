package com.example.spring_boot_project_api.mapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.TicketBookingRequest;
import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.model.Tickets;
import com.example.spring_boot_project_api.model.Users;

public class TicketBookingMapper {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_USED = "USED";

    private TicketBookingMapper() {}

    public static BigDecimal calculateTotal(BigDecimal price, Integer quantity) {
        if (price == null || quantity == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public static TicketBookings toEntity(TicketBookingRequest request, Users user, Tickets ticket) {
        TicketBookings booking = new TicketBookings();
        booking.setUser(user);
        booking.setTickets(ticket);
        booking.setQuantity(request.getQuantity());
        booking.setVisiDate(request.getVisitDate());
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setTotalPrice(calculateTotal(ticket.getPrice(), request.getQuantity()));
        booking.setStatus(STATUS_PENDING);
        return booking;
    }

    public static TicketBookingResponse toResponse(TicketBookings booking) {
        if (booking == null) return null;
        Tickets ticket = booking.getTickets();
        return TicketBookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser() != null ? booking.getUser().getId() : null)
                .userName(booking.getUser() != null ? booking.getUser().getFullname() : null)
                .ticketId(ticket != null ? ticket.getId() : null)
                .ticketName(ticket != null ? ticket.getName() : null)
                .unitPrice(ticket != null ? ticket.getPrice() : null)
                .tourismPlaceId(ticket != null && ticket.getTourismPlaces() != null
                        ? ticket.getTourismPlaces().getId() : null)
                .tourismPlaceName(ticket != null && ticket.getTourismPlaces() != null
                        ? ticket.getTourismPlaces().getName() : null)
                .quantity(booking.getQuantity())
                .totalPrice(booking.getTotalPrice())
                .visitDate(booking.getVisiDate())
                .status(booking.getStatus())
                .qrCode(booking.getQrCode())
                .paymentMethod(booking.getPaymentMethod())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public static List<TicketBookingResponse> toResponseList(List<TicketBookings> bookings) {
        if (bookings == null) return Collections.emptyList();
        return bookings.stream()
                .map(TicketBookingMapper::toResponse)
                .collect(Collectors.toList());
    }
}
