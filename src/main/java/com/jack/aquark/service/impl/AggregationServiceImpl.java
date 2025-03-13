package com.jack.aquark.service.impl;

import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.repository.DailyAggregationRepository;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.SensorDataService;
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

  private final SensorDataService sensorDataService;
  private final HourlyAggregationRepository hourlyAggregationRepository;
  private final DailyAggregationRepository dailyAggregationRepository;

  @Override
  public void saveHourlyAggregation(HourlyAggregation aggregation) {
    hourlyAggregationRepository.save(aggregation);
    log.info(
        "Saved hourly aggregation: {} {} {}: sum={}, avg={}",
        aggregation.getObsDate(),
        aggregation.getObsHour(),
        aggregation.getSensorName(),
        aggregation.getSumValue(),
        aggregation.getAvgValue());
  }

  /** Compute hourly aggregations for the last hour. */
  public void aggregateHourlyData() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneHourAgo = now.minusHours(1);
    LocalDate currentDate = now.toLocalDate();
    int currentHour = now.getHour();

    // Get sensor data for the last hour
    List<SensorData> hourlyData = sensorDataService.getSensorDataByTimeRange(oneHourAgo, now);

    // For each sensor, aggregate values. Here we use a map of sensorName -> getter.
    Map<String, Function<SensorData, Double>> fieldAccessors = new LinkedHashMap<>();
    fieldAccessors.put("v1", SensorData::getV1);
    fieldAccessors.put("v2", SensorData::getV2);
    fieldAccessors.put("v3", SensorData::getV3);
    fieldAccessors.put("v4", SensorData::getV4);
    fieldAccessors.put("v5", SensorData::getV5);
    fieldAccessors.put("v6", SensorData::getV6);
    fieldAccessors.put("v7", SensorData::getV7);
    fieldAccessors.put("rh", SensorData::getRh);
    fieldAccessors.put("tx", SensorData::getTx);
    fieldAccessors.put("echo", SensorData::getEcho);
    fieldAccessors.put("rainD", SensorData::getRainD);
    fieldAccessors.put("speed", SensorData::getSpeed);

    fieldAccessors.forEach(
        (sensorName, getter) -> {
          List<Double> values =
              hourlyData.stream().map(getter).filter(Objects::nonNull).collect(Collectors.toList());

          double sum = values.stream().mapToDouble(Double::doubleValue).sum();
          double avg = values.isEmpty() ? 0.0 : sum / values.size();

          HourlyAggregation aggregation =
              HourlyAggregation.builder()
                  .obsDate(currentDate)
                  .obsHour(currentHour)
                  .sensorName(sensorName)
                  .sumValue(sum)
                  .avgValue(avg)
                  .build();

          hourlyAggregationRepository.save(aggregation);
          log.info(
              "Hourly aggregation saved for {} at hour {} sensor {}: sum={}, avg={}",
              currentDate,
              currentHour,
              sensorName,
              sum,
              avg);
        });
  }

  /**
   * Build daily aggregations by simply taking the 24 hourly aggregation records for a given day.
   * (This example assumes that hourly aggregation runs have stored 24 records per sensor per day.)
   */
  public void aggregateDailyData() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    log.info("Building daily aggregation for {}", yesterday);

    // Retrieve all hourly aggregations for yesterday
    List<HourlyAggregation> hourlyAggregations =
        hourlyAggregationRepository.findByObsDate(yesterday);
    if (hourlyAggregations.isEmpty()) {
      log.warn("No hourly aggregation records found for {}", yesterday);
      return;
    }

    // Group by sensor name
    Map<String, List<HourlyAggregation>> sensorToHourly =
        hourlyAggregations.stream()
            .collect(Collectors.groupingBy(HourlyAggregation::getSensorName));

    // For each sensor, insert 24 daily aggregation records (or update if they already exist)
    sensorToHourly.forEach(
        (sensorName, hourlyList) -> {
          // In this approach, each hourly record is simply copied to the daily_aggregation table.
          // Alternatively, you could compute overall daily sums/averages.
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
}
