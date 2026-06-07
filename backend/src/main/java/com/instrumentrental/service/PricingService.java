package com.instrumentrental.service;

import com.instrumentrental.domain.enums.SeasonType;
import com.instrumentrental.domain.model.InstrumentModel;
import com.instrumentrental.domain.model.PricingSeason;
import com.instrumentrental.domain.model.PricingTier;
import com.instrumentrental.domain.repository.PricingSeasonRepository;
import com.instrumentrental.domain.repository.PricingTierRepository;
import com.instrumentrental.dto.reservation.QuoteResponse;
import com.instrumentrental.dto.reservation.QuoteResponse.DailyBreakdown;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PricingService {

    private final PricingTierRepository pricingTierRepository;
    private final PricingSeasonRepository pricingSeasonRepository;
    private final ConfigService configService;

    private static final String CONFIG_DEPOSIT_DEFAULT_RATIO = "deposit.default_ratio";

    public QuoteResponse calculateQuote(InstrumentModel model, LocalDateTime start, LocalDateTime end, int quantity) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (totalDays <= 0) {
            totalDays = 1;
        }

        // 获取阶梯定价：优先模型专属，否则全局
        List<PricingTier> tiers = pricingTierRepository.findByModelIdOrderByDayFrom(model.getId());
        if (tiers.isEmpty()) {
            tiers = pricingTierRepository.findByModelIdIsNullOrderByDayFrom();
        }

        // 获取所有时段系数，按优先级降序
        List<PricingSeason> seasons = pricingSeasonRepository.findAllByOrderByPriorityDesc();

        BigDecimal totalRental = BigDecimal.ZERO;
        List<DailyBreakdown> dailyBreakdown = new ArrayList<>();

        BigDecimal dailyRate = model.getDailyRate() != null ? model.getDailyRate() : BigDecimal.ZERO;

        LocalDate currentDate = startDate;
        int dayNumber = 1;

        while (currentDate.isBefore(endDate)) {
            // 匹配阶梯定价
            BigDecimal tierRate = matchTierRate(tiers, dayNumber, dailyRate);

            // 匹配时段系数
            BigDecimal coefficient = matchCoefficient(seasons, currentDate);

            // subtotal = tierRate × coefficient，保留2位小数
            BigDecimal subtotal = tierRate.multiply(coefficient).setScale(2, RoundingMode.HALF_UP);

            totalRental = totalRental.add(subtotal);

            dailyBreakdown.add(DailyBreakdown.builder()
                    .date(currentDate.toString())
                    .tierRate(tierRate)
                    .coefficient(coefficient)
                    .subtotal(subtotal)
                    .build());

            currentDate = currentDate.plusDays(1);
            dayNumber++;
        }

        // 计算押金
        BigDecimal deposit = calculateDeposit(model, (int) totalDays, quantity, dailyRate);

        // totalAmount = totalRental × quantity + deposit
        BigDecimal totalAmount = totalRental.multiply(BigDecimal.valueOf(quantity))
                .add(deposit)
                .setScale(2, RoundingMode.HALF_UP);

        return QuoteResponse.builder()
                .modelId(model.getId())
                .modelName(model.getName())
                .startTime(start)
                .endTime(end)
                .totalDays((int) totalDays)
                .dailyRate(dailyRate)
                .totalRental(totalRental.setScale(2, RoundingMode.HALF_UP))
                .deposit(deposit)
                .totalAmount(totalAmount)
                .dailyBreakdown(dailyBreakdown)
                .build();
    }

    private BigDecimal matchTierRate(List<PricingTier> tiers, int dayNumber, BigDecimal fallbackRate) {
        for (PricingTier tier : tiers) {
            if (tier.getDayFrom() <= dayNumber
                    && (tier.getDayTo() == null || dayNumber <= tier.getDayTo())) {
                return tier.getDailyRate();
            }
        }
        return fallbackRate;
    }

    private BigDecimal matchCoefficient(List<PricingSeason> seasons, LocalDate date) {
        // 第一遍：检查 HOLIDAY 类型（日期范围匹配）
        for (PricingSeason season : seasons) {
            if (season.getSeasonType() == SeasonType.HOLIDAY
                    && season.getDateStart() != null && season.getDateEnd() != null
                    && !date.isBefore(season.getDateStart())
                    && !date.isAfter(season.getDateEnd())) {
                return season.getCoefficient() != null ? season.getCoefficient() : BigDecimal.ONE;
            }
        }

        // 第二遍：检查 WEEKEND（周六日）
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            for (PricingSeason season : seasons) {
                if (season.getSeasonType() == SeasonType.WEEKEND) {
                    return season.getCoefficient() != null ? season.getCoefficient() : BigDecimal.ONE;
                }
            }
        }

        // 第三遍：WEEKDAY
        for (PricingSeason season : seasons) {
            if (season.getSeasonType() == SeasonType.WEEKDAY) {
                return season.getCoefficient() != null ? season.getCoefficient() : BigDecimal.ONE;
            }
        }

        return BigDecimal.ONE;
    }

    public BigDecimal calculateDeposit(InstrumentModel model, int totalDays, int quantity, BigDecimal dailyRate) {
        // 如果模型设置了固定押金，直接使用
        if (model.getDeposit() != null) {
            return model.getDeposit().multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // 否则按默认比例计算：ratio × dailyRate × days × quantity
        String ratioStr = configService.getConfigValue(CONFIG_DEPOSIT_DEFAULT_RATIO, "0.5");
        BigDecimal ratio = new BigDecimal(ratioStr);
        return ratio.multiply(dailyRate)
                .multiply(BigDecimal.valueOf(totalDays))
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }
}