package com.example.spring_boot_project_api.service;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.spring_boot_project_api.dto.response.BookingNotificationDTO;
import com.example.spring_boot_project_api.dto.response.UnifiedBookingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyNewBooking(UnifiedBookingResponse booking) {
        BookingNotificationDTO notification = BookingNotificationDTO.builder()
                .eventType("NEW_BOOKING")
                .message("New booking received: " + booking.getId() + " - " + booking.getServiceName())
                .booking(booking)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Broadcasting real-time booking event for ID: {}", booking.getId());

        // 1. Broadcast to system-wide Admin Dashboard
        messagingTemplate.convertAndSend("/topic/admin/bookings", notification);

        // 2. Broadcast to global public bookings feed
        messagingTemplate.convertAndSend("/topic/bookings", notification);

        // 3. Targeted delivery to specific business owner
        if (booking.getOwnerId() != null) {
            String ownerTopic = "/topic/owner/" + booking.getOwnerId() + "/bookings";
            log.info("Sending targeted notification to owner topic: {}", ownerTopic);
            messagingTemplate.convertAndSend(ownerTopic, notification);
        }
    }

    public void notifyStatusChange(UnifiedBookingResponse booking, String newStatus) {
        BookingNotificationDTO notification = BookingNotificationDTO.builder()
                .eventType("STATUS_UPDATED")
                .message("Booking " + booking.getId() + " updated to " + newStatus)
                .booking(booking)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/admin/bookings", notification);
        if (booking.getOwnerId() != null) {
            messagingTemplate.convertAndSend("/topic/owner/" + booking.getOwnerId() + "/bookings", notification);
        }
    }
}
