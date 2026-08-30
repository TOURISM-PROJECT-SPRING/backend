package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.spring_boot_project_api.dto.request.TicketBookingRequest;
import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;
import com.example.spring_boot_project_api.mapper.TicketBookingMapper;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.model.Tickets;
import com.example.spring_boot_project_api.model.TourismPlaces;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TicketRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.impl.TicketBookingServiceImpl;

@ExtendWith(MockitoExtension.class)
class TicketBookingServiceImplTest {

    @Mock
    private TicketBookingRepository ticketBookingRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketBookingServiceImpl ticketBookingService;

    private Users user() {
        Users user = new Users();
        user.setId(1L);
        user.setFullname("John Doe");
        return user;
    }

    private TourismPlaces place() {
        TourismPlaces place = new TourismPlaces();
        place.setId(10L);
        place.setName("Angkor Wat");
        return place;
    }

    private Tickets ticket() {
        Tickets ticket = new Tickets();
        ticket.setId(20L);
        ticket.setName("Adult Entry");
        ticket.setPrice(new BigDecimal("25.00"));
        ticket.setIsAvailable(true);
        ticket.setTourismPlaces(place());
        return ticket;
    }

    private TicketBookingRequest request(int quantity, LocalDate visitDate) {
        return TicketBookingRequest.builder()
                .userId(1L)
                .ticketId(20L)
                .quantity(quantity)
                .visitDate(visitDate)
                .paymentMethod("ABA")
                .build();
    }

    @Test
    void create_usesServerSideTotalAndSetsPendingStatus() {
        Users user = user();
        Tickets ticket = ticket();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepository.findById(20L)).thenReturn(Optional.of(ticket));
        when(ticketBookingRepository.countByTicketsIdAndVisiDate(any(), any())).thenReturn(0L);
        when(ticketBookingRepository.save(any(TicketBookings.class))).thenAnswer(inv -> {
            TicketBookings b = inv.getArgument(0);
            b.setId(99L);
            return b;
        });

        TicketBookingResponse response = ticketBookingService.create(
                request(3, LocalDate.now().plusDays(1)));

        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(response.getStatus()).isEqualTo(TicketBookingMapper.STATUS_PENDING);
        assertThat(response.getQrCode()).isNotBlank();
        assertThat(response.getQuantity()).isEqualTo(3);
    }

    @Test
    void create_rejectsPastVisitDate() {
        Users user = user();
        Tickets ticket = ticket();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepository.findById(20L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketBookingService.create(
                request(1, LocalDate.now().minusDays(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Visit date");

        verify(ticketBookingRepository, never()).save(any());
    }

    @Test
    void create_rejectsUnavailableTicket() {
        Tickets ticket = ticket();
        ticket.setIsAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(ticketRepository.findById(20L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketBookingService.create(
                request(1, LocalDate.now().plusDays(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");

        verify(ticketBookingRepository, never()).save(any());
    }

    @Test
    void create_rejectsWhenDailyQuotaExceeded() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(ticketRepository.findById(20L)).thenReturn(Optional.of(ticket()));
        when(ticketBookingRepository.countByTicketsIdAndVisiDate(any(), any()))
                .thenReturn((long) TicketBookingServiceImpl.DEFAULT_DAILY_QUOTA - 1);

        assertThatThrownBy(() -> ticketBookingService.create(
                request(2, LocalDate.now().plusDays(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quota");

        verify(ticketBookingRepository, never()).save(any());
    }

    @Test
    void cancel_setsStatusToCancelled() {
        TicketBookings booking = new TicketBookings();
        booking.setId(1L);
        booking.setStatus(TicketBookingMapper.STATUS_PENDING);
        booking.setUser(user());
        booking.setTickets(ticket());

        when(ticketBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(ticketBookingRepository.save(any(TicketBookings.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TicketBookingResponse response = ticketBookingService.cancel(1L);

        assertThat(response.getStatus()).isEqualTo(TicketBookingMapper.STATUS_CANCELLED);
        assertThat(booking.getStatus()).isEqualTo(TicketBookingMapper.STATUS_CANCELLED);
    }

    @Test
    void cancel_throwsWhenAlreadyCancelled() {
        TicketBookings booking = new TicketBookings();
        booking.setId(1L);
        booking.setStatus(TicketBookingMapper.STATUS_CANCELLED);

        when(ticketBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> ticketBookingService.cancel(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already cancelled");

        verify(ticketBookingRepository, never()).save(any());
    }

    @Test
    void verify_returnsBookingForValidQrCode() {
        TicketBookings booking = new TicketBookings();
        booking.setId(1L);
        booking.setStatus(TicketBookingMapper.STATUS_CONFIRMED);
        booking.setQrCode("TKT-ABC");
        booking.setUser(user());
        booking.setTickets(ticket());
        booking.setQuantity(1);
        booking.setTotalPrice(new BigDecimal("25.00"));

        when(ticketBookingRepository.findByQrCode("TKT-ABC")).thenReturn(Optional.of(booking));

        TicketBookingResponse response = ticketBookingService.verify("TKT-ABC");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(TicketBookingMapper.STATUS_CONFIRMED);
    }

    @Test
    void verify_throwsForAlreadyUsedTicket() {
        TicketBookings booking = new TicketBookings();
        booking.setId(1L);
        booking.setStatus(TicketBookingMapper.STATUS_USED);

        when(ticketBookingRepository.findByQrCode(anyString())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> ticketBookingService.verify("TKT-ABC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    void verify_throwsForUnknownQrCode() {
        when(ticketBookingRepository.findByQrCode(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketBookingService.verify("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid QR code");
    }
}
