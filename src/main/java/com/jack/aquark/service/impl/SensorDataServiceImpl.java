package com.jack.aquark.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.aquark.dto.RawDataItem;
import com.jack.aquark.dto.RawDataWrapper;
import com.jack.aquark.dto.Summaries;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.repository.SensorDataRepository;
import com.jack.aquark.service.SensorDataService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
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
  private final ObjectMapper objectMapper;

  @Override
  public void fetchAndSaveSensorData(String apiUrl) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

    try {
      JsonNode root = objectMapper.readTree(response.getBody());
      JsonNode rawArray = root.path("raw");

      if (rawArray.isArray()) {
        Iterator<JsonNode> elements = rawArray.elements();

        while (elements.hasNext()) {
          JsonNode node = elements.next();
          SensorData data = parseSensorData(node);
          sensorDataRepository.save(data);
          // 檢查是否超過告警值 (可呼叫 AlarmThresholdService 檢查)
        }
      }
    } catch (Exception e) {
      log.error("Error fetch And Save Sensor Data", e);
    }
  }

  private SensorData parseSensorData(JsonNode node) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return SensorData.builder()
        .stationId(node.path("station_id").asText())
        .obsTime(LocalDateTime.parse(node.path("obs_time").asText(), formatter))
        .csq(node.path("CSQ").asText())
        .v1(node.path("sensor").path("Volt").path("v1").asDouble())
        .v2(node.path("sensor").path("Volt").path("v2").asDouble())
        .v3(node.path("sensor").path("Volt").path("v3").asDouble())
        .v4(node.path("sensor").path("Volt").path("v4").asDouble())
        .v5(node.path("sensor").path("Volt").path("v5").asDouble())
        .v6(node.path("sensor").path("Volt").path("v6").asDouble())
        .v7(node.path("sensor").path("Volt").path("v7").asDouble())
        .rh(node.path("sensor").path("StickTxRh").path("rh").asDouble())
        .tx(node.path("sensor").path("StickTxRh").path("tx").asDouble())
        .echo(node.path("sensor").path("Ultrasonic_Level").path("echo").asDouble())
        .rainD(node.path("rain_d").asDouble())
        .build();
  }

  @Override
  public void saveRawData(RawDataWrapper wrapper) {
    if (wrapper == null || wrapper.getRaw() == null) {
      return;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    for (RawDataItem item : wrapper.getRaw()) {
      LocalDateTime obsTime;

      try {
        obsTime = LocalDateTime.parse(item.getObsTime(), formatter);
      } catch (Exception e) {
        log.error("Error saving Raw Data: {} for item: {}", e, item);
        continue;
      }

      Double v1 = null, v2 = null, v3 = null, v4 = null, v5 = null, v6 = null, v7 = null;

      if (item.getSensor() != null && item.getSensor().getVolt() != null) {
        v1 = item.getSensor().getVolt().getV1();
        v2 = item.getSensor().getVolt().getV2();
        v3 = item.getSensor().getVolt().getV3();
        v4 = item.getSensor().getVolt().getV4();
        v5 = item.getSensor().getVolt().getV5();
        v6 = item.getSensor().getVolt().getV6();
        v7 = item.getSensor().getVolt().getV7();
      }

      Double rh = null, tx = null;

      if (item.getSensor() != null && item.getSensor().getStickTxRh() != null) {
        if (item.getSensor().getStickTxRh().getRh() != null) {
          rh = item.getSensor().getStickTxRh().getRh();
        }

        tx = item.getSensor().getStickTxRh().getTx();
      }

      Double echo = null;

      if (item.getSensor() != null && item.getSensor().getUltrasonicLevel() != null) {
        echo = item.getSensor().getUltrasonicLevel().getEcho();
      }

      Double speed = null;

      if (item.getSensor() != null && item.getSensor().getWaterSpeedAquark() != null) {
        speed = item.getSensor().getWaterSpeedAquark().getSpeed();

        if (speed != null) {
          speed = Math.abs(speed);
        }
      }

      String csq = item.getCSQ();
      Double rainD = item.getRainD();
      SensorData sensorData =
          SensorData.builder()
              .stationId(item.getStationId())
              .obsTime(obsTime)
              .csq(csq)
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
              .rainD(rainD)
              .build();

      sensorDataRepository.save(sensorData);
    }
  }

  @Override
  public RawDataWrapper fetchRawDataFromUrl(String url) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    try {
      return objectMapper.readValue(response.getBody(), RawDataWrapper.class);
    } catch (Exception e) {
      log.error("Error parsing raw data from URL", e);
      throw new RuntimeException("Failed to parse JSON", e);
    }
  }

  @Override
  @Cacheable(value = "summaries", key = "#start.toString() + '_' + #end.toString()")
  public Summaries getSummaries(LocalDateTime start, LocalDateTime end) {
    List<SensorData> dataList = sensorDataRepository.findAllByObsTimeBetween(start, end);

    if (dataList.isEmpty()) {
      return new Summaries();
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

    Summaries summaries = new Summaries();
    summaries.setSumV1(sumV1);
    summaries.setSumV2(sumV2);
    summaries.setSumV3(sumV3);
    summaries.setSumV4(sumV4);
    summaries.setSumV5(sumV5);
    summaries.setSumV6(sumV6);
    summaries.setSumV7(sumV7);
    summaries.setSumRh(sumRh);
    summaries.setSumTx(sumTx);
    summaries.setSumEcho(sumEcho);
    summaries.setSumSpeed(sumSpeed);
    summaries.setSumRainD(maxRainD);

    summaries.setAvgV1(countV1 == 0 ? 0 : sumV1 / countV1);
    summaries.setAvgV2(countV2 == 0 ? 0 : sumV2 / countV2);
    summaries.setAvgV3(countV3 == 0 ? 0 : sumV3 / countV3);
    summaries.setAvgV4(countV4 == 0 ? 0 : sumV4 / countV4);
    summaries.setAvgV5(countV5 == 0 ? 0 : sumV5 / countV5);
    summaries.setAvgV6(countV6 == 0 ? 0 : sumV6 / countV6);
    summaries.setAvgV7(countV7 == 0 ? 0 : sumV7 / countV7);
    summaries.setAvgRh(countRh == 0 ? 0 : sumRh / countRh);
    summaries.setAvgTx(countTx == 0 ? 0 : sumTx / countTx);
    summaries.setAvgEcho(countEcho == 0 ? 0 : sumEcho / countEcho);
    summaries.setAvgSpeed(countSpeed == 0 ? 0 : sumSpeed / countSpeed);

    return summaries;
  }

  @Override
  public List<SensorData> getSensorDataBetween(LocalDateTime start, LocalDateTime end) {
    return sensorDataRepository.findAllByObsTimeBetween(start, end);
  }

  @Override
  @Cacheable(value = "hourlyAverage", key = "#start.toString() + '_' + #end.toString()")
  public List<Object[]> getHourlyAverage(LocalDateTime start, LocalDateTime end) {
    return sensorDataRepository.findHourlyAverages(start, end);
  }
}
