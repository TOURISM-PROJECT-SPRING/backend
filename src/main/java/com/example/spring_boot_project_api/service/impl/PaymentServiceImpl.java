package com.example.spring_boot_project_api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.PaymentCallbackRequest;
import com.example.spring_boot_project_api.dto.request.PaymentProcessRequest;
import com.example.spring_boot_project_api.dto.response.PaymentInitiationResponse;
import com.example.spring_boot_project_api.dto.response.PaymentProcessResponse;
import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;
import com.example.spring_boot_project_api.enums.PaymentMethod;
import com.example.spring_boot_project_api.enums.PaymentStatus;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.RoomBookingMapper;
import com.example.spring_boot_project_api.mapper.TicketBookingMapper;
import com.example.spring_boot_project_api.mapper.TourBookingMapper;
import com.example.spring_boot_project_api.model.FoodOrders;
import com.example.spring_boot_project_api.model.Payments;
import com.example.spring_boot_project_api.model.RoomBookings;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.model.TourBookings;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.PaymentRepository;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TourBookingRepository;
import com.example.spring_boot_project_api.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_SUCCESS = "SUCCESS";
    public static final String PAYMENT_FAILED = "FAILED";

    private final TicketBookingRepository ticketBookingRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final TourBookingRepository tourBookingRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final PaymentRepository paymentRepository;

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

    @Override
    @Transactional
    public PaymentProcessResponse processPayment(PaymentProcessRequest request) {
        String transactionId = "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<Long> confirmedRoomIds = new ArrayList<>();
        List<Long> confirmedTicketIds = new ArrayList<>();
        List<Long> confirmedFoodIds = new ArrayList<>();
        List<Long> confirmedTourIds = new ArrayList<>();

        if (request.getRoomBookingIds() != null) {
            for (Long roomId : request.getRoomBookingIds()) {
                RoomBookings roomBooking = roomBookingRepository.findById(roomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Room Booking", roomId));
                totalAmount = totalAmount.add(roomBooking.getAmount() != null ? roomBooking.getAmount() : BigDecimal.ZERO);
                confirmedRoomIds.add(roomId);
            }
        }

        if (request.getTicketBookingIds() != null) {
            for (Long ticketId : request.getTicketBookingIds()) {
                TicketBookings ticketBooking = ticketBookingRepository.findById(ticketId)
                        .orElseThrow(() -> new ResourceNotFoundException("Ticket Booking", ticketId));
                totalAmount = totalAmount.add(ticketBooking.getTotalPrice() != null ? ticketBooking.getTotalPrice() : BigDecimal.ZERO);
                confirmedTicketIds.add(ticketId);
            }
        }

        if (request.getFoodOrderIds() != null) {
            for (Long foodId : request.getFoodOrderIds()) {
                FoodOrders foodOrder = foodOrderRepository.findById(foodId)
                        .orElseThrow(() -> new ResourceNotFoundException("Food Order", foodId));
                totalAmount = totalAmount.add(foodOrder.getTotalPrice() != null ? foodOrder.getTotalPrice() : BigDecimal.ZERO);
                confirmedFoodIds.add(foodId);
            }
        }

        if (request.getTourBookingIds() != null) {
            for (Long tourId : request.getTourBookingIds()) {
                TourBookings tourBooking = tourBookingRepository.findById(tourId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tour Booking", tourId));
                totalAmount = totalAmount.add(tourBooking.getTotalPrice() != null ? tourBooking.getTotalPrice() : BigDecimal.ZERO);
                confirmedTourIds.add(tourId);
            }
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("No valid bookings provided for payment");
        }

        Payments payment = new Payments();
        payment.setPaymentReference("PAY-REF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        if (!confirmedRoomIds.isEmpty()) {
            payment.setRoomBookings(roomBookingRepository.findById(confirmedRoomIds.get(0)).orElse(null));
        }
        if (!confirmedTicketIds.isEmpty()) {
            payment.setTicketBookings(ticketBookingRepository.findById(confirmedTicketIds.get(0)).orElse(null));
        }
        if (!confirmedFoodIds.isEmpty()) {
            payment.setFoodOrders(foodOrderRepository.findById(confirmedFoodIds.get(0)).orElse(null));
        }
        if (!confirmedTourIds.isEmpty()) {
            payment.setTourBookings(tourBookingRepository.findById(confirmedTourIds.get(0)).orElse(null));
        }

        paymentRepository.save(payment);

        for (Long roomId : confirmedRoomIds) {
            RoomBookings roomBooking = roomBookingRepository.findById(roomId).orElse(null);
            if (roomBooking != null) {
                roomBooking.setPaymentMethod(request.getPaymentMethod());
                roomBookingRepository.save(roomBooking);
            }
        }
        for (Long ticketId : confirmedTicketIds) {
            TicketBookings ticketBooking = ticketBookingRepository.findById(ticketId).orElse(null);
            if (ticketBooking != null) {
                ticketBooking.setStatus(TicketBookingMapper.STATUS_CONFIRMED);
                ticketBooking.setPaymentMethod(request.getPaymentMethod());
                ticketBookingRepository.save(ticketBooking);
            }
        }
        for (Long tourId : confirmedTourIds) {
            TourBookings tourBooking = tourBookingRepository.findById(tourId).orElse(null);
            if (tourBooking != null) {
                tourBooking.setStatus(TourBookingMapper.STATUS_CONFIRMED);
                tourBooking.setPaymentMethod(request.getPaymentMethod());
                tourBookingRepository.save(tourBooking);
            }
        }

        return PaymentProcessResponse.builder()
                .paymentId(payment.getId())
                .transactionId(transactionId)
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .status(PAYMENT_SUCCESS)
                .confirmedRoomBookingIds(confirmedRoomIds)
                .confirmedTicketBookingIds(confirmedTicketIds)
                .confirmedFoodOrderIds(confirmedFoodIds)
                .confirmedTourBookingIds(confirmedTourIds)
                .paidAt(payment.getPaidAt())
                .build();
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
