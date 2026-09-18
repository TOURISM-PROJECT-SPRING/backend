package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import com.example.spring_boot_project_api.config.BakongConfig;
import com.example.spring_boot_project_api.dto.request.BakongQrGenerateRequest;
import com.example.spring_boot_project_api.dto.response.BakongCheckStatusResponse;
import com.example.spring_boot_project_api.dto.response.BakongQrResponse;
import com.example.spring_boot_project_api.enums.PaymentMethod;
import com.example.spring_boot_project_api.enums.PaymentStatus;
import com.example.spring_boot_project_api.model.Payments;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.PaymentRepository;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TourBookingRepository;
import com.example.spring_boot_project_api.service.impl.BakongPaymentServiceImpl;

class BakongPaymentServiceImplTest {

    private final TicketBookingRepository ticketBookingRepository = mock(TicketBookingRepository.class);
    private final RoomBookingRepository roomBookingRepository = mock(RoomBookingRepository.class);
    private final FoodOrderRepository foodOrderRepository = mock(FoodOrderRepository.class);
    private final TourBookingRepository tourBookingRepository = mock(TourBookingRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private BakongConfig mockConfig() {
        BakongConfig config = mock(BakongConfig.class);
        when(config.getApiUrl()).thenReturn("https://api-bakong.nbc.gov.kh/v1");
        when(config.getApiToken()).thenReturn("");
        when(config.getMerchantName()).thenReturn("Merchant");
        when(config.getMerchantAccount()).thenReturn("merchant@aclb");
        when(config.getMerchantCity()).thenReturn("Siem Reap");
        when(config.getQrExpiryMinutes()).thenReturn(5);
        return config;
    }

    private BakongPaymentServiceImpl newService(BakongConfig config) {
        return new BakongPaymentServiceImpl(
                config, restTemplate, ticketBookingRepository, roomBookingRepository,
                foodOrderRepository, tourBookingRepository, paymentRepository);
    }

    @Test
    void generateKhqr_buildsPayloadAndQrImage() {
        BakongPaymentServiceImpl service = newService(mockConfig());

        BakongQrResponse qr = service.generateKhqr(BakongQrGenerateRequest.builder()
                .bookingId(7L)
                .bookingType("TICKET")
                .amount(new BigDecimal("45.00"))
                .build());

        assertThat(qr.getMd5()).hasSize(32);
        assertThat(qr.getBillNumber()).startsWith("BK-");
        assertThat(qr.getAmount()).isEqualByComparingTo("45.00");
        assertThat(qr.getCurrency()).isEqualTo("USD");
        assertThat(qr.getQrImage()).startsWith("data:image/png;base64,");
        assertThat(qr.getExpiresAt()).isAfter(qr.getCreatedAt());
    }

    @Test
    void simulatePayment_confirmsTicketAndPersistsPayment() {
        TicketBookings booking = new TicketBookings();
        booking.setId(7L);
        booking.setTotalPrice(new BigDecimal("45.00"));
        booking.setStatus("PENDING");
        when(ticketBookingRepository.findById(7L)).thenReturn(Optional.of(booking));
        when(ticketBookingRepository.save(any(TicketBookings.class))).thenReturn(booking);

        BakongPaymentServiceImpl service = newService(mockConfig());
        BakongQrResponse qr = service.generateKhqr(BakongQrGenerateRequest.builder()
                .bookingId(7L)
                .bookingType("TICKET")
                .amount(new BigDecimal("45.00"))
                .build());

        BakongCheckStatusResponse status = service.simulatePayment(qr.getMd5());

        assertThat(status.getStatus()).isEqualTo("SUCCESS");
        assertThat(booking.getStatus()).isEqualTo("CONFIRMED");
        verify(ticketBookingRepository).save(booking);
        verify(paymentRepository).save(argThat(p -> {
            Payments payment = (Payments) p;
            return payment.getTicketBookings() == booking
                    && payment.getAmount().compareTo(new BigDecimal("45.00")) == 0
                    && payment.getPaymentMethod() == PaymentMethod.BAKONG_KHQR
                    && payment.getStatus() == PaymentStatus.SUCCESS
                    && payment.getPaymentReference() != null
                    && payment.getQrMd5() != null
                    && payment.getExpiredAt() != null;
        }));
    }

    @Test
    void simulatePayment_forUnregisteredMd5_doesNotPersistWithoutBooking() {
        BakongPaymentServiceImpl service = newService(mockConfig());

        BakongCheckStatusResponse status = service.simulatePayment("unknown-md5-hash");

        assertThat(status.getStatus()).isEqualTo(BakongPaymentServiceImpl.STATUS_SUCCESS);
        org.mockito.Mockito.verifyNoInteractions(paymentRepository);
    }
}