package com.example.spring_boot_project_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.BakongCheckStatusRequest;
import com.example.spring_boot_project_api.dto.request.BakongQrGenerateRequest;
import com.example.spring_boot_project_api.dto.response.BakongCheckStatusResponse;
import com.example.spring_boot_project_api.dto.response.BakongQrResponse;
import com.example.spring_boot_project_api.service.BakongPaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller handling National Bank of Cambodia (NBC) Bakong KHQR payments.
 * Provides endpoints to generate dynamic KHQR, poll transaction status by MD5 hash,
 * and simulate developer sandbox payments.
 */
@RestController
@RequestMapping("/api/v1/bakong")
@RequiredArgsConstructor
public class BakongKhqrController {

    private final BakongPaymentService bakongPaymentService;

    /**
     * Generates a dynamic Bakong KHQR code, computes its MD5 hash, and produces a Base64 QR image.
     */
    @PostMapping("/generate-qr")
    public ResponseEntity<BakongQrResponse> generateQr(
            @Valid @RequestBody BakongQrGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bakongPaymentService.generateKhqr(request));
    }

    /**
     * Checks payment transaction status via Bakong Open API (check_transaction_by_md5).
     */
    @PostMapping("/check-status")
    public ResponseEntity<BakongCheckStatusResponse> checkStatus(
            @Valid @RequestBody BakongCheckStatusRequest request) {
        return ResponseEntity.ok(bakongPaymentService.checkStatus(request));
    }

    /**
     * Development sandbox helper endpoint to simulate customer payment confirmation.
     */
    @PostMapping("/simulate-payment")
    public ResponseEntity<BakongCheckStatusResponse> simulatePayment(
            @RequestParam String md5) {
        return ResponseEntity.ok(bakongPaymentService.simulatePayment(md5));
    }
}
