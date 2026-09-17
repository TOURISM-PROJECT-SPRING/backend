package com.example.spring_boot_project_api.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessRequest {

    private List<Long> roomBookingIds;
    private List<Long> ticketBookingIds;
    private List<Long> foodOrderIds;
    private List<Long> tourBookingIds;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
