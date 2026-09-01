package com.example.spring_boot_project_api.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.PaymentCallbackRequest;
import com.example.spring_boot_project_api.dto.response.PaymentInitiationResponse;
import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.TicketBookingMapper;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_SUCCESS = "SUCCESS";
    public static final String PAYMENT_FAILED = "FAILED";

    private final TicketBookingRepository ticketBookingRepository;

    // Stub gateway state: maps a mock transaction id to the ticket booking id.
    private final ConcurrentMap<String, Long> transactions = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public PaymentInitiationResponse initiatePayment(Long bookingId, String paymentMethod) {
        TicketBookings booking = ticketBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket Booking", bookingId));

        if (!TicketBookingMapper.STATUS_PENDING.equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("Payment can only be initiated for a PENDING booking");
        }

        String transactionId = "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        transactions.put(transactionId, bookingId);

        // Stub: in a real integration this would call the gateway and return a hosted checkout URL.
        return PaymentInitiationResponse.builder()
                .bookingId(bookingId)
                .transactionId(transactionId)
                .paymentUrl("https://payment.example.com/checkout/" + transactionId)
                .status("PENDING")
                .initiatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public TicketBookingResponse handleCallback(PaymentCallbackRequest request) {
        if (!PAYMENT_SUCCESS.equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException(
                    "Unhandled payment status: " + request.getStatus());
        }

        Long bookingId = resolveBookingId(request);
        TicketBookings booking = ticketBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket Booking", bookingId));

        booking.setStatus(TicketBookingMapper.STATUS_CONFIRMED);
        TicketBookings saved = ticketBookingRepository.save(booking);
        return TicketBookingMapper.toResponse(saved);
    }

    private Long resolveBookingId(PaymentCallbackRequest request) {
        if (request.getBookingId() != null) {
            return request.getBookingId();
        }
        Long fromTx = transactions.get(request.getTransactionId());
        if (fromTx == null) {
            throw new IllegalArgumentException(
                    "Unknown transaction id: " + request.getTransactionId());
        }
        return fromTx;
    }
}
