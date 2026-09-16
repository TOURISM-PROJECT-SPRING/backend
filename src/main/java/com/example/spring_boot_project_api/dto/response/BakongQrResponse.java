package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BakongQrResponse {

    private String qrString;

    /**
     * Base64 PNG image (data:image/png;base64,...) for instant zero-dependency display.
     */
    private String qrImage;

    /**
     * 32-character lowercase MD5 hash used by Bakong's check_transaction_by_md5 endpoint.
     */
    private String md5;

    private String billNumber;

    private BigDecimal amount;

    private String currency;

    private String merchantName;

    private String merchantAccount;

    private Long bookingId;

    private String bookingType;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
}
