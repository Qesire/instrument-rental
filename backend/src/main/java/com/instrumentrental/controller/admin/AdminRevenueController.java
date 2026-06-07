package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.repository.PaymentRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.admin.RevenueSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminRevenueController {

    private final PaymentRepository paymentRepository;

    @GetMapping("/summary")
    public ApiResponse<RevenueSummaryDTO> getSummary(@RequestParam(defaultValue = "month") String period) {
        RevenueSummaryDTO summary = RevenueSummaryDTO.builder()
                .currentPeriodRevenue(BigDecimal.ZERO)
                .previousPeriodRevenue(BigDecimal.ZERO)
                .changePercent(BigDecimal.ZERO)
                .orderCount(0L)
                .period(period)
                .build();

        return ApiResponse.success(summary);
    }
}