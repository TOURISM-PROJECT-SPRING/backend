package com.example.spring_boot_project_api.service;

import com.example.spring_boot_project_api.dto.request.BakongCheckStatusRequest;
import com.example.spring_boot_project_api.dto.request.BakongQrGenerateRequest;
import com.example.spring_boot_project_api.dto.response.BakongCheckStatusResponse;
import com.example.spring_boot_project_api.dto.response.BakongQrResponse;

public interface BakongPaymentService {

    /**
     * Generates an EMVCo-standard dynamic Bakong KHQR string, computes the MD5 hash,
     * and renders the Base64 QR code image.
     */
    BakongQrResponse generateKhqr(BakongQrGenerateRequest request);

    /**
     * Checks the transaction status via Bakong Open API (check_transaction_by_md5).
     * If paid, automatically confirms the corresponding booking.
     */
    BakongCheckStatusResponse checkStatus(BakongCheckStatusRequest request);

    /**
     * Sandbox simulation endpoint allowing developers to test full payment confirmation
     * without real bank funds.
     */
    BakongCheckStatusResponse simulatePayment(String md5);
}
