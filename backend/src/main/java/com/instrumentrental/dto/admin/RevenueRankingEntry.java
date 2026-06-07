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
public class RevenueRankingEntry {

    private String name;
    private BigDecimal revenue;
    private long orderCount;
}