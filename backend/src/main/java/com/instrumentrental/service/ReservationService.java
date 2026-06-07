package com.instrumentrental.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.enums.PaymentStatus;
import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.Instrument;
import com.instrumentrental.domain.model.InstrumentModel;
import com.instrumentrental.domain.model.OverdueRecord;
import com.instrumentrental.domain.model.Payment;
import com.instrumentrental.domain.model.Reservation;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.InstrumentModelRepository;
import com.instrumentrental.domain.repository.InstrumentRepository;
import com.instrumentrental.domain.repository.OverdueRecordRepository;
import com.instrumentrental.domain.repository.ReservationRepository;
import com.instrumentrental.domain.repository.UserRepository;
import com.instrumentrental.dto.reservation.CreateReservationRequest;
import com.instrumentrental.dto.reservation.QuoteRequest;
import com.instrumentrental.dto.reservation.QuoteResponse;
import com.instrumentrental.dto.reservation.ReservationResponse;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final InstrumentRepository instrumentRepository;
    private final InstrumentModelRepository instrumentModelRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final DistributedLockService distributedLockService;
    private final InstrumentStateMachine instrumentStateMachine;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final OverdueRecordRepository overdueRecordRepository;

    // ──────────────────────────────────────────────
    //  quote
    // ──────────────────────────────────────────────

    public QuoteResponse quote(QuoteRequest request) {
        InstrumentModel model = instrumentModelRepository.findById(request.getModelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MODEL_NOT_FOUND));

        int availableCount = inventoryService.getAvailableCount(model.getId(), request.getStartTime(), request.getEndTime());

        QuoteResponse quote = pricingService.calculateQuote(model, request.getStartTime(), request.getEndTime(), request.getQuantity());
        quote.setAvailableCount(availableCount);

        return quote;
    }

    // ──────────────────────────────────────────────
    //  createReservation
    // ──────────────────────────────────────────────

    @Transactional
    public List<ReservationResponse> createReservation(CreateReservationRequest request, Long userId) {
        InstrumentModel model = instrumentModelRepository.findById(request.getModelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MODEL_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String lockKey = "reserve:" + model.getId() + ":" + request.getStartTime() + ":" + request.getEndTime();
        distributedLockService.acquireLock(lockKey, 30);

        try {
            List<Instrument> instruments = inventoryService.lockInstruments(
                    model.getId(), request.getStartTime(), request.getEndTime(), request.getQuantity());

            QuoteResponse quote = pricingService.calculateQuote(model,
                    request.getStartTime(), request.getEndTime(), request.getQuantity());
            String priceDetailJson = toJson(quote);

            List<Reservation> reservations = new ArrayList<>();
            for (Instrument instrument : instruments) {
                Reservation reservation = Reservation.builder()
                        .user(user)
                        .instrument(instrument)
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .status(ReservationStatus.UNPAID)
                        .pickupCode(generatePickupCode())
                        .priceDetail(priceDetailJson)
                        .build();
                reservations.add(reservation);
            }

            List<Reservation> saved = reservationRepository.saveAll(reservations);
            log.info("Created {} reservations for user {} model {}", saved.size(), userId, model.getId());

            return saved.stream().map(this::buildResponse).collect(Collectors.toList());
        } finally {
            distributedLockService.releaseLock(lockKey);
        }
    }

    // ──────────────────────────────────────────────
    //  cancelReservation
    // ──────────────────────────────────────────────

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        ReservationStatus currentStatus = reservation.getStatus();
        if (currentStatus == ReservationStatus.CANCELLED || currentStatus == ReservationStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION);
        }

        // 如果已支付（RESERVED），发起退款
        if (currentStatus == ReservationStatus.RESERVED) {
            Payment payment = paymentService.findByReservationId(reservationId);
            if (payment != null && payment.getStatus() == PaymentStatus.PAID) {
                paymentService.refund(payment.getId());
            }
        }

        // 释放乐器
        inventoryService.releaseInstruments(List.of(reservation.getInstrument().getId()));

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        log.info("Reservation {} cancelled by user {}", reservationId, userId);
    }

    // ──────────────────────────────────────────────
    //  confirmPickup
    // ──────────────────────────────────────────────

    @Transactional
    public ReservationResponse confirmPickup(String code, User operator) {
        // 先按 pickupCode 查找
        List<Reservation> byPickupCode = reservationRepository.findByStatusAndPickupCode(ReservationStatus.RESERVED, code);
        Reservation reservation;
        Instrument instrument;

        if (!byPickupCode.isEmpty()) {
            reservation = byPickupCode.get(0);
            instrument = reservation.getInstrument();
        } else {
            // 没找到，按 barcode 查 instrument，再找对应的 RESERVED reservation
            instrument = instrumentRepository.findByBarcode(code)
                    .orElseThrow(() -> new BusinessException(ErrorCode.SCAN_CODE_INVALID));

            reservation = reservationRepository
                    .findFirstByInstrumentIdAndStatusOrderByCreatedAtDesc(instrument.getId(), ReservationStatus.RESERVED)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        }

        // 验证未过期
        if (reservation.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.RESERVATION_EXPIRED);
        }

        // 验证状态转换合法
        instrumentStateMachine.validateTransition(instrument.getStatus(), InstrumentStatus.RENTED);

        inventoryService.markAsRented(instrument.getId(), operator);

        reservation.setStatus(ReservationStatus.RENTED);
        reservation.setPickupTime(LocalDateTime.now());
        reservationRepository.save(reservation);

        log.info("Reservation {} picked up via code '{}' by operator {}", reservation.getId(), code, operator.getId());

        return buildResponse(reservation);
    }

    // ──────────────────────────────────────────────
    //  confirmReturn
    // ──────────────────────────────────────────────

    @Transactional
    public ReservationResponse confirmReturn(String barcode, User operator, boolean damaged) {
        Instrument instrument = instrumentRepository.findByBarcode(barcode)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCAN_CODE_INVALID));

        Reservation reservation = reservationRepository
                .findFirstByInstrumentIdAndStatusOrderByCreatedAtDesc(instrument.getId(), ReservationStatus.RENTED)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        // 检查逾期
        if (reservation.getEndTime().isBefore(LocalDateTime.now())
                && !overdueRecordRepository.existsByReservationId(reservation.getId())) {
            OverdueRecord overdueRecord = OverdueRecord.builder()
                    .user(reservation.getUser())
                    .reservation(reservation)
                    .overdueDays((int) java.time.temporal.ChronoUnit.DAYS.between(
                            reservation.getEndTime().toLocalDate(), LocalDateTime.now().toLocalDate()))
                    .build();
            overdueRecordRepository.save(overdueRecord);
            notificationService.sendOverdueAlert(overdueRecord);
            log.warn("Overdue return: reservation {} instrument {} by user {}",
                    reservation.getId(), instrument.getId(), reservation.getUser().getId());
        }

        // 处理损坏或正常归还
        if (damaged) {
            inventoryService.markAsDamaged(instrument.getId(), operator);
        } else {
            inventoryService.markAsReturned(instrument.getId(), operator);
        }

        reservation.setStatus(ReservationStatus.RETURNED);
        reservation.setReturnTime(LocalDateTime.now());
        reservationRepository.save(reservation);

        log.info("Reservation {} returned, instrument {} barcode={} damaged={}",
                reservation.getId(), instrument.getId(), barcode, damaged);

        return buildResponse(reservation);
    }

    // ──────────────────────────────────────────────
    //  getMyReservations
    // ──────────────────────────────────────────────

    public Page<ReservationResponse> getMyReservations(Long userId, Pageable pageable) {
        Page<Reservation> page = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<ReservationResponse> responses = page.getContent().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    // ──────────────────────────────────────────────
    //  helpers
    // ──────────────────────────────────────────────

    private String generatePickupCode() {
        String code;
        do {
            code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        } while (reservationRepository.existsByPickupCode(code));
        return code;
    }

    private String toJson(QuoteResponse quote) {
        return "{\"totalAmount\":" + quote.getTotalAmount().toString()
                + ",\"deposit\":" + quote.getDeposit().toString() + "}";
    }

    private ReservationResponse buildResponse(Reservation r) {
        Instrument instrument = r.getInstrument();
        InstrumentModel model = instrument.getModel();

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal deposit = BigDecimal.ZERO;

        if (r.getPriceDetail() != null && !r.getPriceDetail().isEmpty()) {
            totalAmount = parseJsonNumber(r.getPriceDetail(), "totalAmount");
            deposit = parseJsonNumber(r.getPriceDetail(), "deposit");
        }

        return ReservationResponse.builder()
                .id(r.getId())
                .modelName(model.getName())
                .brand(model.getBrand())
                .instrumentSerials(List.of(instrument.getSerialNo()))
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .status(r.getStatus().name())
                .totalAmount(totalAmount)
                .deposit(deposit)
                .pickupCode(r.getPickupCode())
                .pickupTime(r.getPickupTime())
                .returnTime(r.getReturnTime())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private BigDecimal parseJsonNumber(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) {
            return BigDecimal.ZERO;
        }
        start += search.length();
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '}' || c == ' ') {
                break;
            }
            end++;
        }
        if (start >= end) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(json.substring(start, end).trim());
    }
}