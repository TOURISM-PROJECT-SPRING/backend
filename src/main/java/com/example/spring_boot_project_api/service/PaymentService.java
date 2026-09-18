package com.example.spring_boot_project_api.service;

import com.example.spring_boot_project_api.dto.request.PaymentCallbackRequest;
import com.example.spring_boot_project_api.dto.request.PaymentProcessRequest;
import com.example.spring_boot_project_api.dto.response.PaymentInitiationResponse;
import com.example.spring_boot_project_api.dto.response.PaymentProcessResponse;
import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;

public interface PaymentService {

    PaymentInitiationResponse initiatePayment(Long bookingId, String paymentMethod);

    TicketBookingResponse handleCallback(PaymentCallbackRequest request);

    PaymentProcessResponse processPayment(PaymentProcessRequest request);
}
