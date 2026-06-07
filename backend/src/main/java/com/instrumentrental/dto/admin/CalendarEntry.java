package com.instrumentrental.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEntry {

    private Long modelId;
    private String modelName;
    private List<ReservationBlock> reservations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationBlock {
        private Long reservationId;
        private String userName;
        private String instrumentSerial;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
    }
}