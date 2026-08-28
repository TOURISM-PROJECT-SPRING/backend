package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomBookingResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long roomId;
    private Long hotelId;
    private String hotelName;
    private String roomType;
    private Integer numGuest;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String paymentMethod;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}