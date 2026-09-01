package com.example.spring_boot_project_api.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.TicketRequest;
import com.example.spring_boot_project_api.dto.response.TicketResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.TicketMapper;
import com.example.spring_boot_project_api.model.Tickets;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.repository.TicketRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.service.TicketService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TourismPlaceRepository tourismPlaceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findAll() {
        return TicketMapper.toResponseList(ticketRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findAvailable() {
        return TicketMapper.toResponseList(ticketRepository.findByIsAvailableTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse findById(Long id) {
        Tickets ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
        return TicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findByTourismPlaceId(Long tourismPlaceId) {
        if (!tourismPlaceRepository.existsById(tourismPlaceId)) {
            throw new ResourceNotFoundException("Tourism place", tourismPlaceId);
        }
        return TicketMapper.toResponseList(
                ticketRepository.findByTourPlacesIdAndIsAvailableTrue(tourismPlaceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> search(String keyword) {
        return TicketMapper.toResponseList(ticketRepository.findByNameContainingIgnoreCase(keyword));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findByPriceBetween(BigDecimal min, BigDecimal max) {
        return TicketMapper.toResponseList(ticketRepository.findByPriceBetween(min, max));
    }

    @Override
    public TicketResponse create(TicketRequest request) {
        TourPlaces place = tourismPlaceRepository.findById(request.getTourismPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourism place", request.getTourismPlaceId()));
        Tickets ticket = TicketMapper.toEntity(request, place);
        Tickets saved = ticketRepository.save(ticket);
        return TicketMapper.toResponse(saved);
    }

    @Override
    public TicketResponse update(Long id, TicketRequest request) {
        Tickets ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
        TourPlaces place = tourismPlaceRepository.findById(request.getTourismPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourism place", request.getTourismPlaceId()));
        TicketMapper.toEntity(ticket, request, place);
        Tickets updated = ticketRepository.save(ticket);
        return TicketMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket", id);
        }
        ticketRepository.deleteById(id);
    }
}
