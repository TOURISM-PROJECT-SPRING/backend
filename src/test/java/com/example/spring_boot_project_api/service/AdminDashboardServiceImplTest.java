package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.spring_boot_project_api.dto.response.AdminDashboardStatsDTO;
import com.example.spring_boot_project_api.dto.response.BookingFeedPageResponse;
import com.example.spring_boot_project_api.dto.response.UnifiedBookingResponse;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.PromotionRepository;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TourBookingRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.impl.AdminDashboardServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomBookingRepository roomBookingRepository;
    @Mock
    private TicketBookingRepository ticketBookingRepository;
    @Mock
    private FoodOrderRepository foodOrderRepository;
    @Mock
    private TourBookingRepository tourBookingRepository;
    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private AdminOwnerBookingService bookingService;

    @InjectMocks
    private AdminDashboardServiceImpl dashboardService;

    @Test
    void getDashboardStats_aggregatesRealRepositoryValues() {
        when(userRepository.count()).thenReturn(5L);
        when(roomBookingRepository.count()).thenReturn(2L);
        when(ticketBookingRepository.count()).thenReturn(3L);
        when(foodOrderRepository.count()).thenReturn(4L);
        when(tourBookingRepository.count()).thenReturn(1L);

        when(roomBookingRepository.sumNonCancelledRevenue()).thenReturn(new BigDecimal("100.00"));
        when(ticketBookingRepository.sumNonCancelledRevenue()).thenReturn(new BigDecimal("50.00"));
        when(foodOrderRepository.sumNonCancelledRevenue()).thenReturn(new BigDecimal("30.00"));
        when(tourBookingRepository.sumNonCancelledRevenue()).thenReturn(new BigDecimal("20.00"));

        when(roomBookingRepository.countByStatusIgnoreCase("PENDING")).thenReturn(1L);
        when(ticketBookingRepository.countByStatusIgnoreCase("PENDING")).thenReturn(1L);
        when(foodOrderRepository.countByStatusIgnoreCase("PENDING")).thenReturn(0L);
        when(tourBookingRepository.countByStatusIgnoreCase("PENDING")).thenReturn(0L);

        when(promotionRepository.countByStatus("ACTIVE")).thenReturn(3L);

        UnifiedBookingResponse recent = UnifiedBookingResponse.builder().id("ROOM-1").build();
        when(bookingService.getAdminBookings(isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(BookingFeedPageResponse.builder().content(List.of(recent)).build());

        when(roomBookingRepository.findByCreatedAtAfter(any())).thenReturn(Collections.emptyList());
        when(ticketBookingRepository.findByCreatedAtAfter(any())).thenReturn(Collections.emptyList());
        when(foodOrderRepository.findByCreatedAtAfter(any())).thenReturn(Collections.emptyList());
        when(tourBookingRepository.findByCreatedAtAfter(any())).thenReturn(Collections.emptyList());

        AdminDashboardStatsDTO stats = dashboardService.getDashboardStats();

        assertThat(stats.getTotalUsers()).isEqualTo(5L);
        assertThat(stats.getTotalBookings()).isEqualTo(10L);
        assertThat(stats.getBookingBreakdown())
                .containsEntry("ROOM", 2L)
                .containsEntry("TICKET", 3L)
                .containsEntry("FOOD_ORDER", 4L)
                .containsEntry("TOUR", 1L);
        assertThat(stats.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(stats.getPendingOrders()).isEqualTo(2L);
        assertThat(stats.getActivePromotions()).isEqualTo(3L);
        assertThat(stats.getRevenueTrend()).hasSize(6);
        assertThat(stats.getRevenueTrend().get(0)).containsEntry("revenue", BigDecimal.ZERO);
        assertThat(stats.getRecentBookings()).hasSize(1);
        assertThat(stats.getRecentBookings().get(0).getId()).isEqualTo("ROOM-1");
    }
}