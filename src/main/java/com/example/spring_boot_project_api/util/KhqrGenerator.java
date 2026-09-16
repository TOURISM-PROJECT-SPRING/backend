package com.example.spring_boot_project_api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Standards-compliant EMVCo KHQR String Generator for National Bank of Cambodia (NBC) Bakong.
 * Generates dynamic/static KHQR strings, calculates CRC-16 CCITT checksum, and produces MD5 transaction hashes.
 */
public final class KhqrGenerator {

    private KhqrGenerator() {}

    public static class KhqrPayload {
        private final String qrString;
        private final String md5;
        private final String billNumber;

        public KhqrPayload(String qrString, String md5, String billNumber) {
            this.qrString = qrString;
            this.md5 = md5;
            this.billNumber = billNumber;
        }

        public String getQrString() {
            return qrString;
        }

        public String getMd5() {
            return md5;
        }

        public String getBillNumber() {
            return billNumber;
        }
    }

    /**
     * Generates an EMVCo-compliant KHQR string and its MD5 hash.
     *
     * @param bakongAccountId The merchant's Bakong ID (e.g. "sovanndomnour@aclb")
     * @param merchantName    Display merchant name (e.g. "SovannDomNour Tourism")
     * @param merchantCity    Merchant city (e.g. "Siem Reap" or "Phnom Penh")
     * @param amount          Transaction amount
     * @param currency        "USD" or "KHR"
     * @param billNumber      Unique invoice or reference identifier
     * @param storeLabel      Optional store label or counter identifier
     * @param terminalLabel   Optional terminal ID
     * @return KhqrPayload containing the final KHQR string and MD5 hash
     */
    public static KhqrPayload generateDynamicKhqr(
            String bakongAccountId,
            String merchantName,
            String merchantCity,
            BigDecimal amount,
            String currency,
            String billNumber,
            String storeLabel,
            String terminalLabel) {

        StringBuilder payload = new StringBuilder();

        // 00: Payload Format Indicator (01)
        payload.append(formatTag("00", "01"));

        // 01: Point of Initiation Method (12 = Dynamic QR, 11 = Static)
        payload.append(formatTag("01", amount != null && amount.compareTo(BigDecimal.ZERO) > 0 ? "12" : "11"));

        // 29: Merchant Account Information (Bakong Account)
        StringBuilder merchantAccount = new StringBuilder();
        merchantAccount.append(formatTag("00", "bakong@nbc"));
        merchantAccount.append(formatTag("01", bakongAccountId != null ? bakongAccountId : "sovanndomnour@aclb"));
        payload.append(formatTag("29", merchantAccount.toString()));

        // 52: Merchant Category Code (4722 = Travel agencies / tour operators)
        payload.append(formatTag("52", "4722"));

        // 53: Transaction Currency (840 = USD, 116 = KHR)
        String currencyCode = "KHR".equalsIgnoreCase(currency) ? "116" : "840";
        payload.append(formatTag("53", currencyCode));

        // 54: Transaction Amount
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            String formattedAmount = "116".equals(currencyCode)
                    ? amount.setScale(0, RoundingMode.HALF_UP).toPlainString()
                    : amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
            payload.append(formatTag("54", formattedAmount));
        }

        // 58: Country Code (KH)
        payload.append(formatTag("58", "KH"));

        // 59: Merchant Name (Max 25 chars)
        String cleanName = (merchantName != null && !merchantName.isBlank()) ? merchantName : "SovannDomNour";
        if (cleanName.length() > 25) {
            cleanName = cleanName.substring(0, 25);
        }
        payload.append(formatTag("59", cleanName));

        // 60: Merchant City (Max 15 chars)
        String cleanCity = (merchantCity != null && !merchantCity.isBlank()) ? merchantCity : "Siem Reap";
        if (cleanCity.length() > 15) {
            cleanCity = cleanCity.substring(0, 15);
        }
        payload.append(formatTag("60", cleanCity));

        // 62: Additional Data Field Template
        StringBuilder additionalData = new StringBuilder();
        if (billNumber != null && !billNumber.isBlank()) {
            additionalData.append(formatTag("01", billNumber));
        }
        if (storeLabel != null && !storeLabel.isBlank()) {
            additionalData.append(formatTag("03", storeLabel));
        }
        if (terminalLabel != null && !terminalLabel.isBlank()) {
            additionalData.append(formatTag("07", terminalLabel));
        }
        if (additionalData.length() > 0) {
            payload.append(formatTag("62", additionalData.toString()));
        }

        // 63: CRC placeholder tag (Length 04)
        payload.append("6304");

        // Calculate CRC16 checksum over entire payload including "6304"
        String crc = calculateCrc16(payload.toString());
        String finalKhqrString = payload.toString().substring(0, payload.length() - 4) + formatTag("63", crc);

        // Calculate MD5 hash over the finalized KHQR string
        String md5 = calculateMd5(finalKhqrString);

        return new KhqrPayload(finalKhqrString, md5, billNumber);
    }

    /**
     * Formats Tag, Length (2 digits), and Value according to EMVCo specs.
     */
    public static String formatTag(String tag, String value) {
        if (value == null) {
            value = "";
        }
        return tag + String.format("%02d", value.getBytes(StandardCharsets.UTF_8).length) + value;
    }

    /**
     * Computes the CRC-16/CCITT-FALSE checksum for EMVCo strings.
     * Polynomial: 0x1021, Initial: 0xFFFF.
     */
    public static String calculateCrc16(String data) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }

    /**
     * Computes a 32-character lowercase MD5 hash.
     */
    public static String calculateMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm unavailable in runtime", e);
        }
    }
}
