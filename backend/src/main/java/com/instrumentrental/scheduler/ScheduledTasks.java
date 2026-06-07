package com.instrumentrental.scheduler;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.Instrument;
import com.instrumentrental.domain.model.OverdueRecord;
import com.instrumentrental.domain.model.Reservation;
import com.instrumentrental.domain.repository.InstrumentRepository;
import com.instrumentrental.domain.repository.OverdueRecordRepository;
import com.instrumentrental.domain.repository.ReservationRepository;
import com.instrumentrental.service.ConfigService;
import com.instrumentrental.service.InventoryService;
import com.instrumentrental.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {

    private final InstrumentRepository instrumentRepository;
    private final ReservationRepository reservationRepository;
    private final OverdueRecordRepository overdueRecordRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final ConfigService configService;

    /**
     * 库存一致性检查：验证各状态仪器数量之和是否等于 total。
     */
    @Scheduled(fixedDelayString = "${inventory.check-interval-ms:1800000}")
    public void inventoryConsistencyCheck() {
        long total = instrumentRepository.count();
        long sum = instrumentRepository.countByStatus(InstrumentStatus.IN_STOCK)
                + instrumentRepository.countByStatus(InstrumentStatus.RESERVED)
                + instrumentRepository.countByStatus(InstrumentStatus.RENTED)
                + instrumentRepository.countByStatus(InstrumentStatus.MAINTENANCE)
                + instrumentRepository.countByStatus(InstrumentStatus.DAMAGED_CHECK)
                + instrumentRepository.countByStatus(InstrumentStatus.SCRAPPED);

        if (total != sum) {
            log.error("INVENTORY INCONSISTENCY: total={}, sum={}", total, sum);
        } else {
            log.debug("Inventory consistency check passed: total={}", total);
        }
    }

    /**
     * 释放过期预约：超时未支付或未取件的预约自动过期。
     */
    @Scheduled(fixedDelayString = "${reservation.timeout-check-ms:900000}")
    @Transactional
    public void releaseExpiredReservations() {
        int hours = Integer.parseInt(configService.getConfigValue("reservation.timeout_hours", "24"));
        LocalDateTime deadline = LocalDateTime.now().minusHours(hours);

        List<Reservation> expired = reservationRepository.findExpiredUnpaidOrUnpicked(deadline);

        for (Reservation r : expired) {
            if (r.getStatus() == ReservationStatus.UNPAID) {
                r.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(r);
                log.info("Expired unpaid reservation {}", r.getId());
            } else if (r.getStatus() == ReservationStatus.RESERVED) {
                r.setStatus(ReservationStatus.EXPIRED);
                Instrument instrument = r.getInstrument();
                instrument.setStatus(InstrumentStatus.IN_STOCK);
                instrumentRepository.save(instrument);
                reservationRepository.save(r);
                log.info("Expired unpicked reservation {}, instrument {} released", r.getId(), instrument.getId());
            }
        }
    }

    /**
     * 发送归还提醒：上午 9 点和下午 6 点提醒即将到期的用户。
     */
    @Scheduled(cron = "0 0 9,18 * * ?")
    public void sendReturnReminders() {
        int hours = Integer.parseInt(configService.getConfigValue("reminder.before_hours", "24"));
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusHours(hours);

        List<Reservation> endingSoon = reservationRepository.findReservationsEndingBetween(start, end);
        for (Reservation r : endingSoon) {
            notificationService.sendReturnReminder(r);
        }
        log.info("Sent return reminders to {} reservations ending between {} and {}", endingSoon.size(), start, end);
    }

    /**
     * 检测逾期：每天凌晨 1 点检查已逾期未归还的租赁。
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void detectOverdue() {
        int graceHours = Integer.parseInt(configService.getConfigValue("overdue.grace_hours", "2"));
        LocalDateTime threshold = LocalDateTime.now().minusHours(graceHours);

        List<Reservation> overdueList = reservationRepository.findOverdueRentals(threshold);

        for (Reservation r : overdueList) {
            if (!overdueRecordRepository.existsByReservationId(r.getId())) {
                long overdueDays = Duration.between(r.getEndTime(), LocalDateTime.now()).toDays();

                OverdueRecord record = OverdueRecord.builder()
                        .user(r.getUser())
                        .reservation(r)
                        .overdueDays((int) overdueDays)
                        .build();

                overdueRecordRepository.save(record);
                notificationService.sendOverdueAlert(record);
                log.info("Detected overdue for reservation {}, {} days", r.getId(), overdueDays);
            }
        }
    }

    /**
     * 释放未支付预约：超过未支付超时时间的预约自动释放库存并过期。
     */
    @Scheduled(fixedDelayString = "${reservation.unpaid-check-ms:600000}")
    @Transactional
    public void releaseUnpaidReservations() {
        int minutes = Integer.parseInt(configService.getConfigValue("reservation.unpaid_timeout_min", "30"));
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(minutes);

        List<Reservation> expired = reservationRepository.findExpiredUnpaidOrUnpicked(deadline);

        for (Reservation r : expired) {
            if (r.getStatus() == ReservationStatus.UNPAID) {
                Instrument instrument = r.getInstrument();
                instrument.setStatus(InstrumentStatus.IN_STOCK);
                instrumentRepository.save(instrument);

                r.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(r);
                log.info("Released unpaid reservation {}, instrument {} back to stock", r.getId(), instrument.getId());
            }
        }
    }
}