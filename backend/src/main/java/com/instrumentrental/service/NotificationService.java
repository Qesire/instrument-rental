package com.instrumentrental.service;

import com.instrumentrental.domain.model.OverdueRecord;
import com.instrumentrental.domain.model.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送归还提醒消息到 RabbitMQ。
     */
    public void sendReturnReminder(Reservation reservation) {
        String jsonMsg = String.format(
                "{\"userId\":%d,\"reservationId\":%d,\"modelName\":\"%s\",\"endTime\":\"%s\"}",
                reservation.getUser().getId(),
                reservation.getId(),
                reservation.getInstrument().getModel().getName(),
                reservation.getEndTime()
        );
        rabbitTemplate.convertAndSend("notification.exchange", "notification.reminder", jsonMsg);
        log.info("Return reminder sent for reservation {}", reservation.getId());
    }

    /**
     * 发送逾期提醒消息到 RabbitMQ。
     */
    public void sendOverdueAlert(OverdueRecord record) {
        String jsonMsg = String.format(
                "{\"userId\":%d,\"reservationId\":%d,\"overdueDays\":%d,\"resolution\":\"%s\"}",
                record.getUser().getId(),
                record.getReservation().getId(),
                record.getOverdueDays(),
                record.getResolution() != null ? record.getResolution() : ""
        );
        rabbitTemplate.convertAndSend("notification.exchange", "notification.overdue", jsonMsg);
        log.info("Overdue alert sent for overdueRecord {}", record.getId());
    }
}