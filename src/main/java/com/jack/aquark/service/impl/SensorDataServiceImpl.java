package com.jack.aquark.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.aquark.dto.RawDataItemDto;
import com.jack.aquark.dto.RawDataWrapperDto;
import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.exception.DataFetchException;
import com.jack.aquark.exception.DataParseException;
import com.jack.aquark.repository.DailyAggregationRepository;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.SensorDataRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.SensorDataService;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class SensorDataServiceImpl implements SensorDataService {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final SensorDataRepository sensorDataRepository;
  private final TempSensorDataRepository tempSensorDataRepository;
  private final HourlyAggregationRepository hourlyAggregationRepository;
  private final DailyAggregationRepository dailyAggregationRepository;
  private final AggregationService aggregationService;
  private final ObjectMapper objectMapper;

  @Override
  @CacheEvict(
      value = {"sensorData", "hourlyAggregation"},
      allEntries = true)
  public void fetchAndSaveSensorData(String apiUrl) {
    RawDataWrapperDto wrapper = fetchRawDataFromUrl(apiUrl);

    if (wrapper == null || wrapper.getRaw() == null || wrapper.getRaw().isEmpty()) {
      log.warn("No raw sensor data found in the response from URL: {}", apiUrl);
      return;
    }

    for (RawDataItemDto item : wrapper.getRaw()) {
      processRawItem(item);
    }

    log.info("Completed fetching & storing raw sensor data from {}", apiUrl);
  }

  private void processRawItem(RawDataItemDto item) {
    try {
      LocalDateTime obsTime = LocalDateTime.parse(item.getObsTime(), FORMATTER);
      String stationId = item.getStationId();
      String csq = item.getCsq();
      // Check duplicates
      if (sensorDataRepository.existsByStationIdAndObsTimeAndCsq(stationId, obsTime, csq)) {
        log.info(
            "Duplicate data. Skipping stationId={}, obsTime={}, csq={}", stationId, obsTime, csq);
        return;
      }
      // Parse main sensor data
      SensorData data = parseSensorData(item, obsTime);

      if (data == null) {
        log.warn("Sensor data was null for item: {}", item);
        return;
      }

      sensorDataRepository.save(data);
      // Parse temporary sensor data for aggregation
      TempSensorData tempData = parseTempSensorData(item, obsTime);

      if (tempData == null) {
        log.warn("Temp sensor data was null for item: {}", item);
        return;
      }

      tempSensorDataRepository.save(tempData);
    } catch (Exception e) {
      log.error("Error processing raw item: {}", item, e);
    }
  }

  RawDataWrapperDto fetchRawDataFromUrl(String url) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      log.error("Failed to fetch data from {}, HTTP status: {}", url, response.getStatusCode());
      throw new DataFetchException("Failed fetching data from " + url);
    }

    try {
      return objectMapper.readValue(response.getBody(), RawDataWrapperDto.class);
    } catch (Exception e) {
      log.error("Error parsing JSON from {}", url, e);
      throw new DataParseException("Failed to parse JSON from " + url, e);
    }
  }

  // Helper: Extract common sensor values from RawDataItemDto.Sensor
  private SensorValues extractSensorValues(RawDataItemDto.Sensor sensor) {
    SensorValues values = new SensorValues();

    if (sensor.getVolt() != null) {
      values.v1 = sensor.getVolt().getV1();
      values.v2 = sensor.getVolt().getV2();
      values.v3 = sensor.getVolt().getV3();
      values.v4 = sensor.getVolt().getV4();
      values.v5 = sensor.getVolt().getV5();
      values.v6 = sensor.getVolt().getV6();
      values.v7 = sensor.getVolt().getV7();
    }

    if (sensor.getStickTxRh() != null) {
      values.rh = sensor.getStickTxRh().getRh();
      values.tx = sensor.getStickTxRh().getTx();
    }

    if (sensor.getUltrasonicLevel() != null) {
      values.echo = sensor.getUltrasonicLevel().getEcho();
    }

    if (sensor.getWaterSpeedAquark() != null) {
      values.speed = sensor.getWaterSpeedAquark().getSpeed();
    }

    return values;
  }

  // Parse main SensorData from raw item
  private SensorData parseSensorData(RawDataItemDto item, LocalDateTime obsTime) {
    RawDataItemDto.Sensor sensor = item.getSensor();

    if (sensor == null) {
      return null;
    }

    SensorValues values = extractSensorValues(sensor);
    return SensorData.builder()
        .stationId(item.getStationId())
        .obsTime(obsTime)
        .csq(item.getCsq())
        .rainD(item.getRainD())
        .v1(values.v1)
        .v2(values.v2)
        .v3(values.v3)
        .v4(values.v4)
        .v5(values.v5)
        .v6(values.v6)
        .v7(values.v7)
        .rh(values.rh)
        .tx(values.tx)
        .echo(values.echo)
        .speed(values.speed)
        .build();
  }

  // Parse TempSensorData (for aggregator usage) from raw item
  private TempSensorData parseTempSensorData(RawDataItemDto item, LocalDateTime obsTime) {
    RawDataItemDto.Sensor sensor = item.getSensor();

    if (sensor == null) {
      return null;
    }

    SensorValues values = extractSensorValues(sensor);
    return TempSensorData.builder()
        .stationId(item.getStationId())
        .obsTime(obsTime)
        .csq(item.getCsq())
        .rainD(item.getRainD())
        .v1(values.v1)
        .v2(values.v2)
        .v3(values.v3)
        .v4(values.v4)
        .v5(values.v5)
        .v6(values.v6)
        .v7(values.v7)
        .rh(values.rh)
        .tx(values.tx)
        .echo(values.echo)
        .speed(values.speed)
        .build();
  }

  // Helper class to hold sensor values
  private static class SensorValues {
    BigDecimal v1;
    BigDecimal v2;
    BigDecimal v3;
    BigDecimal v4;
    BigDecimal v5;
    BigDecimal v6;
    BigDecimal v7;
    BigDecimal rh;
    BigDecimal tx;
    BigDecimal echo;
    BigDecimal speed;
  }

  // ----------------------------------------------------------------------------
  // Reading from the aggregator tables
  // ----------------------------------------------------------------------------
  @Override
  @Cacheable(
      value = "hourlyAggregation",
      key = "#start.toLocalDate().toString() + '-' + #end.toLocalDate().toString()")
  public List<HourlyAggregation> getHourlyAverage(LocalDateTime start, LocalDateTime end) {
    LocalDate startDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();
    return hourlyAggregationRepository.findByObsDateBetween(startDate, endDate);
  }

  @Override
  public List<DailyAggregation> getDailyAverage(LocalDateTime start, LocalDateTime end) {
    LocalDate startDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();
    return dailyAggregationRepository.findByObsDateBetween(startDate, endDate);
  }

  @Override
  public List<SensorData> getPeakTimeData(LocalDateTime start, LocalDateTime end) {
    List<SensorData> allData = aggregationService.getSensorDataByTimeRange(start, end);
    return allData.stream()
        .filter(Objects::nonNull)
        .filter(data -> isPeakTime(data.getObsTime()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SensorData> getOffPeakTimeData(LocalDateTime start, LocalDateTime end) {
    List<SensorData> allData = aggregationService.getSensorDataByTimeRange(start, end);
    return allData.stream()
        .filter(Objects::nonNull)
        .filter(data -> !isPeakTime(data.getObsTime()))
        .collect(Collectors.toList());
  }

  private boolean isPeakTime(LocalDateTime dateTime) {
    DayOfWeek day = dateTime.getDayOfWeek();
    LocalTime time = dateTime.toLocalTime();
    LocalTime startPeak = LocalTime.of(7, 30);
    LocalTime endPeak = LocalTime.of(17, 30);
    return switch (day) {
      case MONDAY, TUESDAY, WEDNESDAY -> (!time.isBefore(startPeak) && time.isBefore(endPeak));
      case THURSDAY, FRIDAY -> true;
      case SATURDAY, SUNDAY -> false;
    };
  }
}
