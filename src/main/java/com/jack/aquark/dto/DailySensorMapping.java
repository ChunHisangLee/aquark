package com.jack.aquark.dto;

import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Maps a single sensor from HourlyAggregation -> DailyAggregation (sum & avg).
 *
 * <p>For example: - hourSumGetter: HourlyAggregation::getV1SumValue - hourAvgGetter:
 * HourlyAggregation::getV1AvgValue - daySumSetter: DailyAggregation::setV1SumValue - dayAvgSetter:
 * DailyAggregation::setV1AvgValue
 */
public record DailySensorMapping(
    Function<HourlyAggregation, BigDecimal> hourSumGetter,
    Function<HourlyAggregation, BigDecimal> hourAvgGetter,
    BiConsumer<DailyAggregation, BigDecimal> daySumSetter,
    BiConsumer<DailyAggregation, BigDecimal> dayAvgSetter) {}
