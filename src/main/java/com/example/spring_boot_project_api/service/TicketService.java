package com.example.spring_boot_project_api.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.spring_boot_project_api.dto.request.TicketRequest;
import com.example.spring_boot_project_api.dto.response.TicketResponse;

public interface TicketService {

    List<TicketResponse> findAll();

    List<TicketResponse> findAvailable();

    TicketResponse findById(Long id);

    List<TicketResponse> findByTourismPlaceId(Long tourismPlaceId);

    List<TicketResponse> search(String keyword);

    List<TicketResponse> findByPriceBetween(BigDecimal min, BigDecimal max);

    TicketResponse create(TicketRequest request);

    TicketResponse update(Long id, TicketRequest request);

    void delete(Long id);
}
