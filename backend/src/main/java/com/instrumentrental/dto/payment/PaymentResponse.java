package com.instrumentrental.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private Long reservationId;
    private BigDecimal amount;
    private String channel;
    private String qrCode;
    private String prepayId;
    private String paymentUrl;
    private String status;
}