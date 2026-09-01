package com.example.spring_boot_project_api.service;

import java.time.LocalDate;
import java.util.List;

import com.example.spring_boot_project_api.dto.request.TicketBookingRequest;
import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;

public interface TicketBookingService {

    List<TicketBookingResponse> findAll();

    TicketBookingResponse findById(Long id);

    List<TicketBookingResponse> findByUserId(Long userId);

    List<TicketBookingResponse> findByTicketId(Long ticketId);

    List<TicketBookingResponse> findByStatus(String status);

    List<TicketBookingResponse> findByVisitDate(LocalDate visitDate);

    TicketBookingResponse create(TicketBookingRequest request);

    TicketBookingResponse cancel(Long id);

    TicketBookingResponse markUsed(Long id);

    TicketBookingResponse verify(String qrCode);

    void delete(Long id);
}
