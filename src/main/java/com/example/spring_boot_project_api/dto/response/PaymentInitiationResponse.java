package com.example.spring_boot_project_api.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationResponse {

    private Long bookingId;
    private String transactionId;
    private String paymentUrl;
    private String status;
    private LocalDateTime initiatedAt;
}
