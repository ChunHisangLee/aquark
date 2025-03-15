package com.jack.aquark.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.aquark.dto.RawDataItemDto;
import com.jack.aquark.dto.RawDataWrapperDto;
import com.jack.aquark.dto.SummariesDto;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.SensorDataRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import com.jack.aquark.service.SensorDataService;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
      if (wrapper != null && wrapper.getRaw() != null) {
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

    try {
      return objectMapper.readValue(response.getBody(), RawDataWrapperDto.class);
    } catch (Exception e) {
      log.error("Error parsing raw data from URL", e);
      throw new RuntimeException("Failed to parse JSON", e);
    }
  }

  @Override
  public SummariesDto getSummaries(LocalDateTime start, LocalDateTime end) {
    List<SensorData> dataList =
        sensorDataRepository.findAllByObsTimeBetweenOrderByObsTimeAsc(start, end);

    if (dataList.isEmpty()) {
      return new SummariesDto();
    }

    // Initialize sums as BigDecimal.ZERO and counts as ints
    BigDecimal sumV1 = BigDecimal.ZERO,
        sumV2 = BigDecimal.ZERO,
        sumV3 = BigDecimal.ZERO,
        sumV4 = BigDecimal.ZERO,
        sumV5 = BigDecimal.ZERO,
        sumV6 = BigDecimal.ZERO,
        sumV7 = BigDecimal.ZERO;
    int countV1 = 0, countV2 = 0, countV3 = 0, countV4 = 0, countV5 = 0, countV6 = 0, countV7 = 0;
    BigDecimal sumRh = BigDecimal.ZERO,
        sumTx = BigDecimal.ZERO,
        sumEcho = BigDecimal.ZERO,
        sumSpeed = BigDecimal.ZERO;
    int countRh = 0, countTx = 0, countEcho = 0, countSpeed = 0;
    BigDecimal maxRainD = BigDecimal.ZERO;

    for (SensorData s : dataList) {
      if (s.getV1() != null) {
        sumV1 = sumV1.add(s.getV1());
        countV1++;
      }
      if (s.getV2() != null) {
        sumV2 = sumV2.add(s.getV2());
        countV2++;
      }
      if (s.getV3() != null) {
        sumV3 = sumV3.add(s.getV3());
        countV3++;
      }
      if (s.getV4() != null) {
        sumV4 = sumV4.add(s.getV4());
        countV4++;
      }
      if (s.getV5() != null) {
        sumV5 = sumV5.add(s.getV5());
        countV5++;
      }
      if (s.getV6() != null) {
        sumV6 = sumV6.add(s.getV6());
        countV6++;
      }
      if (s.getV7() != null) {
        sumV7 = sumV7.add(s.getV7());
        countV7++;
      }
      if (s.getRh() != null) {
        sumRh = sumRh.add(s.getRh());
        countRh++;
      }
      if (s.getTx() != null) {
        sumTx = sumTx.add(s.getTx());
        countTx++;
      }
      if (s.getEcho() != null) {
        sumEcho = sumEcho.add(s.getEcho());
        countEcho++;
      }
      if (s.getSpeed() != null) {
        sumSpeed = sumSpeed.add(s.getSpeed());
        countSpeed++;
      }
      if (s.getRainD() != null) {
        // Use maxRainD as the maximum rain value recorded
        if (s.getRainD().compareTo(maxRainD) > 0) {
          maxRainD = s.getRainD();
        }
      }
    }

    SummariesDto summariesDto = new SummariesDto();
    summariesDto.setSumV1(sumV1);
    summariesDto.setSumV2(sumV2);
    summariesDto.setSumV3(sumV3);
    summariesDto.setSumV4(sumV4);
    summariesDto.setSumV5(sumV5);
    summariesDto.setSumV6(sumV6);
    summariesDto.setSumV7(sumV7);
    summariesDto.setSumRh(sumRh);
    summariesDto.setSumTx(sumTx);
    summariesDto.setSumEcho(sumEcho);
    summariesDto.setSumSpeed(sumSpeed);
    summariesDto.setSumRainD(maxRainD);

    // Calculate averages with scale 2 and HALF_UP rounding
    summariesDto.setAvgV1(
        countV1 == 0
            ? BigDecimal.ZERO
            : sumV1.divide(BigDecimal.valueOf(countV1), 2, RoundingMode.HALF_UP));
    summariesDto.setAvgV2(
        countV2 == 0
            ? BigDecimal.ZERO
            : sumV2.divide(BigDecimal.valueOf(countV2), 2, RoundingMode.HALF_UP));
    summariesDto.setAvgV3(
        countV3 == 0
            ? BigDecimal.ZERO
            : sumV3.divide(BigDecimal.valueOf(countV3), 2, RoundingMode.HALF_UP));
    summariesDto.setAvgV4(
        countV4 == 0
            ? BigDecimal.ZERO
            : sumV4.divide(BigDecimal.valueOf(countV4), 2, RoundingMode.HALF_UP));
    summariesDto.setAvgV5(
        countV5 == 0
            ? BigDecimal.ZERO
            : sumV5.divide(BigDecimal.valueOf(countV5), 2, RoundingMode.HALF_UP));
    summariesDto.setAvgV6(
        countV6 == 0
            ? BigDecimal.ZERO
            : sumV6.divide(BigDecimal.valueOf(countV6), 2, RoundingMode.HALF_UP));
    summariesDto.setAvgV7(
        countV7 == 0
            ? BigDecimal.ZERO
            : sumV7.divide(BigDecimal.valueOf(countV7), 2, RoundingMode.HALF_UP));
    summariesDto.setAvgRh(
        countRh == 0
            ? BigDecimal.ZERO
            : sumRh.divide(BigDecimal.valueOf(countRh), 2, RoundingMode.HALF_UP));
    summariesDto.setAvgTx(
        countTx == 0
            ? BigDecimal.ZERO
            : sumTx.divide(BigDecimal.valueOf(countTx), 2, RoundingMode.HALF_UP));
    summariesDto.setAvgEcho(
        countEcho == 0
            ? BigDecimal.ZERO
            : sumEcho.divide(BigDecimal.valueOf(countEcho), 2, RoundingMode.HALF_UP));
    summariesDto.setAvgSpeed(
        countSpeed == 0
            ? BigDecimal.ZERO
            : sumSpeed.divide(BigDecimal.valueOf(countSpeed), 2, RoundingMode.HALF_UP));

    return summariesDto;
  }

  @Override
  public List<SensorData> getSensorDataByTimeRange(LocalDateTime start, LocalDateTime end) {
    log.info("Querying DB for sensor data between {} and {}", start, end);
    return sensorDataRepository.findAllByObsTimeBetweenOrderByObsTimeAsc(start, end);
  }

  @Override
  public List<HourlyAggregation> getHourlyAverage(LocalDateTime start, LocalDateTime end) {
    // Convert the LocalDateTime parameters to LocalDate.
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
      case MONDAY, TUESDAY, WEDNESDAY ->
          // Peak if 07:30 <= time < 17:30
          !time.isBefore(startPeak) && time.isBefore(endPeak);
      case THURSDAY, FRIDAY ->
          // Peak all day
          true;
      case SATURDAY, SUNDAY ->
          // Off-peak all day
          false;
    };
  }
}
