package com.instrumentrental.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {

    private Long modelId;
    private String modelName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalDays;
    private int availableCount;
    private BigDecimal dailyRate;
    private BigDecimal totalRental;
    private BigDecimal deposit;
    private BigDecimal totalAmount;
    private List<DailyBreakdown> dailyBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyBreakdown {
        private String date;
        private BigDecimal tierRate;
        private BigDecimal coefficient;
        private BigDecimal subtotal;
    }
}