package com.example.spring_boot_project_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.PaymentCallbackRequest;
import com.example.spring_boot_project_api.dto.response.PaymentInitiationResponse;
import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;
import com.example.spring_boot_project_api.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/ticket-bookings/{id}/payment")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
            @PathVariable Long id,
            @RequestParam(defaultValue = "CARD") String paymentMethod) {
        return ResponseEntity.ok(paymentService.initiatePayment(id, paymentMethod));
    }

    @PostMapping("/api/payments/callback")
    public ResponseEntity<TicketBookingResponse> callback(
            @Valid @RequestBody PaymentCallbackRequest request) {
        return ResponseEntity.ok(paymentService.handleCallback(request));
    }

    @PostMapping("/api/payments/callback-form")
    public ResponseEntity<TicketBookingResponse> callbackForm(
            @RequestParam String transactionId,
            @RequestParam String status,
            @RequestParam(required = false) Long bookingId) {
        PaymentCallbackRequest request = PaymentCallbackRequest.builder()
                .transactionId(transactionId)
                .status(status)
                .bookingId(bookingId)
                .build();
        return ResponseEntity.ok(paymentService.handleCallback(request));
    }
}
