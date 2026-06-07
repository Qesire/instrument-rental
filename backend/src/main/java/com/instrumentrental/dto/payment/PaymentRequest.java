package com.instrumentrental.dto.payment;

import com.instrumentrental.domain.enums.PaymentChannel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "预约ID不能为空")
    private Long reservationId;

    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel channel;
}