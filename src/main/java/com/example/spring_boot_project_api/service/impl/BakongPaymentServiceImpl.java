package com.example.spring_boot_project_api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.spring_boot_project_api.config.BakongConfig;
import com.example.spring_boot_project_api.dto.request.BakongCheckStatusRequest;
import com.example.spring_boot_project_api.dto.request.BakongQrGenerateRequest;
import com.example.spring_boot_project_api.dto.response.BakongCheckStatusResponse;
import com.example.spring_boot_project_api.dto.response.BakongQrResponse;
import com.example.spring_boot_project_api.enums.PaymentMethod;
import com.example.spring_boot_project_api.enums.PaymentStatus;
import com.example.spring_boot_project_api.mapper.TicketBookingMapper;
import com.example.spring_boot_project_api.model.FoodOrders;
import com.example.spring_boot_project_api.model.Payments;
import com.example.spring_boot_project_api.model.RoomBookings;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.model.TourBookings;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.PaymentRepository;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TourBookingRepository;
import com.example.spring_boot_project_api.service.BakongPaymentService;
import com.example.spring_boot_project_api.util.KhqrGenerator;
import com.example.spring_boot_project_api.util.QrCodeUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BakongPaymentServiceImpl implements BakongPaymentService {

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_FAILED = "FAILED";

    private final BakongConfig bakongConfig;
    private final RestTemplate restTemplate;
    private final TicketBookingRepository ticketBookingRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final TourBookingRepository tourBookingRepository;
    private final PaymentRepository paymentRepository;

    /**
     * In-memory transaction registry for tracking KHQR sessions and sandbox mode.
     */
    private final Map<String, KhqrTransactionRecord> transactionStore = new ConcurrentHashMap<>();

    public BakongPaymentServiceImpl(
            BakongConfig bakongConfig,
            @Qualifier("bakongRestTemplate") RestTemplate restTemplate,
            TicketBookingRepository ticketBookingRepository,
            RoomBookingRepository roomBookingRepository,
            FoodOrderRepository foodOrderRepository,
            TourBookingRepository tourBookingRepository,
            PaymentRepository paymentRepository) {
        this.bakongConfig = bakongConfig;
        this.restTemplate = restTemplate;
        this.ticketBookingRepository = ticketBookingRepository;
        this.roomBookingRepository = roomBookingRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.tourBookingRepository = tourBookingRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public BakongQrResponse generateKhqr(BakongQrGenerateRequest request) {
        String currency = (request.getCurrency() != null && !request.getCurrency().isBlank())
                ? request.getCurrency().toUpperCase()
                : "USD";

        String billNumber = "BK-" + System.currentTimeMillis();

        // Generate standards-compliant EMVCo KHQR String and 32-character MD5 hash
        KhqrGenerator.KhqrPayload khqr = KhqrGenerator.generateDynamicKhqr(
                bakongConfig.getMerchantAccount(),
                bakongConfig.getMerchantName(),
                bakongConfig.getMerchantCity(),
                request.getAmount(),
                currency,
                billNumber,
                "STORE-01",
                "POS-01");

        // Render QR Code Base64 PNG image directly for zero-dependency frontend display
        String qrImage = QrCodeUtil.generateBase64(khqr.getQrString(), 340);

        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusMinutes(bakongConfig.getQrExpiryMinutes());

        KhqrTransactionRecord record = new KhqrTransactionRecord(
                khqr.getMd5(),
                billNumber,
                request.getAmount(),
                currency,
                request.getBookingId(),
                request.getBookingType(),
                STATUS_PENDING,
                createdAt,
                expiresAt,
                null,
                null);

        transactionStore.put(khqr.getMd5().toLowerCase(), record);

        log.info("Generated Bakong KHQR for amount: {} {}, MD5: {}, bill: {}",
                request.getAmount(), currency, khqr.getMd5(), billNumber);

        return BakongQrResponse.builder()
                .qrString(khqr.getQrString())
                .qrImage(qrImage)
                .md5(khqr.getMd5().toLowerCase())
                .billNumber(billNumber)
                .amount(request.getAmount())
                .currency(currency)
                .merchantName(bakongConfig.getMerchantName())
                .merchantAccount(bakongConfig.getMerchantAccount())
                .bookingId(request.getBookingId())
                .bookingType(request.getBookingType())
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    @Transactional
    public BakongCheckStatusResponse checkStatus(BakongCheckStatusRequest request) {
        String md5 = request.getMd5().toLowerCase().trim();
        KhqrTransactionRecord record = transactionStore.get(md5);

        // Check expiration
        if (record != null && LocalDateTime.now().isAfter(record.expiresAt)
                && !STATUS_SUCCESS.equals(record.status)) {
            record.status = STATUS_EXPIRED;
            return buildStatusResponse(record, "QR code has expired. Please regenerate.");
        }

        // 1. If already confirmed in store
        if (record != null && STATUS_SUCCESS.equals(record.status)) {
            return buildStatusResponse(record, "Payment successfully confirmed.");
        }

        // 2. Query live Bakong Open API if token is configured
        if (bakongConfig.getApiToken() != null && !bakongConfig.getApiToken().isBlank()) {
            try {
                BakongCheckStatusResponse liveResponse = callBakongOpenApi(md5, record);
                if (STATUS_SUCCESS.equals(liveResponse.getStatus())) {
                    applyBookingConfirmation(record != null ? record.bookingId : request.getBookingId(),
                            record != null ? record.bookingType : request.getBookingType(),
                            liveResponse.getTransactionId(), record);
                    if (record != null) {
                        record.status = STATUS_SUCCESS;
                        record.paidAt = LocalDateTime.now();
                        record.transactionId = liveResponse.getTransactionId();
                    }
                    return liveResponse;
                }
            } catch (Exception e) {
                log.warn("Bakong Open API query encountered exception: {}", e.getMessage());
            }
        }

        // 3. Pending state
        if (record != null) {
            return buildStatusResponse(record, "Payment is pending customer scan and confirmation.");
        }

        return BakongCheckStatusResponse.builder()
                .status(STATUS_PENDING)
                .message("Awaiting transaction confirmation")
                .md5(md5)
                .bookingId(request.getBookingId())
                .bookingType(request.getBookingType())
                .build();
    }

    @Override
    @Transactional
    public BakongCheckStatusResponse simulatePayment(String md5) {
        String cleanMd5 = md5.toLowerCase().trim();
        KhqrTransactionRecord record = transactionStore.get(cleanMd5);

        if (record == null) {
            record = new KhqrTransactionRecord(
                    cleanMd5,
                    "SIM-" + System.currentTimeMillis(),
                    BigDecimal.valueOf(25.00),
                    "USD",
                    null,
                    "TICKET",
                    STATUS_SUCCESS,
                    LocalDateTime.now().minusMinutes(1),
                    LocalDateTime.now().plusMinutes(4),
                    LocalDateTime.now(),
                    "TX-" + System.currentTimeMillis());
            transactionStore.put(cleanMd5, record);
        } else {
            record.status = STATUS_SUCCESS;
            record.paidAt = LocalDateTime.now();
            record.transactionId = "TX-SIM-" + System.currentTimeMillis();
        }

        applyBookingConfirmation(record.bookingId, record.bookingType, record.transactionId, record);

        log.info("Simulated Bakong payment success for MD5: {}, bookingId: {}", cleanMd5, record.bookingId);
        return buildStatusResponse(record, "Sandbox test payment confirmed successfully.");
    }

    /**
     * Calls the live Bakong Open API endpoint (/check_transaction_by_md5).
     */
    @SuppressWarnings("unchecked")
    private BakongCheckStatusResponse callBakongOpenApi(String md5, KhqrTransactionRecord record) {
        String url = bakongConfig.getApiUrl() + "/check_transaction_by_md5";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(bakongConfig.getApiToken());

        Map<String, String> requestBody = Map.of("md5", md5);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        Map<String, Object> body = response.getBody();

        if (body != null) {
            Object code = body.get("responseCode");
            boolean isSuccess = Integer.valueOf(0).equals(code) || "0".equals(String.valueOf(code));
            if (isSuccess && body.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                String txHash = String.valueOf(data.getOrDefault("hash", md5));
                String fromAccount = String.valueOf(data.getOrDefault("fromAccountId", ""));
                String toAccount = String.valueOf(data.getOrDefault("toAccountId", ""));

                return BakongCheckStatusResponse.builder()
                        .status(STATUS_SUCCESS)
                        .message("Payment verified via Bakong Open API")
                        .md5(md5)
                        .transactionId(txHash)
                        .amount(record != null ? record.amount : null)
                        .currency(record != null ? record.currency : "USD")
                        .fromAccountId(fromAccount)
                        .toAccountId(toAccount)
                        .bookingId(record != null ? record.bookingId : null)
                        .bookingType(record != null ? record.bookingType : null)
                        .paidAt(LocalDateTime.now())
                        .build();
            }
        }

        return BakongCheckStatusResponse.builder()
                .status(STATUS_PENDING)
                .message("Transaction not yet settled on Bakong network")
                .md5(md5)
                .bookingId(record != null ? record.bookingId : null)
                .bookingType(record != null ? record.bookingType : null)
                .build();
    }

    /**
     * Automatically confirms the associated tour, room, or food booking and persists a
     * successful KhqrPayment record in the payments table.
     */
    private void applyBookingConfirmation(Long bookingId, String bookingType, String transactionId,
            KhqrTransactionRecord record) {
        if (bookingId == null) {
            return;
        }

        try {
            String type = (bookingType != null) ? bookingType.toUpperCase() : "TICKET";
            switch (type) {
                case "TICKET" -> ticketBookingRepository.findById(bookingId).ifPresent(b -> {
                    b.setStatus(TicketBookingMapper.STATUS_CONFIRMED);
                    TicketBookings saved = ticketBookingRepository.save(b);
                    persistPayment(saved, saved.getTotalPrice(), type, transactionId, record);
                    log.info("Auto-confirmed TicketBooking #{}", bookingId);
                });
                case "ROOM" -> roomBookingRepository.findById(bookingId).ifPresent(b -> {
                    b.setStatus("CONFIRMED");
                    RoomBookings saved = roomBookingRepository.save(b);
                    persistPayment(saved, saved.getAmount(), type, transactionId, record);
                    log.info("Auto-confirmed RoomBooking #{}", bookingId);
                });
                case "FOOD", "FOOD_ORDER" -> foodOrderRepository.findById(bookingId).ifPresent(b -> {
                    b.setStatus("CONFIRMED");
                    FoodOrders saved = foodOrderRepository.save(b);
                    persistPayment(saved, saved.getTotalPrice(), type, transactionId, record);
                    log.info("Auto-confirmed FoodOrder #{}", bookingId);
                });
                case "TOUR", "TOUR_BOOKING" -> tourBookingRepository.findById(bookingId).ifPresent(b -> {
                    b.setStatus("CONFIRMED");
                    TourBookings saved = tourBookingRepository.save(b);
                    persistPayment(saved, saved.getTotalPrice(), type, transactionId, record);
                    log.info("Auto-confirmed TourBooking #{}", bookingId);
                });
                default -> log.debug("Unknown booking type: {}, skipping entity update", bookingType);
            }
        } catch (Exception e) {
            log.error("Failed to auto-confirm booking #{} (type: {}): {}", bookingId, bookingType, e.getMessage());
        }
    }

    /**
     * Persists a successful Bakong KHQR payment against the referenced booking. Skips the insert
     * when the same transaction id was already recorded (idempotency guard).
     */
    private void persistPayment(Object booking, BigDecimal amount, String bookingType,
            String transactionId, KhqrTransactionRecord record) {
        if (booking == null || amount == null) {
            return;
        }

        String txId = (transactionId == null || transactionId.isBlank())
                ? "BK-" + System.currentTimeMillis() : transactionId;
        if (txId.length() > 100) {
            txId = txId.substring(0, 100);
        }

        if (!paymentRepository.findByTransactionId(txId).isEmpty()) {
            log.debug("Payment transaction {} already recorded, skipping", txId);
            return;
        }

        String reference = (record != null && record.billNumber != null)
                ? record.billNumber : "BK-" + System.currentTimeMillis();

        Payments payment = new Payments();
        payment.setPaymentReference(reference);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.BAKONG_KHQR);
        payment.setTransactionId(txId);
        payment.setQrMd5(record != null ? record.md5 : null);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setExpiredAt(record != null ? record.expiresAt : null);

        String type = (bookingType != null) ? bookingType.toUpperCase() : "";
        switch (type) {
            case "TICKET" -> payment.setTicketBookings((TicketBookings) booking);
            case "ROOM" -> payment.setRoomBookings((RoomBookings) booking);
            case "FOOD", "FOOD_ORDER" -> payment.setFoodOrders((FoodOrders) booking);
            case "TOUR", "TOUR_BOOKING" -> payment.setTourBookings((TourBookings) booking);
            default -> {
                log.debug("Unknown booking type: {}, payment not persisted", type);
                return;
            }
        }

        paymentRepository.save(payment);
        log.info("Persisted Bakong KHQR payment record (ref: {}, tx: {}) for booking type: {}",
                reference, txId, type);
    }

    private BakongCheckStatusResponse buildStatusResponse(KhqrTransactionRecord record, String message) {
        return BakongCheckStatusResponse.builder()
                .status(record.status)
                .message(message)
                .md5(record.md5)
                .transactionId(record.transactionId)
                .amount(record.amount)
                .currency(record.currency)
                .bookingId(record.bookingId)
                .bookingType(record.bookingType)
                .paidAt(record.paidAt)
                .build();
    }

    private static class KhqrTransactionRecord {
        final String md5;
        final String billNumber;
        final BigDecimal amount;
        final String currency;
        final Long bookingId;
        final String bookingType;
        String status;
        final LocalDateTime createdAt;
        final LocalDateTime expiresAt;
        LocalDateTime paidAt;
        String transactionId;

        KhqrTransactionRecord(String md5, String billNumber, BigDecimal amount, String currency,
                              Long bookingId, String bookingType, String status,
                              LocalDateTime createdAt, LocalDateTime expiresAt,
                              LocalDateTime paidAt, String transactionId) {
            this.md5 = md5;
            this.billNumber = billNumber;
            this.amount = amount;
            this.currency = currency;
            this.bookingId = bookingId;
            this.bookingType = bookingType;
            this.status = status;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.paidAt = paidAt;
            this.transactionId = transactionId;
        }
    }
}
