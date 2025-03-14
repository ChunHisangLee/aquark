package com.jack.aquark.service.impl;

import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.repository.DailyAggregationRepository;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import com.jack.aquark.service.AggregationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AggregationServiceImpl implements AggregationService {

  private final HourlyAggregationRepository hourlyAggregationRepository;
  private final DailyAggregationRepository dailyAggregationRepository;
  private final TempSensorDataRepository tempSensorDataRepository;

  @Override
  public void saveOrUpdateHourlyAggregation(HourlyAggregation aggregation) {
    // Check if an aggregation record already exists for the given composite key.
    Optional<HourlyAggregation> existingOpt =
        hourlyAggregationRepository.findByStationIdAndObsDateAndObsHourAndCsqAndSensorName(
            aggregation.getStationId(),
            aggregation.getObsDate(),
            aggregation.getObsHour(),
            aggregation.getCsq(),
            aggregation.getSensorName());
    if (existingOpt.isPresent()) {
      HourlyAggregation existing = existingOpt.get();
      // Update the fields. (Here we simply replace with recalculated values.)
      existing.setSumValue(aggregation.getSumValue());
      existing.setAvgValue(aggregation.getAvgValue());
      hourlyAggregationRepository.save(existing);
      log.info(
          "Updated hourly aggregation for station {} on {} hour {} sensor {}: sum={}, avg={}",
          existing.getStationId(),
          existing.getObsDate(),
          existing.getObsHour(),
          existing.getSensorName(),
          existing.getSumValue(),
          existing.getAvgValue());
    } else {
      hourlyAggregationRepository.save(aggregation);
      log.info(
          "Created new hourly aggregation for station {} on {} hour {} sensor {}: sum={}, avg={}",
          aggregation.getStationId(),
          aggregation.getObsDate(),
          aggregation.getObsHour(),
          aggregation.getSensorName(),
          aggregation.getSumValue(),
          aggregation.getAvgValue());
    }
  }

  @Override
  public void aggregateHourlyData() {
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

    // Group by measurement parameter. Each key in fieldAccessors is the sensor parameter
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

    fieldAccessors.forEach(
        (parameter, getter) -> {
          List<BigDecimal> values = tempData.stream().map(getter).filter(Objects::nonNull).toList();
          BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
          BigDecimal avg =
              values.isEmpty()
                  ? BigDecimal.ZERO
                  : sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);

          String stationId = tempData.getFirst().getStationId();
          String csq = tempData.getFirst().getCsq();

          HourlyAggregation aggregation =
              HourlyAggregation.builder()
                  .stationId(stationId)
                  .csq(csq)
                  .obsDate(currentDate)
                  .obsHour(currentHour)
                  .sensorName(parameter)
                  .sumValue(sum)
                  .avgValue(avg)
                  .build();

          saveOrUpdateHourlyAggregation(aggregation);
        });
  }

  @Override
  public void aggregateDailyData() {
    LocalDate today = LocalDate.now();
    log.info("Building daily aggregation for {}", today);

    List<HourlyAggregation> hourlyAggregations = hourlyAggregationRepository.findByObsDate(today);
    if (hourlyAggregations.isEmpty()) {
      log.warn("No hourly aggregation records found for {}", today);
      return;
    }

    // Group by composite key: stationId, csq, sensorName.
    Map<String, List<HourlyAggregation>> groupByKey =
        hourlyAggregations.stream()
            .collect(
                Collectors.groupingBy(
                    ha -> ha.getStationId() + "_" + ha.getCsq() + "_" + ha.getSensorName()));

    groupByKey.forEach(
        (key, list) -> {
          // Calculate daily aggregation values
          BigDecimal dailySum =
              list.stream()
                  .map(HourlyAggregation::getSumValue)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
          // Here we compute daily average as the arithmetic mean of hourly averages.
          BigDecimal dailyAvg =
              list.stream()
                  .map(HourlyAggregation::getAvgValue)
                  .reduce(BigDecimal.ZERO, BigDecimal::add)
                  .divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP);

          // Split the key to retrieve stationId, csq, sensorName.
          String[] parts = key.split("_");
          String stationId = parts[0];
          String csq = parts[1];
          String sensorName = parts[2];

          // Check if a daily aggregation record already exists for this composite key.
          Optional<DailyAggregation> existingOpt =
              dailyAggregationRepository.findByStationIdAndObsDateAndCsqAndSensorName(
                  stationId, today, csq, sensorName);

          if (existingOpt.isPresent()) {
            DailyAggregation existing = existingOpt.get();
            existing.setSumValue(dailySum);
            existing.setAvgValue(dailyAvg);
            dailyAggregationRepository.save(existing);
            log.info(
                "Updated daily aggregation record for station {} parameter {} on {}",
                stationId,
                sensorName,
                today);
          } else {
            DailyAggregation dailyAgg =
                DailyAggregation.builder()
                    .stationId(stationId)
                    .csq(csq)
                    .obsDate(today)
                    .sensorName(sensorName)
                    .sumValue(dailySum)
                    .avgValue(dailyAvg)
                    .build();
            dailyAggregationRepository.save(dailyAgg);
            log.info(
                "Created daily aggregation record for station {} parameter {} on {}",
                stationId,
                sensorName,
                today);
          }
        });
  }

  @Override
  public void processTempDataForAggregations() {
    aggregateHourlyData();
    aggregateDailyData();
    tempSensorDataRepository.deleteAll();
    log.info("Temporary sensor data cleared after processing aggregations.");
  }
}
