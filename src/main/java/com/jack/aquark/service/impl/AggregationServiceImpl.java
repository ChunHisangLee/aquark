package com.jack.aquark.service.impl;

import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.repository.DailyAggregationRepository;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import com.jack.aquark.service.AggregationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@AllArgsConstructor
@Slf4j
public class AggregationServiceImpl implements AggregationService {

  private final HourlyAggregationRepository hourlyAggregationRepository;
  private final DailyAggregationRepository dailyAggregationRepository;
  private final TempSensorDataRepository tempSensorDataRepository;

  @Override
  public void saveHourlyAggregation(HourlyAggregation aggregation) {
    hourlyAggregationRepository.save(aggregation);
    log.info(
            "Saved hourly aggregation for {} hour {} sensor {}: sum={}, avg={}",
            aggregation.getObsDate(),
            aggregation.getObsHour(),
            aggregation.getSensorName(),
            aggregation.getSumValue(),
            aggregation.getAvgValue());
  }

  @Override
  public void aggregateHourlyData() {
    // Existing aggregation from raw SensorData (if needed)
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneHourAgo = now.minusHours(1);
    LocalDate currentDate = now.toLocalDate();
    int currentHour = now.getHour();

    List<TempSensorData> tempData =
            tempSensorDataRepository.findAllByObsTimeBetween(oneHourAgo, now);
    if (tempData.isEmpty()) {
      log.info("No temporary sensor data available for the last hour.");
      return;
    }

    // Use BigDecimal field accessors
    Map<String, Function<TempSensorData, BigDecimal>> fieldAccessors = new LinkedHashMap<>();
    fieldAccessors.put("v1", TempSensorData::getV1);
    fieldAccessors.put("v2", TempSensorData::getV2);
    fieldAccessors.put("v3", TempSensorData::getV3);
    fieldAccessors.put("v4", TempSensorData::getV4);
    fieldAccessors.put("v5", TempSensorData::getV5);
    fieldAccessors.put("v6", TempSensorData::getV6);
    fieldAccessors.put("v7", TempSensorData::getV7);
    fieldAccessors.put("rh", TempSensorData::getRh);
    fieldAccessors.put("tx", TempSensorData::getTx);
    fieldAccessors.put("echo", TempSensorData::getEcho);
    fieldAccessors.put("rainD", TempSensorData::getRainD);
    fieldAccessors.put("speed", TempSensorData::getSpeed);

    fieldAccessors.forEach((sensorName, getter) -> {
      List<BigDecimal> values = tempData.stream()
              .map(getter)
              .filter(Objects::nonNull)
              .toList();

      BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
      BigDecimal avg = values.isEmpty()
              ? BigDecimal.ZERO
              : sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);

      HourlyAggregation aggregation =
              HourlyAggregation.builder()
                      .obsDate(currentDate)
                      .obsHour(currentHour)
                      .sensorName(sensorName)
                      .sumValue(sum)
                      .avgValue(avg)
                      .build();

      saveHourlyAggregation(aggregation);
    });
  }

  @Override
  public void aggregateDailyData() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    log.info("Building daily aggregation for {}", yesterday);

    // Retrieve all hourly aggregations for yesterday.
    List<HourlyAggregation> hourlyAggregations =
            hourlyAggregationRepository.findByObsDate(yesterday);
    if (hourlyAggregations.isEmpty()) {
      log.warn("No hourly aggregation records found for {}", yesterday);
      return;
    }

    Map<String, List<HourlyAggregation>> sensorToHourly =
            hourlyAggregations.stream()
                    .collect(Collectors.groupingBy(HourlyAggregation::getSensorName));

    sensorToHourly.forEach((sensorName, hourlyList) -> {
      for (HourlyAggregation hourly : hourlyList) {
        DailyAggregation dailyAgg =
                DailyAggregation.builder()
                        .obsDate(yesterday)
                        .obsHour(hourly.getObsHour())
                        .sensorName(sensorName)
                        .sumValue(hourly.getSumValue())
                        .avgValue(hourly.getAvgValue())
                        .build();
        dailyAggregationRepository.save(dailyAgg);
        log.info(
                "Daily aggregation record saved for {} hour {} sensor {}",
                yesterday,
                hourly.getObsHour(),
                sensorName);
      }
    });
  }

  @Override
  public void processTempDataForAggregations() {
    // First, aggregate hourly data from the temporary table.
    aggregateHourlyData();

    // Then, aggregate daily data from the hourly aggregation table.
    aggregateDailyData();

    // Finally, clear the temporary table for the next fetch cycle.
    tempSensorDataRepository.deleteAll();
    log.info("Temporary sensor data cleared after processing aggregations.");
  }
}
