package com.instrumentrental.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private long inStock;
    private long reserved;
    private long rented;
    private long overdue;
    private long maintenance;
    private long total;
    private Map<String, Long> byWarehouse;
}