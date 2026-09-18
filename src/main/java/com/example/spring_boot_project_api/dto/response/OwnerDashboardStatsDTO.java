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
public class OwnerDashboardStatsDTO {

    private Long ownerId;
    private String businessName;
    private long totalBookings;
    private long activeServices;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private List<Map<String, Object>> revenueTrend;
    private List<UnifiedBookingResponse> recentBookings;
}
