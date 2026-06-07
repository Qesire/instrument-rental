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
public class ReservationResponse {

    private Long id;
    private String modelName;
    private String brand;
    private List<String> instrumentSerials;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal deposit;
    private String pickupCode;
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;
    private LocalDateTime createdAt;
}