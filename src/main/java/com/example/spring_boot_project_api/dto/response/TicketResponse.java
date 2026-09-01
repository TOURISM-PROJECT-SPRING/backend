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
public class TicketResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private Boolean isAvailable;
    private Long tourismPlaceId;
    private String tourismPlaceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
