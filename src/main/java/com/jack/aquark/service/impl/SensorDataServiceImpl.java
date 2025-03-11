package com.jack.aquark.service.impl;

import com.jack.aquark.dto.RawDataItem;
import com.jack.aquark.dto.RawDataWrapper;
import com.jack.aquark.dto.Summaries;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.repository.SensorDataRepository;
import com.jack.aquark.service.SensorDataService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SensorDataServiceImpl implements SensorDataService {
  private final SensorDataRepository sensorDataRepository;

  @Override
  public void saveRawData(RawDataWrapper wrapper) {
    if (wrapper == null || wrapper.getRaw() == null) {
      return;
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    for (RawDataItem item : wrapper.getRaw()) {
      // Parse obs_time to LocalDateTime
      LocalDateTime obsTime = null;
      try {
        obsTime = LocalDateTime.parse(item.getObsTime(), formatter);
      } catch (Exception e) {
        // handle parse error
        e.printStackTrace();
        continue; // skip this record
      }

      // Extract v1, v5, rh, tx, echo, speed, etc. from item.sensor
      Double v1 =
          item.getSensor() != null && item.getSensor().getVolt() != null
              ? item.getSensor().getVolt().getV1()
              : null;

      Double v5 =
          item.getSensor() != null && item.getSensor().getVolt() != null
              ? item.getSensor().getVolt().getV5()
              : null;

      Double rh =
          item.getSensor() != null && item.getSensor().getStickTxRh() != null
              ? item.getSensor().getStickTxRh().getRh()
              : null;

      Double tx =
          item.getSensor() != null && item.getSensor().getStickTxRh() != null
              ? item.getSensor().getStickTxRh().getTx()
              : null;

      Double echo =
          item.getSensor() != null && item.getSensor().getUltrasonicLevel() != null
              ? item.getSensor().getUltrasonicLevel().getEcho()
              : null;

      Double speed =
          item.getSensor() != null && item.getSensor().getWaterSpeedAquark() != null
              ? item.getSensor().getWaterSpeedAquark().getSpeed()
              : null;
      // store absolute value
      if (speed != null) speed = Math.abs(speed);

      // Build entity
      SensorData sensorData =
          SensorData.builder()
              .stationId(item.getStationId())
              .obsTime(obsTime)
              .v1(v1)
              .v5(v5)
              .rh(rh)
              .tx(tx)
              .echo(echo)
              .speed(speed)
              .rainD(item.getRainD()) // day rainfall
              .build();

      sensorDataRepository.save(sensorData);
    }
  }

  @Override
  public Summaries getSummaries(LocalDateTime start, LocalDateTime end) {
    List<SensorData> dataList = sensorDataRepository.findAllByObsTimeBetween(start, end);
    if (dataList.isEmpty()) {
      return new Summaries(); // everything 0 by default
    }

    double sumV1 = 0;
    double sumV5 = 0;
    double sumRh = 0;
    double sumTx = 0;
    double sumEcho = 0;
    double sumRainD = 0;
    double sumSpeed = 0;
    int countV1 = 0;
    int countV5 = 0;
    int countRh = 0;
    int countTx = 0;
    int countEcho = 0;
    int countSpeed = 0;
    double maxRainD = 0; // optional if you want total or daily

    for (SensorData s : dataList) {
      if (s.getV1() != null) {
        sumV1 += s.getV1();
        countV1++;
      }
      if (s.getV5() != null) {
        sumV5 += s.getV5();
        countV5++;
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
        // Depending on your use-case, you might sum or track the highest "rainD" in that day
        maxRainD = Math.max(maxRainD, s.getRainD());
      }
    }

    Summaries summaries = new Summaries();
    summaries.setSumV1(sumV1);
    summaries.setSumV5(sumV5);
    summaries.setSumRh(sumRh);
    summaries.setSumTx(sumTx);
    summaries.setSumEcho(sumEcho);
    summaries.setSumSpeed(sumSpeed);

    // If you want total rain:
    //   it might be sum of all daily rains or the maximum reading for that day
    //   for demonstration, let's store it as a total (or you can store maxRainD)
    summaries.setSumRainD(maxRainD);

    // Averages
    summaries.setAvgV1(countV1 == 0 ? 0 : sumV1 / countV1);
    summaries.setAvgV5(countV5 == 0 ? 0 : sumV5 / countV5);
    summaries.setAvgRh(countRh == 0 ? 0 : sumRh / countRh);
    summaries.setAvgTx(countTx == 0 ? 0 : sumTx / countTx);
    summaries.setAvgEcho(countEcho == 0 ? 0 : sumEcho / countEcho);
    summaries.setAvgSpeed(countSpeed == 0 ? 0 : sumSpeed / countSpeed);

    return summaries;
  }
}
