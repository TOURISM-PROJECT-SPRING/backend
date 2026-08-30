package com.example.spring_boot_project_api.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.TicketRequest;
import com.example.spring_boot_project_api.dto.response.TicketResponse;
import com.example.spring_boot_project_api.model.Tickets;
import com.example.spring_boot_project_api.model.TourismPlaces;

public class TicketMapper {

    private TicketMapper() {}

    public static Tickets toEntity(TicketRequest request, TourismPlaces place) {
        Tickets ticket = new Tickets();
        toEntity(ticket, request, place);
        return ticket;
    }

    public static void toEntity(Tickets ticket, TicketRequest request, TourismPlaces place) {
        ticket.setName(request.getName());
        ticket.setPrice(request.getPrice());
        ticket.setDescription(request.getDescription());
        ticket.setIsAvailable(request.getIsAvailable() == null || request.getIsAvailable());
        ticket.setTourismPlaces(place);
    }

    public static TicketResponse toResponse(Tickets ticket) {
        if (ticket == null) return null;
        return TicketResponse.builder()
                .id(ticket.getId())
                .name(ticket.getName())
                .price(ticket.getPrice())
                .description(ticket.getDescription())
                .isAvailable(ticket.getIsAvailable())
                .tourismPlaceId(ticket.getTourismPlaces() != null
                        ? ticket.getTourismPlaces().getId() : null)
                .tourismPlaceName(ticket.getTourismPlaces() != null
                        ? ticket.getTourismPlaces().getName() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public static List<TicketResponse> toResponseList(List<Tickets> tickets) {
        if (tickets == null) return Collections.emptyList();
        return tickets.stream()
                .map(TicketMapper::toResponse)
                .collect(Collectors.toList());
    }
}
