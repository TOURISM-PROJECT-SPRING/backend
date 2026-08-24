package com.example.spring_boot_project_api.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Tickets;

@Repository
public interface TicketRepository extends JpaRepository<Tickets, Long> {

    List<Tickets> findByTourismPlacesId(Long tourismPlaceId);

    List<Tickets> findByTourismPlacesIdAndIsAvailableTrue(Long tourismPlaceId);

    List<Tickets> findByIsAvailableTrue();

    List<Tickets> findByNameContainingIgnoreCase(String keyword);

    List<Tickets> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
