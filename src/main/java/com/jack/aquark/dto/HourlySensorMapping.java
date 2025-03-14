package com.jack.aquark.dto;

import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.TempSensorData;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Maps a single sensor from TempSensorData -> HourlyAggregation (sum & avg).
 *
 * <p>For example: - rawGetter: TempSensorData::getV1 - sumSetter: HourlyAggregation::setV1SumValue
 * - avgSetter: HourlyAggregation::setV1AvgValue
 */
public record HourlySensorMapping(
    Function<TempSensorData, BigDecimal> rawGetter,
    BiConsumer<HourlyAggregation, BigDecimal> sumSetter,
    BiConsumer<HourlyAggregation, BigDecimal> avgSetter) {}
