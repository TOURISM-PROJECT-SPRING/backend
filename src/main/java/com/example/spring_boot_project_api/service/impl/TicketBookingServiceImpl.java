package com.example.spring_boot_project_api.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.TicketBookingRequest;
import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.TicketBookingMapper;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.model.Tickets;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TicketRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.TicketBookingService;
import com.example.spring_boot_project_api.util.QrCodeUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketBookingServiceImpl implements TicketBookingService {

    public static final int DEFAULT_DAILY_QUOTA = 200;

    private final TicketBookingRepository ticketBookingRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TicketBookingResponse> findAll() {
        return TicketBookingMapper.toResponseList(ticketBookingRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketBookingResponse findById(Long id) {
        TicketBookings booking = ticketBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket Booking", id));
        return TicketBookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketBookingResponse> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return TicketBookingMapper.toResponseList(
                ticketBookingRepository.findByUserIdOrderByVisiDateDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketBookingResponse> findByTicketId(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket", ticketId);
        }
        return TicketBookingMapper.toResponseList(ticketBookingRepository.findByTicketsId(ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketBookingResponse> findByStatus(String status) {
        return TicketBookingMapper.toResponseList(ticketBookingRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketBookingResponse> findByVisitDate(LocalDate visitDate) {
        return TicketBookingMapper.toResponseList(ticketBookingRepository.findByVisiDate(visitDate));
    }

    @Override
    public TicketBookingResponse create(TicketBookingRequest request) {
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Tickets ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", request.getTicketId()));

        if (Boolean.FALSE.equals(ticket.getIsAvailable())) {
            throw new IllegalArgumentException("Ticket is not available for booking");
        }

        validateVisitDate(request.getVisitDate());
        checkDailyQuota(ticket.getId(), request.getVisitDate(), request.getQuantity());

        TicketBookings booking = TicketBookingMapper.toEntity(request, user, ticket);
        booking.setQrCode(QrCodeUtil.generateToken());
        TicketBookings saved = ticketBookingRepository.save(booking);
        return TicketBookingMapper.toResponse(saved);
    }

    @Override
    public TicketBookingResponse cancel(Long id) {
        TicketBookings booking = getExisting(id);
        if (TicketBookingMapper.STATUS_CANCELLED.equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }
        booking.setStatus(TicketBookingMapper.STATUS_CANCELLED);
        TicketBookings saved = ticketBookingRepository.save(booking);
        return TicketBookingMapper.toResponse(saved);
    }

    @Override
    public TicketBookingResponse markUsed(Long id) {
        TicketBookings booking = getExisting(id);
        if (!TicketBookingMapper.STATUS_CONFIRMED.equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("Only confirmed bookings can be marked as used");
        }
        booking.setStatus(TicketBookingMapper.STATUS_USED);
        TicketBookings saved = ticketBookingRepository.save(booking);
        return TicketBookingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketBookingResponse verify(String qrCode) {
        if (qrCode == null || qrCode.isBlank()) {
            throw new IllegalArgumentException("QR code is required");
        }
        TicketBookings booking = ticketBookingRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid QR code"));
        if (TicketBookingMapper.STATUS_USED.equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("Ticket has already been used");
        }
        if (TicketBookingMapper.STATUS_CANCELLED.equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("Ticket booking is cancelled");
        }
        return TicketBookingMapper.toResponse(booking);
    }

    @Override
    public void delete(Long id) {
        if (!ticketBookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket Booking", id);
        }
        ticketBookingRepository.deleteById(id);
    }

    private TicketBookings getExisting(Long id) {
        return ticketBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket Booking", id));
    }

    private void validateVisitDate(LocalDate visitDate) {
        if (visitDate == null || visitDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Visit date must be today or in the future");
        }
    }

    private void checkDailyQuota(Long ticketId, LocalDate visitDate, Integer requestedQuantity) {
        long booked = ticketBookingRepository.countByTicketsIdAndVisiDate(ticketId, visitDate);
        if (booked + requestedQuantity > DEFAULT_DAILY_QUOTA) {
            throw new IllegalArgumentException("Daily quota reached for this ticket on the selected date");
        }
    }
}
