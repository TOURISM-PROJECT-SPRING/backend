package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BakongCheckStatusResponse {

    /**
     * "SUCCESS", "PENDING", "EXPIRED", or "FAILED"
     */
    private String status;

    private String message;

    private String md5;

    private String transactionId;

    private BigDecimal amount;

    private String currency;

    private String fromAccountId;

    private String toAccountId;

    private Long bookingId;

    private String bookingType;

    private LocalDateTime paidAt;
}
