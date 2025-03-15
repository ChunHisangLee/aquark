package com.jack.aquark.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.aquark.dto.RawDataItemDto;
import com.jack.aquark.dto.RawDataWrapperDto;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.SensorDataRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import com.jack.aquark.service.SensorDataService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final ObjectMapper objectMapper;
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Override
  public void fetchAndSaveSensorData(String apiUrl) {
    try {
      RawDataWrapperDto wrapper = fetchRawDataFromUrl(apiUrl);
      if (wrapper == null || wrapper.getRaw() == null || wrapper.getRaw().isEmpty()) {
        log.warn("No raw sensor data found in the response from URL: {}", apiUrl);
        return;
      }
      for (RawDataItemDto item : wrapper.getRaw()) {
        try {
          LocalDateTime obsTime = LocalDateTime.parse(item.getObsTime(), formatter);
          String stationId = item.getStationId();
          String csq = item.getCsq();

          // Check duplicate using stationId, obsTime, and csq.
          if (sensorDataRepository.existsByStationIdAndObsTimeAndCsq(stationId, obsTime, csq)) {
            log.info(
                "Duplicate record found for station {} at {} with csq {}. Skipping.",
                stationId,
                obsTime,
                csq);
            continue;
          }

          SensorData data = parseSensorData(item);
          sensorDataRepository.save(data);

          // Also save to temp table.
          TempSensorData tempData = parseTempSensorData(item);
          tempSensorDataRepository.save(tempData);
        } catch (Exception e) {
          log.error("Error processing sensor data item: {}", item, e);
        }
      }
    } catch (Exception e) {
      log.error("Error fetching and saving sensor data from URL: {}", apiUrl, e);
    }
  }

  private SensorData parseSensorData(RawDataItemDto item) {
    LocalDateTime obsTime = LocalDateTime.parse(item.getObsTime(), formatter);
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

  private TempSensorData parseTempSensorData(RawDataItemDto item) {
    LocalDateTime obsTime = LocalDateTime.parse(item.getObsTime(), formatter);
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

  @Override
  public RawDataWrapperDto fetchRawDataFromUrl(String url) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Check if HTTP response is successful
    if (!response.getStatusCode().is2xxSuccessful()) {
      log.error(
          "Failed to fetch data from URL: {}. HTTP Status: {}", url, response.getStatusCode());
      throw new RuntimeException("Failed to fetch data from URL: " + url);
    }

    try {
      return objectMapper.readValue(response.getBody(), RawDataWrapperDto.class);
    } catch (Exception e) {
      log.error("Error parsing raw data from URL", e);
      throw new RuntimeException("Failed to parse JSON", e);
    }
  }
  @Override
  public List<SensorData> getSensorDataByTimeRange(LocalDateTime start, LocalDateTime end) {
    log.info("Querying DB for sensor data between {} and {}", start, end);
    return sensorDataRepository.findAllByObsTimeBetweenOrderByObsTimeAsc(start, end);
  }

  @Override
  public List<HourlyAggregation> getHourlyAverage(LocalDateTime start, LocalDateTime end) {
    LocalDate startDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();
    return hourlyAggregationRepository.findByObsDateBetween(startDate, endDate);
  }

  @Override
  public List<SensorData> getPeakTimeData(LocalDateTime start, LocalDateTime end) {
    List<SensorData> allData = getSensorDataByTimeRange(start, end);
    return allData.stream()
        .filter(data -> isPeakTime(data.getObsTime()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SensorData> getOffPeakTimeData(LocalDateTime start, LocalDateTime end) {
    List<SensorData> allData = getSensorDataByTimeRange(start, end);
    return allData.stream()
        .filter(data -> !isPeakTime(data.getObsTime()))
        .collect(Collectors.toList());
  }

  private boolean isPeakTime(LocalDateTime dateTime) {
    DayOfWeek day = dateTime.getDayOfWeek();
    LocalTime time = dateTime.toLocalTime();

    LocalTime startPeak = LocalTime.of(7, 30);
    LocalTime endPeak = LocalTime.of(17, 30);

    return switch (day) {
      case MONDAY, TUESDAY, WEDNESDAY -> !time.isBefore(startPeak) && time.isBefore(endPeak);
      case THURSDAY, FRIDAY -> true;
      case SATURDAY, SUNDAY -> false;
    };
  }
}
