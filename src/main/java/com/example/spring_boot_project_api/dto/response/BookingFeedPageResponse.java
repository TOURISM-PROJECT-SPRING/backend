package com.example.spring_boot_project_api.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingFeedPageResponse {

    private List<UnifiedBookingResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}