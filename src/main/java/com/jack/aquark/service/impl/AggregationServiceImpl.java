package com.jack.aquark.service.impl;

import com.jack.aquark.dto.DailySensorMapping;
import com.jack.aquark.dto.HourlySensorMapping;
import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.repository.DailyAggregationRepository;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import com.jack.aquark.service.AggregationService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
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

  // MAPPINGS FOR HOURLY
  private static final List<HourlySensorMapping> HOURLY_FIELDS =
      List.of(
          new HourlySensorMapping(
              TempSensorData::getV1,
              HourlyAggregation::setV1SumValue,
              HourlyAggregation::setV1AvgValue),
          new HourlySensorMapping(
              TempSensorData::getV2,
              HourlyAggregation::setV2SumValue,
              HourlyAggregation::setV2AvgValue),
          new HourlySensorMapping(
              TempSensorData::getV3,
              HourlyAggregation::setV3SumValue,
              HourlyAggregation::setV3AvgValue),
          new HourlySensorMapping(
              TempSensorData::getV4,
              HourlyAggregation::setV4SumValue,
              HourlyAggregation::setV4AvgValue),
          new HourlySensorMapping(
              TempSensorData::getV5,
              HourlyAggregation::setV5SumValue,
              HourlyAggregation::setV5AvgValue),
          new HourlySensorMapping(
              TempSensorData::getV6,
              HourlyAggregation::setV6SumValue,
              HourlyAggregation::setV6AvgValue),
          new HourlySensorMapping(
              TempSensorData::getV7,
              HourlyAggregation::setV7SumValue,
              HourlyAggregation::setV7AvgValue),
          new HourlySensorMapping(
              TempSensorData::getRh,
              HourlyAggregation::setRhSumValue,
              HourlyAggregation::setRhAvgValue),
          new HourlySensorMapping(
              TempSensorData::getTx,
              HourlyAggregation::setTxSumValue,
              HourlyAggregation::setTxAvgValue),
          new HourlySensorMapping(
              TempSensorData::getEcho,
              HourlyAggregation::setEchoSumValue,
              HourlyAggregation::setEchoAvgValue),
          new HourlySensorMapping(
              TempSensorData::getRainD,
              HourlyAggregation::setRainDSumValue,
              HourlyAggregation::setRainDAvgValue),
          new HourlySensorMapping(
              TempSensorData::getSpeed,
              HourlyAggregation::setSpeedSumValue,
              HourlyAggregation::setSpeedAvgValue));

  // MAPPINGS FOR DAILY
  private static final List<DailySensorMapping> DAILY_FIELDS =
      List.of(
          new DailySensorMapping(
              HourlyAggregation::getV1SumValue,
              HourlyAggregation::getV1AvgValue,
              DailyAggregation::setV1SumValue,
              DailyAggregation::setV1AvgValue),
          new DailySensorMapping(
              HourlyAggregation::getV2SumValue,
              HourlyAggregation::getV2AvgValue,
              DailyAggregation::setV2SumValue,
              DailyAggregation::setV2AvgValue),
          new DailySensorMapping(
              HourlyAggregation::getV3SumValue,
              HourlyAggregation::getV3AvgValue,
              DailyAggregation::setV3SumValue,
              DailyAggregation::setV3AvgValue),
          new DailySensorMapping(
              HourlyAggregation::getV4SumValue,
              HourlyAggregation::getV4AvgValue,
              DailyAggregation::setV4SumValue,
              DailyAggregation::setV4AvgValue),
          new DailySensorMapping(
              HourlyAggregation::getV5SumValue,
              HourlyAggregation::getV5AvgValue,
              DailyAggregation::setV5SumValue,
              DailyAggregation::setV5AvgValue),
          new DailySensorMapping(
              HourlyAggregation::getV6SumValue,
              HourlyAggregation::getV6AvgValue,
              DailyAggregation::setV6SumValue,
              DailyAggregation::setV6AvgValue),
          new DailySensorMapping(
              HourlyAggregation::getV7SumValue,
              HourlyAggregation::getV7AvgValue,
              DailyAggregation::setV7SumValue,
              DailyAggregation::setV7AvgValue),
          new DailySensorMapping(
              HourlyAggregation::getRhSumValue,
              HourlyAggregation::getRhAvgValue,
              DailyAggregation::setRhSumValue,
              DailyAggregation::setRhAvgValue),
          new DailySensorMapping(
              HourlyAggregation::getTxSumValue,
              HourlyAggregation::getTxAvgValue,
              DailyAggregation::setTxSumValue,
              DailyAggregation::setTxAvgValue),
          new DailySensorMapping(
              HourlyAggregation::getEchoSumValue,
              HourlyAggregation::getEchoAvgValue,
              DailyAggregation::setEchoSumValue,
              DailyAggregation::setEchoAvgValue),
          new DailySensorMapping(
              HourlyAggregation::getRainDSumValue,
              HourlyAggregation::getRainDAvgValue,
              DailyAggregation::setRainDSumValue,
              DailyAggregation::setRainDAvgValue),
          new DailySensorMapping(
              HourlyAggregation::getSpeedSumValue,
              HourlyAggregation::getSpeedAvgValue,
              DailyAggregation::setSpeedSumValue,
              DailyAggregation::setSpeedAvgValue));

  @Override
  @Transactional
  public void aggregateHourlyData() {
    List<TempSensorData> tempData = tempSensorDataRepository.findAll();
    if (tempData.isEmpty()) {
      log.info("No temporary sensor data available for hourly aggregation.");
      return;
    }

    // Group by (stationId|date|hour|csq)
    Map<String, List<TempSensorData>> grouped =
        tempData.stream()
            .collect(
                Collectors.groupingBy(
                    tsd -> {
                      String station = tsd.getStationId();
                      LocalDate date = tsd.getObsTime().toLocalDate();
                      int hour = tsd.getObsTime().getHour();
                      String csq = tsd.getCsq();
                      return station + "|" + date + "|" + hour + "|" + csq;
                    }));

    for (var entry : grouped.entrySet()) {
      String key = entry.getKey();
      List<TempSensorData> groupRecords = entry.getValue();

      // Parse the key
      String[] parts = key.split("\\|");
      String stationId = parts[0];
      LocalDate obsDate = LocalDate.parse(parts[1]);
      int obsHour = Integer.parseInt(parts[2]);
      String csq = parts[3];

      // Build an HourlyAggregation object
      HourlyAggregation agg = new HourlyAggregation();
      agg.setStationId(stationId);
      agg.setObsDate(obsDate);
      agg.setObsHour(obsHour);
      agg.setCsq(csq);

      // Loop over each sensor mapping
      for (HourlySensorMapping map : HOURLY_FIELDS) {
        // sum
        BigDecimal sum =
            groupRecords.stream()
                .map(map.rawGetter())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // average
        BigDecimal avg = BigDecimal.ZERO;
        if (sum.compareTo(BigDecimal.ZERO) != 0) {
          long count = groupRecords.stream().map(map.rawGetter()).filter(Objects::nonNull).count();
          avg = sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        }

        // set sum & avg on aggregator
        map.sumSetter().accept(agg, sum);
        map.avgSetter().accept(agg, avg);
      }

      // Upsert into DB
      saveOrUpdateHourlyAggregation(agg);
    }

    log.info("Hourly aggregation complete. {} grouped sets processed.", grouped.size());
  }

  /**
   * Insert or update one HourlyAggregation row, identified by (stationId, obsDate, obsHour, csq).
   */
  public void saveOrUpdateHourlyAggregation(HourlyAggregation aggregation) {
    Optional<HourlyAggregation> optExisting =
        hourlyAggregationRepository.findByStationIdAndObsDateAndObsHourAndCsq(
            aggregation.getStationId(),
            aggregation.getObsDate(),
            aggregation.getObsHour(),
            aggregation.getCsq());

    if (optExisting.isPresent()) {
      HourlyAggregation existing = optExisting.get();
      // copy over all sensor fields
      existing.setV1SumValue(aggregation.getV1SumValue());
      existing.setV1AvgValue(aggregation.getV1AvgValue());
      existing.setV2SumValue(aggregation.getV2SumValue());
      existing.setV2AvgValue(aggregation.getV2AvgValue());
      existing.setV3SumValue(aggregation.getV3SumValue());
      existing.setV3AvgValue(aggregation.getV3AvgValue());
      existing.setV4SumValue(aggregation.getV4SumValue());
      existing.setV4AvgValue(aggregation.getV4AvgValue());
      existing.setV5SumValue(aggregation.getV5SumValue());
      existing.setV5AvgValue(aggregation.getV5AvgValue());
      existing.setV6SumValue(aggregation.getV6SumValue());
      existing.setV6AvgValue(aggregation.getV6AvgValue());
      existing.setV7SumValue(aggregation.getV7SumValue());
      existing.setV7AvgValue(aggregation.getV7AvgValue());
      existing.setRhSumValue(aggregation.getRhSumValue());
      existing.setRhAvgValue(aggregation.getRhAvgValue());
      existing.setTxSumValue(aggregation.getTxSumValue());
      existing.setTxAvgValue(aggregation.getTxAvgValue());
      existing.setEchoSumValue(aggregation.getEchoSumValue());
      existing.setEchoAvgValue(aggregation.getEchoAvgValue());
      existing.setRainDSumValue(aggregation.getRainDSumValue());
      existing.setRainDAvgValue(aggregation.getRainDAvgValue());
      existing.setSpeedSumValue(aggregation.getSpeedSumValue());
      existing.setSpeedAvgValue(aggregation.getSpeedAvgValue());

      hourlyAggregationRepository.save(existing);
      log.debug(
          "Updated existing HourlyAggregation row for station={}, date={}, hour={}, csq={}",
          existing.getStationId(),
          existing.getObsDate(),
          existing.getObsHour(),
          existing.getCsq());
    } else {
      // new row
      hourlyAggregationRepository.save(aggregation);
      log.debug(
          "Inserted new HourlyAggregation row for station={}, date={}, hour={}, csq={}",
          aggregation.getStationId(),
          aggregation.getObsDate(),
          aggregation.getObsHour(),
          aggregation.getCsq());
    }
  }

  // -------------------------------------------------------------------------

  @Override
  @Transactional
  public void aggregateDailyData() {
    List<HourlyAggregation> allHourly = hourlyAggregationRepository.findAll();
    if (allHourly.isEmpty()) {
      log.warn("No hourly aggregation records found, skipping daily aggregation.");
      return;
    }

    // Group by (stationId|obsDate|csq)
    Map<String, List<HourlyAggregation>> grouped =
        allHourly.stream()
            .collect(
                Collectors.groupingBy(
                    ha -> {
                      String station = ha.getStationId();
                      LocalDate date = ha.getObsDate();
                      String csq = ha.getCsq();
                      return station + "|" + date + "|" + csq;
                    }));

    for (var entry : grouped.entrySet()) {
      String key = entry.getKey();
      List<HourlyAggregation> group = entry.getValue();

      String[] parts = key.split("\\|");
      String stationId = parts[0];
      LocalDate obsDate = LocalDate.parse(parts[1]);
      String csq = parts[2];

      // Either update existing or create new daily row
      DailyAggregation dailyAgg =
          dailyAggregationRepository
              .findByStationIdAndObsDateAndCsq(stationId, obsDate, csq)
              .orElseGet(
                  () -> {
                    DailyAggregation d = new DailyAggregation();
                    d.setStationId(stationId);
                    d.setObsDate(obsDate);
                    d.setCsq(csq);
                    return d;
                  });

      // Loop over the daily fields to sum & average
      for (DailySensorMapping map : DAILY_FIELDS) {
        BigDecimal sumVal =
            group.stream()
                .map(map.hourSumGetter())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // unweighted average of the hourly "avg" fields
        List<BigDecimal> avgVals =
            group.stream().map(map.hourAvgGetter()).filter(Objects::nonNull).toList();

        BigDecimal avgVal = BigDecimal.ZERO;
        if (!avgVals.isEmpty()) {
          BigDecimal sumOfAvg = avgVals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
          avgVal = sumOfAvg.divide(BigDecimal.valueOf(avgVals.size()), 2, RoundingMode.HALF_UP);
        }

        // set them
        map.daySumSetter().accept(dailyAgg, sumVal);
        map.dayAvgSetter().accept(dailyAgg, avgVal);
      }

      dailyAggregationRepository.save(dailyAgg);
      log.debug(
          "Upserted DailyAggregation for station={}, date={}, csq={}", stationId, obsDate, csq);
    }

    log.info("Daily aggregation complete. Processed {} day-group(s).", grouped.size());
  }

  @Override
  @Transactional
  public void processTempDataForAggregations() {
    // 1) Hourly
    aggregateHourlyData();
    // 2) Daily
    aggregateDailyData();
    // 3) Optionally clear the raw data
    tempSensorDataRepository.deleteAll();
    log.info("Temporary sensor data cleared after daily aggregation.");
  }
}
