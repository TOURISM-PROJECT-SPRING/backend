package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.spring_boot_project_api.dto.request.PaymentCallbackRequest;
import com.example.spring_boot_project_api.dto.response.PaymentInitiationResponse;
import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;
import com.example.spring_boot_project_api.mapper.TicketBookingMapper;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.service.impl.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private TicketBookingRepository ticketBookingRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private TicketBookings pendingBooking() {
        TicketBookings booking = new TicketBookings();
        booking.setId(7L);
        booking.setStatus(TicketBookingMapper.STATUS_PENDING);
        return booking;
    }

    @Test
    void initiatePayment_returnsStubUrlForPendingBooking() {
        when(ticketBookingRepository.findById(7L)).thenReturn(Optional.of(pendingBooking()));

        PaymentInitiationResponse response = paymentService.initiatePayment(7L, "ABA");

        assertThat(response.getBookingId()).isEqualTo(7L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getPaymentUrl()).isNotBlank();
        assertThat(response.getTransactionId()).isNotBlank();
    }

    @Test
    void initiatePayment_rejectsNonPendingBooking() {
        TicketBookings booking = pendingBooking();
        booking.setStatus(TicketBookingMapper.STATUS_CONFIRMED);
        when(ticketBookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.initiatePayment(7L, "ABA"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void callback_marksBookingConfirmed() {
        TicketBookings booking = pendingBooking();
        when(ticketBookingRepository.findById(7L)).thenReturn(Optional.of(booking));
        when(ticketBookingRepository.save(any(TicketBookings.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TicketBookingResponse response = paymentService.handleCallback(
                PaymentCallbackRequest.builder()
                        .transactionId("PAY-123")
                        .status(PaymentServiceImpl.PAYMENT_SUCCESS)
                        .bookingId(7L)
                        .build());

        assertThat(response.getStatus()).isEqualTo(TicketBookingMapper.STATUS_CONFIRMED);
        assertThat(booking.getStatus()).isEqualTo(TicketBookingMapper.STATUS_CONFIRMED);
    }

    @Test
    void callback_rejectsUnknownTransactionAndNoBookingId() {
        assertThatThrownBy(() -> paymentService.handleCallback(
                PaymentCallbackRequest.builder()
                        .transactionId("PAY-NOPE")
                        .status(PaymentServiceImpl.PAYMENT_SUCCESS)
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown transaction");

        verify(ticketBookingRepository, never()).save(any());
    }

    @Test
    void callback_rejectsUnsupportedStatus() {
        assertThatThrownBy(() -> paymentService.handleCallback(
                PaymentCallbackRequest.builder()
                        .transactionId("PAY-123")
                        .status("FAILED")
                        .bookingId(7L)
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unhandled payment status");
    }
}
