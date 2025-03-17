package com.jack.aquark.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.aquark.dto.RawDataItemDto;
import com.jack.aquark.dto.RawDataWrapperDto;
import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.repository.DailyAggregationRepository;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.SensorDataRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.SensorDataService;
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

  private final SensorDataRepository sensorDataRepository;
  private final TempSensorDataRepository tempSensorDataRepository;
  private final HourlyAggregationRepository hourlyAggregationRepository;
  private final DailyAggregationRepository dailyAggregationRepository;
  private final AggregationService aggregationService;
  private final ObjectMapper objectMapper;

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * 1) Fetch & save raw sensor data to both SensorData + TempSensorData. No aggregator logic
   * here—unchanged from your typical approach.
   */
  @Override
  @CacheEvict(
      value = {"sensorData", "hourlyAggregation"},
      allEntries = true)
  public void fetchAndSaveSensorData(String apiUrl) {
    try {
      // Fetch the wrapper from external URL
      RawDataWrapperDto wrapper = fetchRawDataFromUrl(apiUrl);
      if (wrapper == null || wrapper.getRaw() == null || wrapper.getRaw().isEmpty()) {
        log.warn("No raw sensor data found in the response from URL: {}", apiUrl);
        return;
      }

      // Iterate raw items
      for (RawDataItemDto item : wrapper.getRaw()) {
        try {
          LocalDateTime obsTime = LocalDateTime.parse(item.getObsTime(), FORMATTER);
          String stationId = item.getStationId();
          String csq = item.getCsq();

          // Check duplicates
          if (sensorDataRepository.existsByStationIdAndObsTimeAndCsq(stationId, obsTime, csq)) {
            log.info(
                "Duplicate data. Skipping stationId={}, obsTime={}, csq={}",
                stationId,
                obsTime,
                csq);
            continue;
          }

          // Create & save main sensor data
          SensorData data = parseSensorData(item, obsTime);
          sensorDataRepository.save(data);

          // Create & save temp sensor data (for aggregator usage)
          TempSensorData tempData = parseTempSensorData(item, obsTime);
          tempSensorDataRepository.save(tempData);

        } catch (Exception e) {
          log.error("Error processing raw item: {}", item, e);
        }
      }

      log.info("Completed fetching & storing raw sensor data from {}", apiUrl);

    } catch (Exception e) {
      log.error("Error fetching/saving sensor data from URL: {}", apiUrl, e);
    }
  }

  RawDataWrapperDto fetchRawDataFromUrl(String url) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      log.error("Failed to fetch data from {}, HTTP status: {}", url, response.getStatusCode());
      throw new RuntimeException("Failed fetching data from " + url);
    }

    try {
      return objectMapper.readValue(response.getBody(), RawDataWrapperDto.class);
    } catch (Exception e) {
      log.error("Error parsing JSON from {}", url, e);
      throw new RuntimeException("Failed to parse JSON from " + url, e);
    }
  }

  private SensorData parseSensorData(RawDataItemDto item, LocalDateTime obsTime) {
    RawDataItemDto.Sensor sensor = item.getSensor();
    return SensorData.builder()
        .stationId(item.getStationId())
        .obsTime(obsTime)
        .csq(item.getCsq())
        .rainD(item.getRainD())
        .v1(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV1() : null)
        .v2(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV2() : null)
        .v3(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV3() : null)
        .v4(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV4() : null)
        .v5(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV5() : null)
        .v6(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV6() : null)
        .v7(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV7() : null)
        .rh(sensor != null && sensor.getStickTxRh() != null ? sensor.getStickTxRh().getRh() : null)
        .tx(sensor != null && sensor.getStickTxRh() != null ? sensor.getStickTxRh().getTx() : null)
        .echo(
            sensor != null && sensor.getUltrasonicLevel() != null
                ? sensor.getUltrasonicLevel().getEcho()
                : null)
        .speed(
            sensor != null && sensor.getWaterSpeedAquark() != null
                ? sensor.getWaterSpeedAquark().getSpeed()
                : null)
        .build();
  }

  private TempSensorData parseTempSensorData(RawDataItemDto item, LocalDateTime obsTime) {
    RawDataItemDto.Sensor sensor = item.getSensor();
    return TempSensorData.builder()
        .stationId(item.getStationId())
        .obsTime(obsTime)
        .csq(item.getCsq())
        .rainD(item.getRainD())
        .v1(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV1() : null)
        .v2(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV2() : null)
        .v3(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV3() : null)
        .v4(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV4() : null)
        .v5(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV5() : null)
        .v6(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV6() : null)
        .v7(sensor != null && sensor.getVolt() != null ? sensor.getVolt().getV7() : null)
        .rh(sensor != null && sensor.getStickTxRh() != null ? sensor.getStickTxRh().getRh() : null)
        .tx(sensor != null && sensor.getStickTxRh() != null ? sensor.getStickTxRh().getTx() : null)
        .echo(
            sensor != null && sensor.getUltrasonicLevel() != null
                ? sensor.getUltrasonicLevel().getEcho()
                : null)
        .speed(
            sensor != null && sensor.getWaterSpeedAquark() != null
                ? sensor.getWaterSpeedAquark().getSpeed()
                : null)
        .build();
  }

  // ----------------------------------------------------------------------------
  // Reading from the aggregator tables (unchanged aggregator code).
  // Just do "like getHourlyData" => read from HourlyAggregationRepository &
  // DailyAggregationRepository
  // ----------------------------------------------------------------------------

  @Override
  @Cacheable(
      value = "hourlyAggregation",
      key = "#start.toLocalDate().toString() + '_' + #end.toLocalDate().toString()")
  public List<HourlyAggregation> getHourlyAverage(LocalDateTime start, LocalDateTime end) {
    // aggregator logic remains in AggregationServiceImpl, but we only need to do a simple read:
    LocalDate startDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();
    return hourlyAggregationRepository.findByObsDateBetween(startDate, endDate);
  }

  @Override
  public List<DailyAggregation> getDailyAverage(LocalDateTime start, LocalDateTime end) {
    // same approach as hourly, but reading from daily_aggregation
    LocalDate startDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();
    return dailyAggregationRepository.findByObsDateBetween(startDate, endDate);
  }

  @Override
  public List<SensorData> getPeakTimeData(LocalDateTime start, LocalDateTime end) {
    // aggregatorService has getSensorDataByTimeRange(...) that returns List<SensorData>
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
