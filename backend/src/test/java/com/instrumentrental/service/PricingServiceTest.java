package com.instrumentrental.service;

import com.instrumentrental.domain.enums.SeasonType;
import com.instrumentrental.domain.model.InstrumentModel;
import com.instrumentrental.domain.model.PricingSeason;
import com.instrumentrental.domain.model.PricingTier;
import com.instrumentrental.domain.repository.PricingSeasonRepository;
import com.instrumentrental.domain.repository.PricingTierRepository;
import com.instrumentrental.dto.reservation.QuoteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingTierRepository pricingTierRepository;

    @Mock
    private PricingSeasonRepository pricingSeasonRepository;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private PricingService pricingService;

    private InstrumentModel model;

    @BeforeEach
    void setUp() {
        model = InstrumentModel.builder()
                .id(1L)
                .name("Test Guitar")
                .dailyRate(new BigDecimal("100"))
                .deposit(new BigDecimal("500"))
                .build();
    }

    @Test
    void calculateQuote_3DayRental_shouldCalculateCorrectTotal() {
        // given: 阶梯定价：1-3天 100/天
        PricingTier tier = PricingTier.builder()
                .id(1L)
                .dayFrom(1)
                .dayTo(3)
                .dailyRate(new BigDecimal("100"))
                .build();

        when(pricingTierRepository.findByModelIdOrderByDayFrom(1L))
                .thenReturn(List.of(tier));

        // 默认时段系数（WEEKDAY = 1.0）
        PricingSeason weekdaySeason = PricingSeason.builder()
                .id(1L)
                .seasonType(SeasonType.WEEKDAY)
                .coefficient(BigDecimal.ONE)
                .priority(1)
                .build();

        when(pricingSeasonRepository.findAllByOrderByPriorityDesc())
                .thenReturn(List.of(weekdaySeason));

        // when: 2026-06-08 09:00（周一）到 2026-06-11 09:00（周四）= 3天
        LocalDateTime start = LocalDateTime.of(2026, 6, 8, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 11, 9, 0);

        QuoteResponse response = pricingService.calculateQuote(model, start, end, 1);

        // then
        assertThat(response.getModelId()).isEqualTo(1L);
        assertThat(response.getModelName()).isEqualTo("Test Guitar");
        assertThat(response.getTotalDays()).isEqualTo(3);

        // 总租金：3天 × 100/天 × 1.0 = 300.00
        assertThat(response.getTotalRental()).isEqualTo(new BigDecimal("300.00"));

        // 押金：model.deposit = 500 × quantity(1) = 500
        assertThat(response.getDeposit()).isEqualTo(new BigDecimal("500.00"));

        // 总金额：300 × 1 + 500 = 800.00
        assertThat(response.getTotalAmount()).isEqualTo(new BigDecimal("800.00"));

        // 每日明细
        assertThat(response.getDailyBreakdown()).hasSize(3);
        assertThat(response.getDailyBreakdown().get(0).getDate()).isEqualTo("2026-06-08");
        assertThat(response.getDailyBreakdown().get(0).getTierRate()).isEqualTo(new BigDecimal("100"));
        assertThat(response.getDailyBreakdown().get(0).getCoefficient()).isEqualTo(BigDecimal.ONE);
        assertThat(response.getDailyBreakdown().get(0).getSubtotal()).isEqualTo(new BigDecimal("100.00"));
    }
}