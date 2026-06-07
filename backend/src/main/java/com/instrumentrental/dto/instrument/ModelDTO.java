package com.instrumentrental.dto.instrument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDTO {

    private Long id;
    private String name;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private BigDecimal dailyRate;
    private BigDecimal deposit;
    private List<String> images;
    private String specs;
    private String status;
    private long availableCount;
    private long totalCount;
}