package com.example.spring_boot_project_api.service;

import java.io.ByteArrayOutputStream;

import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;

public interface ETicketService {

    byte[] generatePdf(TicketBookingResponse booking);

    ByteArrayOutputStream generatePdfStream(TicketBookingResponse booking);
}
