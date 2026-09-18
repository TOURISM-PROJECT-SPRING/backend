package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessResponse {

    private Long paymentId;
    private String transactionId;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String status;
    private List<Long> confirmedRoomBookingIds;
    private List<Long> confirmedTicketBookingIds;
    private List<Long> confirmedFoodOrderIds;
    private List<Long> confirmedTourBookingIds;
    private LocalDateTime paidAt;
}
