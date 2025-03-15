package com.jack.aquark.dto;

import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.TempSensorData;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record HourlySensorMapping(
    Function<TempSensorData, BigDecimal> rawGetter,
    BiConsumer<HourlyAggregation, BigDecimal> sumSetter,
    BiConsumer<HourlyAggregation, BigDecimal> avgSetter) {}
