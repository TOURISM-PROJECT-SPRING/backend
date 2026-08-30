package com.example.spring_boot_project_api.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketBookingRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;

    @Size(max = 70, message = "Payment method must not exceed 70 characters")
    private String paymentMethod;
}
