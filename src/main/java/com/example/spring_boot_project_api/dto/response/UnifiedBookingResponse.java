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
public class UnifiedBookingResponse {

    private String id;
    private Long rawId;
    private String bookingType;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String serviceName;
    private Long ownerId;
    private String ownerName;
    private BigDecimal totalAmount;
    private Integer quantity;
    private String status;
    private String paymentMethod;
    private String bookingDate;
    private LocalDateTime createdAt;
    private List<String> items;
}
