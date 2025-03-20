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
    try {
      // Fetch the wrapper from the external URL
      RawDataWrapperDto wrapper = fetchRawDataFromUrl(apiUrl);
      if (wrapper == null || wrapper.getRaw() == null || wrapper.getRaw().isEmpty()) {
        log.warn("No raw sensor data found in the response from URL: {}", apiUrl);
        return;
      }

      // Iterate raw items
      for (RawDataItemDto item : wrapper.getRaw()) {
        processRawItem(item);
      }

      log.info("Completed fetching & storing raw sensor data from {}", apiUrl);

    } catch (Exception e) {
      log.error("Error fetching/saving sensor data from URL: {}", apiUrl, e);
    }
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

      // Create & save main sensor data
      SensorData data = parseSensorData(item, obsTime);
      if (data == null) {
        log.warn("Sensor data was null for item: {}", item);
        return;
      }
      sensorDataRepository.save(data);

      // Create & save temp sensor data (for aggregator usage)
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

  private SensorData parseSensorData(RawDataItemDto item, LocalDateTime obsTime) {
    RawDataItemDto.Sensor sensor = item.getSensor();
    if (sensor == null) {
      return null;
    }

    BigDecimal v1 = null;
    BigDecimal v2 = null;
    BigDecimal v3 = null;
    BigDecimal v4 = null;
    BigDecimal v5 = null;
    BigDecimal v6 = null;
    BigDecimal v7 = null;
    if (sensor.getVolt() != null) {
      v1 = sensor.getVolt().getV1();
      v2 = sensor.getVolt().getV2();
      v3 = sensor.getVolt().getV3();
      v4 = sensor.getVolt().getV4();
      v5 = sensor.getVolt().getV5();
      v6 = sensor.getVolt().getV6();
      v7 = sensor.getVolt().getV7();
    }

    BigDecimal rh = null;
    BigDecimal tx = null;
    if (sensor.getStickTxRh() != null) {
      rh = sensor.getStickTxRh().getRh();
      tx = sensor.getStickTxRh().getTx();
    }

    BigDecimal echo = null;
    if (sensor.getUltrasonicLevel() != null) {
      echo = sensor.getUltrasonicLevel().getEcho();
    }

    BigDecimal speed = null;
    if (sensor.getWaterSpeedAquark() != null) {
      speed = sensor.getWaterSpeedAquark().getSpeed();
    }

    return SensorData.builder()
        .stationId(item.getStationId())
        .obsTime(obsTime)
        .csq(item.getCsq())
        .rainD(item.getRainD())
        .v1(v1)
        .v2(v2)
        .v3(v3)
        .v4(v4)
        .v5(v5)
        .v6(v6)
        .v7(v7)
        .rh(rh)
        .tx(tx)
        .echo(echo)
        .speed(speed)
        .build();
  }

  private TempSensorData parseTempSensorData(RawDataItemDto item, LocalDateTime obsTime) {
    RawDataItemDto.Sensor sensor = item.getSensor();
    if (sensor == null) {
      return null;
    }

    BigDecimal v1 = null;
    BigDecimal v2 = null;
    BigDecimal v3 = null;
    BigDecimal v4 = null;
    BigDecimal v5 = null;
    BigDecimal v6 = null;
    BigDecimal v7 = null;
    if (sensor.getVolt() != null) {
      v1 = sensor.getVolt().getV1();
      v2 = sensor.getVolt().getV2();
      v3 = sensor.getVolt().getV3();
      v4 = sensor.getVolt().getV4();
      v5 = sensor.getVolt().getV5();
      v6 = sensor.getVolt().getV6();
      v7 = sensor.getVolt().getV7();
    }

    BigDecimal rh = null;
    BigDecimal tx = null;
    if (sensor.getStickTxRh() != null) {
      rh = sensor.getStickTxRh().getRh();
      tx = sensor.getStickTxRh().getTx();
    }

    BigDecimal echo = null;
    if (sensor.getUltrasonicLevel() != null) {
      echo = sensor.getUltrasonicLevel().getEcho();
    }

    BigDecimal speed = null;
    if (sensor.getWaterSpeedAquark() != null) {
      speed = sensor.getWaterSpeedAquark().getSpeed();
    }

    return TempSensorData.builder()
        .stationId(item.getStationId())
        .obsTime(obsTime)
        .csq(item.getCsq())
        .rainD(item.getRainD())
        .v1(v1)
        .v2(v2)
        .v3(v3)
        .v4(v4)
        .v5(v5)
        .v6(v6)
        .v7(v7)
        .rh(rh)
        .tx(tx)
        .echo(echo)
        .speed(speed)
        .build();
  }

  // ----------------------------------------------------------------------------
  // Reading from the aggregator tables
  // ----------------------------------------------------------------------------

  @Override
  @Cacheable(
      value = "hourlyAggregation",
      key = "#start.toLocalDate().toString() + '_' + #end.toLocalDate().toString()")
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
