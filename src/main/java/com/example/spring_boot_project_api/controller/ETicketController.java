package com.example.spring_boot_project_api.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;
import com.example.spring_boot_project_api.service.ETicketService;
import com.example.spring_boot_project_api.service.TicketBookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/ticket-bookings")
@RequiredArgsConstructor
public class ETicketController {

    private final TicketBookingService ticketBookingService;
    private final ETicketService eTicketService;

    @GetMapping("/{id}/eticket")
    public ResponseEntity<byte[]> downloadETicket(@PathVariable Long id) {
        TicketBookingResponse booking = ticketBookingService.findById(id);
        byte[] pdf = eTicketService.generatePdf(booking);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=eticket-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
