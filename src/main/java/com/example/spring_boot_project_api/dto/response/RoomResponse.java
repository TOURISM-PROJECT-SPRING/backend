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
public class RoomResponse {

    private Long id;
    private Long hotelId;
    private String hotelName;
    private Long roomTypeId;
    private String roomType;
    private Integer capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}