package com.instrumentrental.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付服务 — 占位实现（Task 12 完善）。
 */
@Service
@Slf4j
public class PaymentService {

    public void refund(Long reservationId) {
        log.info("Refund requested for reservation {} (placeholder)", reservationId);
    }
}