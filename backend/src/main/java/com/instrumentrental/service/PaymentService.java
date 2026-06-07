package com.instrumentrental.service;

import com.instrumentrental.domain.enums.PaymentChannel;
import com.instrumentrental.domain.enums.PaymentStatus;
import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.Payment;
import com.instrumentrental.domain.model.Reservation;
import com.instrumentrental.domain.repository.PaymentRepository;
import com.instrumentrental.domain.repository.ReservationRepository;
import com.instrumentrental.dto.payment.PaymentResponse;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 创建支付。
     */
    @Transactional
    public PaymentResponse createPayment(Long reservationId, PaymentChannel channel) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        // 检查重复支付
        paymentRepository.findByReservationId(reservationId).ifPresent(existing -> {
            if (existing.getStatus() == PaymentStatus.PAID) {
                throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT);
            }
        });

        // 复用已有 PENDING 记录或新建
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseGet(() -> Payment.builder()
                        .reservation(reservation)
                        .channel(channel)
                        .build());

        // 解析 priceDetail 获取金额
        BigDecimal amount = parseTotalAmount(reservation.getPriceDetail());
        payment.setAmount(amount);
        payment.setChannel(channel);
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        log.info("Payment created: id={}, reservationId={}, amount={}, channel={}",
                payment.getId(), reservationId, amount, channel);

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .reservationId(reservationId)
                .amount(amount)
                .channel(channel.name())
                .status(payment.getStatus().name())
                .build();
    }

    /**
     * 处理支付回调。
     */
    @Transactional
    public void handleCallback(String channel, String transactionId, boolean success) {
        if (transactionId == null) {
            log.warn("handleCallback: transactionId is null, skipping");
            return;
        }

        Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
        if (paymentOpt.isEmpty()) {
            log.warn("handleCallback: no payment found for transactionId={}", transactionId);
            return;
        }

        Payment payment = paymentOpt.get();
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("handleCallback: payment {} already PAID, idempotent skip", payment.getId());
            return;
        }

        payment.setStatus(success ? PaymentStatus.PAID : PaymentStatus.FAILED);
        paymentRepository.save(payment);

        log.info("handleCallback: payment {} status updated to {}", payment.getId(), payment.getStatus());

        if (success) {
            Reservation reservation = payment.getReservation();
            if (reservation.getStatus() == ReservationStatus.UNPAID) {
                reservation.setStatus(ReservationStatus.RESERVED);
                reservationRepository.save(reservation);
                log.info("handleCallback: reservation {} status updated to RESERVED", reservation.getId());
            }
        }
    }

    /**
     * 退款。
     */
    @Transactional
    public void refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.REFUND_FAILED);
        }

        payment.setStatus(PaymentStatus.REFUNDING);
        paymentRepository.save(payment);
        log.info("Refund initiated: paymentId={}, amount={}", paymentId, payment.getAmount());

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(payment.getAmount());
        paymentRepository.save(payment);
        log.info("Refund completed: paymentId={}, refundAmount={}", paymentId, payment.getRefundAmount());
    }

    /**
     * 按预约ID查找支付记录。
     */
    @Transactional(readOnly = true)
    public Payment findByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId).orElse(null);
    }

    /**
     * 从 priceDetail JSON 字符串中简单解析 totalAmount。
     */
    private BigDecimal parseTotalAmount(String priceDetail) {
        if (priceDetail == null || priceDetail.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            int idx = priceDetail.indexOf("\"totalAmount\"");
            if (idx >= 0) {
                int colonIdx = priceDetail.indexOf(":", idx);
                int start = colonIdx + 1;
                while (start < priceDetail.length() && Character.isWhitespace(priceDetail.charAt(start))) {
                    start++;
                }
                int end = start;
                while (end < priceDetail.length()
                        && (Character.isDigit(priceDetail.charAt(end)) || priceDetail.charAt(end) == '.')) {
                    end++;
                }
                return new BigDecimal(priceDetail.substring(start, end));
            }
        } catch (Exception e) {
            log.warn("Failed to parse totalAmount from priceDetail: {}", priceDetail, e);
        }
        return BigDecimal.ZERO;
    }
}