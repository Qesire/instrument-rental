package com.instrumentrental.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSummaryDTO {

    private BigDecimal currentPeriodRevenue;
    private BigDecimal previousPeriodRevenue;
    private BigDecimal changePercent;
    private long orderCount;
    private String period;
}