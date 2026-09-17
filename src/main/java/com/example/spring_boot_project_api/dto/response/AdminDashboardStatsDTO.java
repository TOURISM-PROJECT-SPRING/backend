package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDTO {

    private long totalUsers;
    private long totalBookings;
    private Map<String, Long> bookingBreakdown;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long activePromotions;
    private List<Map<String, Object>> revenueTrend;
    private List<UnifiedBookingResponse> recentBookings;
}