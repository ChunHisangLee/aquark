package com.jack.aquark.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.aquark.config.SchedulingProperties;
import com.jack.aquark.dto.RawDataItemDto;
import com.jack.aquark.dto.RawDataWrapperDto;
import com.jack.aquark.dto.SummariesDto;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.repository.SensorDataRepository;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class SensorDataServiceImpl implements SensorDataService {
  private final SensorDataRepository sensorDataRepository;
  private final SchedulingProperties schedulingProperties;
  private final ObjectMapper objectMapper;
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Override
  public void fetchAndSaveSensorData(String apiUrl) {
    try {
      RawDataWrapperDto wrapper = fetchRawDataFromUrl(apiUrl);
      if (wrapper != null && wrapper.getRaw() != null) {

        for (RawDataItemDto item : wrapper.getRaw()) {
          try {
            SensorData data = parseSensorData(item);
            sensorDataRepository.save(data);
          } catch (Exception e) {
            log.error("Error Fetch And Save Sensor Data: {}", item, e);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error fetching and saving sensor data from URL: {}", apiUrl, e);
    }
  }

  private SensorData parseSensorData(RawDataItemDto item) {
    LocalDateTime obsTime = LocalDateTime.parse(item.getObsTime(), formatter);
    SensorData.SensorDataBuilder builder =
        SensorData.builder()
            .stationId(item.getStationId())
            .obsTime(obsTime)
            .csq(item.getCsq())
            .rainD(item.getRainD());

    populateSensorFields(item.getSensor(), builder);
    return builder.build();
  }

  private void populateSensorFields(
      RawDataItemDto.Sensor sensor, SensorData.SensorDataBuilder builder) {
    if (sensor != null) {
      if (sensor.getVolt() != null) {
        builder
            .v1(sensor.getVolt().getV1())
            .v2(sensor.getVolt().getV2())
            .v3(sensor.getVolt().getV3())
            .v4(sensor.getVolt().getV4())
            .v5(sensor.getVolt().getV5())
            .v6(sensor.getVolt().getV6())
            .v7(sensor.getVolt().getV7());
      }
      if (sensor.getStickTxRh() != null) {
        builder.rh(sensor.getStickTxRh().getRh()).tx(sensor.getStickTxRh().getTx());
      }
      if (sensor.getUltrasonicLevel() != null) {
        builder.echo(sensor.getUltrasonicLevel().getEcho());
      }
      if (sensor.getWaterSpeedAquark() != null) {
        Double speed = sensor.getWaterSpeedAquark().getSpeed();
        if (speed != null) {
          builder.speed(Math.abs(speed));
        }
      }
    }
  }

  @Override
  public void saveRawData(RawDataWrapperDto wrapper) {
    if (wrapper == null || wrapper.getRaw() == null) {
      return;
    }

    for (RawDataItemDto item : wrapper.getRaw()) {
      try {
        LocalDateTime obsTime = LocalDateTime.parse(item.getObsTime(), formatter);

        SensorData.SensorDataBuilder builder =
            SensorData.builder()
                .stationId(item.getStationId())
                .obsTime(obsTime)
                .csq(item.getCsq())
                .rainD(item.getRainD());

        populateSensorFields(item.getSensor(), builder);

        SensorData sensorData = builder.build();
        sensorDataRepository.save(sensorData);
      } catch (Exception e) {
        log.error("Error Saving Raw Data: {}", item, e);
      }
    }
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
  @Cacheable(value = "summaries", key = "#start.toString() + '_' + #end.toString()")
  public SummariesDto getSummaries(LocalDateTime start, LocalDateTime end) {
    List<SensorData> dataList = sensorDataRepository.findByTimeRange(start, end);

    if (dataList.isEmpty()) {
      return new SummariesDto();
    }

    double sumV1 = 0, sumV2 = 0, sumV3 = 0, sumV4 = 0, sumV5 = 0, sumV6 = 0, sumV7 = 0;
    int countV1 = 0, countV2 = 0, countV3 = 0, countV4 = 0, countV5 = 0, countV6 = 0, countV7 = 0;
    double sumRh = 0, sumTx = 0, sumEcho = 0, sumSpeed = 0;
    int countRh = 0, countTx = 0, countEcho = 0, countSpeed = 0;
    double maxRainD = 0;

    for (SensorData s : dataList) {
      if (s.getV1() != null) {
        sumV1 += s.getV1();
        countV1++;
      }

      if (s.getV2() != null) {
        sumV2 += s.getV2();
        countV2++;
      }

      if (s.getV3() != null) {
        sumV3 += s.getV3();
        countV3++;
      }

      if (s.getV4() != null) {
        sumV4 += s.getV4();
        countV4++;
      }

      if (s.getV5() != null) {
        sumV5 += s.getV5();
        countV5++;
      }

      if (s.getV6() != null) {
        sumV6 += s.getV6();
        countV6++;
      }

      if (s.getV7() != null) {
        sumV7 += s.getV7();
        countV7++;
      }

      if (s.getRh() != null) {
        sumRh += s.getRh();
        countRh++;
      }

      if (s.getTx() != null) {
        sumTx += s.getTx();
        countTx++;
      }

      if (s.getEcho() != null) {
        sumEcho += s.getEcho();
        countEcho++;
      }

      if (s.getSpeed() != null) {
        sumSpeed += s.getSpeed();
        countSpeed++;
      }

      if (s.getRainD() != null) {
        maxRainD = Math.max(maxRainD, s.getRainD());
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

    summariesDto.setAvgV1(countV1 == 0 ? 0 : sumV1 / countV1);
    summariesDto.setAvgV2(countV2 == 0 ? 0 : sumV2 / countV2);
    summariesDto.setAvgV3(countV3 == 0 ? 0 : sumV3 / countV3);
    summariesDto.setAvgV4(countV4 == 0 ? 0 : sumV4 / countV4);
    summariesDto.setAvgV5(countV5 == 0 ? 0 : sumV5 / countV5);
    summariesDto.setAvgV6(countV6 == 0 ? 0 : sumV6 / countV6);
    summariesDto.setAvgV7(countV7 == 0 ? 0 : sumV7 / countV7);
    summariesDto.setAvgRh(countRh == 0 ? 0 : sumRh / countRh);
    summariesDto.setAvgTx(countTx == 0 ? 0 : sumTx / countTx);
    summariesDto.setAvgEcho(countEcho == 0 ? 0 : sumEcho / countEcho);
    summariesDto.setAvgSpeed(countSpeed == 0 ? 0 : sumSpeed / countSpeed);

    return summariesDto;
  }

  @Override
  public List<SensorData> getSensorDataByDate(LocalDate date) {
    return sensorDataRepository.findByDate(date);
  }

  @Override
  public List<SensorData> getSensorDataBetween(LocalDateTime start, LocalDateTime end) {
    return sensorDataRepository.findByTimeRange(start, end);
  }

  @Override
  @Cacheable(value = "hourlyAverage", key = "#start.toString() + '_' + #end.toString()")
  public List<Object[]> getHourlyAverage(LocalDateTime start, LocalDateTime end) {
    return sensorDataRepository.findHourlyAverages(start, end);
  }

  @Override
  public List<SensorData> getLatestSensorData() {
    double intervalMinutes = schedulingProperties.getIntervalMinutes();
    return sensorDataRepository.findLatestSensorData(intervalMinutes);
  }

  @Override
  public List<SensorData> getPeakTimeData(LocalDateTime start, LocalDateTime end) {
    List<SensorData> allData = getSensorDataBetween(start, end);
    return allData.stream()
            .filter(data -> isPeakTime(data.getObsTime()))
            .collect(Collectors.toList());
  }

  @Override
  public List<SensorData> getOffPeakTimeData(LocalDateTime start, LocalDateTime end) {
    List<SensorData> allData = getSensorDataBetween(start, end);
    return allData.stream()
            .filter(data -> !isPeakTime(data.getObsTime()))
            .collect(Collectors.toList());
  }

  private boolean isPeakTime(LocalDateTime dateTime) {
    DayOfWeek day = dateTime.getDayOfWeek();
    LocalTime time = dateTime.toLocalTime();

    LocalTime startPeak = LocalTime.of(7, 30);
    LocalTime endPeak = LocalTime.of(17, 30);

    switch (day) {
      case MONDAY:
      case TUESDAY:
      case WEDNESDAY:
        // Peak if 07:30 <= time < 17:30
        return !time.isBefore(startPeak) && time.isBefore(endPeak);
      case THURSDAY:
      case FRIDAY:
        // Peak all day
        return true;
      case SATURDAY:
      case SUNDAY:
        // Off-peak all day
        return false;
      default:
        return false; // Fallback, shouldn't happen
    }
  }
}
