package com.example.spring_boot_project_api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.response.AdminDashboardStatsDTO;
import com.example.spring_boot_project_api.dto.response.UnifiedBookingResponse;
import com.example.spring_boot_project_api.model.FoodOrders;
import com.example.spring_boot_project_api.model.RoomBookings;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.model.TourBookings;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.PromotionRepository;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TourBookingRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.AdminDashboardService;
import com.example.spring_boot_project_api.service.AdminOwnerBookingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final TicketBookingRepository ticketBookingRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final TourBookingRepository tourBookingRepository;
    private final PromotionRepository promotionRepository;
    private final AdminOwnerBookingService bookingService;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsDTO getDashboardStats() {
        long totalUsers = userRepository.count();

        long roomCount = roomBookingRepository.count();
        long ticketCount = ticketBookingRepository.count();
        long foodCount = foodOrderRepository.count();
        long tourCount = tourBookingRepository.count();
        long totalBookings = roomCount + ticketCount + foodCount + tourCount;

        BigDecimal totalRevenue = roomBookingRepository.sumNonCancelledRevenue()
                .add(ticketBookingRepository.sumNonCancelledRevenue())
                .add(foodOrderRepository.sumNonCancelledRevenue())
                .add(tourBookingRepository.sumNonCancelledRevenue());

        long pendingOrders = roomBookingRepository.countByStatusIgnoreCase("PENDING")
                + ticketBookingRepository.countByStatusIgnoreCase("PENDING")
                + foodOrderRepository.countByStatusIgnoreCase("PENDING")
                + tourBookingRepository.countByStatusIgnoreCase("PENDING");

        long activePromotions = promotionRepository.countByStatus("ACTIVE");

        List<UnifiedBookingResponse> recentBookings =
                bookingService.getAdminBookings(null, null, null, 0, 6).getContent();

        return AdminDashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalBookings(totalBookings)
                .bookingBreakdown(Map.of(
                        "ROOM", roomCount,
                        "TICKET", ticketCount,
                        "FOOD_ORDER", foodCount,
                        "TOUR", tourCount))
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .activePromotions(activePromotions)
                .revenueTrend(buildRevenueTrend())
                .recentBookings(recentBookings)
                .build();
    }

    /**
     * Aggregate non-cancelled revenue for the last 6 calendar months from real
     * booking rows, filling missing months with zero.
     */
    private List<Map<String, Object>> buildRevenueTrend() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMonths(5)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        Map<YearMonth, BigDecimal> byMonth = new TreeMap<>();

        accumulateRooms(roomBookingRepository.findByCreatedAtAfter(start), byMonth);
        accumulateTickets(ticketBookingRepository.findByCreatedAtAfter(start), byMonth);
        accumulateFoodOrders(foodOrderRepository.findByCreatedAtAfter(start), byMonth);
        accumulateTours(tourBookingRepository.findByCreatedAtAfter(start), byMonth);

        List<Map<String, Object>> trend = new ArrayList<>();
        YearMonth ymStart = YearMonth.from(start);
        YearMonth ymEnd = YearMonth.from(end);
        for (YearMonth ym = ymStart; !ym.isAfter(ymEnd); ym = ym.plusMonths(1)) {
            Map<String, Object> m = new HashMap<>();
            m.put("label", ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            m.put("revenue", byMonth.getOrDefault(ym, BigDecimal.ZERO));
            trend.add(m);
        }
        return trend;
    }

    private void accumulateRooms(List<RoomBookings> rows, Map<YearMonth, BigDecimal> byMonth) {
        rows.stream()
                .filter(rb -> rb.getCreatedAt() != null && rb.getAmount() != null)
                .filter(rb -> !"CANCELLED".equalsIgnoreCase(rb.getStatus()))
                .forEach(rb -> byMonth.merge(YearMonth.from(rb.getCreatedAt()),
                        rb.getAmount(), BigDecimal::add));
    }

    private void accumulateTickets(List<TicketBookings> rows, Map<YearMonth, BigDecimal> byMonth) {
        rows.stream()
                .filter(tb -> tb.getCreatedAt() != null && tb.getTotalPrice() != null)
                .filter(tb -> !"CANCELLED".equalsIgnoreCase(tb.getStatus()))
                .forEach(tb -> byMonth.merge(YearMonth.from(tb.getCreatedAt()),
                        tb.getTotalPrice(), BigDecimal::add));
    }

    private void accumulateFoodOrders(List<FoodOrders> rows, Map<YearMonth, BigDecimal> byMonth) {
        rows.stream()
                .filter(fo -> fo.getCreatedAt() != null && fo.getTotalPrice() != null)
                .filter(fo -> !"CANCELLED".equalsIgnoreCase(fo.getStatus()))
                .forEach(fo -> byMonth.merge(YearMonth.from(fo.getCreatedAt()),
                        fo.getTotalPrice(), BigDecimal::add));
    }

    private void accumulateTours(List<TourBookings> rows, Map<YearMonth, BigDecimal> byMonth) {
        rows.stream()
                .filter(tb -> tb.getCreatedAt() != null && tb.getTotalPrice() != null)
                .filter(tb -> !"CANCELLED".equalsIgnoreCase(tb.getStatus()))
                .forEach(tb -> byMonth.merge(YearMonth.from(tb.getCreatedAt()),
                        tb.getTotalPrice(), BigDecimal::add));
    }
}