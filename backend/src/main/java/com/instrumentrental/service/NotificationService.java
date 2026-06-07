package com.instrumentrental.service;

import com.instrumentrental.domain.model.Reservation;
import com.instrumentrental.domain.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 通知服务 — 占位实现（Task 13 完善）。
 */
@Service
@Slf4j
public class NotificationService {

    public void sendOverdueAlert(User user, Reservation reservation) {
        log.info("Overdue alert for user {} on reservation {} (placeholder)", user.getId(), reservation.getId());
    }
}