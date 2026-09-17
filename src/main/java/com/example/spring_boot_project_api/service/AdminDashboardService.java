package com.example.spring_boot_project_api.service;

import com.example.spring_boot_project_api.dto.response.AdminDashboardStatsDTO;

public interface AdminDashboardService {

    /**
     * Fetch global platform statistics for the Admin Dashboard
     * (users, bookings, revenue, pending orders, active promotions, revenue trend).
     */
    AdminDashboardStatsDTO getDashboardStats();
}