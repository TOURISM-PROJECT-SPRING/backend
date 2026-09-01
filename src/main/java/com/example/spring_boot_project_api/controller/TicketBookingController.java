package com.example.spring_boot_project_api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.TicketBookingRequest;
import com.example.spring_boot_project_api.dto.request.VerifyRequest;
import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;
import com.example.spring_boot_project_api.service.TicketBookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/ticket-bookings")
@RequiredArgsConstructor
public class TicketBookingController {

    private final TicketBookingService ticketBookingService;

    @GetMapping
    public ResponseEntity<List<TicketBookingResponse>> findAll() {
        return ResponseEntity.ok(ticketBookingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketBookingResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketBookingService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketBookingResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketBookingService.findByUserId(userId));
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<TicketBookingResponse>> findByTicketId(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketBookingService.findByTicketId(ticketId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketBookingResponse>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(ticketBookingService.findByStatus(status));
    }

    @GetMapping("/date")
    public ResponseEntity<List<TicketBookingResponse>> findByVisitDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        return ResponseEntity.ok(ticketBookingService.findByVisitDate(visitDate));
    }

    @PostMapping
    public ResponseEntity<TicketBookingResponse> create(@Valid @RequestBody TicketBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketBookingService.create(request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TicketBookingResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ticketBookingService.cancel(id));
    }

    @PostMapping("/{id}/use")
    public ResponseEntity<TicketBookingResponse> markUsed(@PathVariable Long id) {
        return ResponseEntity.ok(ticketBookingService.markUsed(id));
    }

    @PostMapping("/verify")
    public ResponseEntity<TicketBookingResponse> verify(@Valid @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(ticketBookingService.verify(request.getQrCode()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketBookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
