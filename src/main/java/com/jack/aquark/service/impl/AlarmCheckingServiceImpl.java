package com.jack.aquark.service.impl;

import com.jack.aquark.dto.AlarmCheckResult;
import com.jack.aquark.dto.AlarmDetail;
import com.jack.aquark.dto.AlarmEvent;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.service.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AlarmCheckingServiceImpl implements AlarmCheckingService {

  private final AggregationService aggregationService;
  private final AlarmThresholdService alarmThresholdService;
  private final KafkaProducerService kafkaProducerService;

  @Override
  public AlarmCheckResult checkSensorAlarms(int intervalMinutes) {
    log.info("Checking sensor alarms for the last {} minutes...", intervalMinutes);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startTime = now.minusMinutes(intervalMinutes);
    List<SensorData> sensorDataList = aggregationService.getSensorDataByTimeRange(startTime, now);

    List<AlarmDetail> alarmDetails = new ArrayList<>();

    // Check various sensor parameters and publish events if triggered.
    checkAndAlarm(sensorDataList, "v1", alarmDetails);
    checkAndAlarm(sensorDataList, "v2", alarmDetails);
    checkAndAlarm(sensorDataList, "v3", alarmDetails);
    checkAndAlarm(sensorDataList, "v4", alarmDetails);
    checkAndAlarm(sensorDataList, "v5", alarmDetails);
    checkAndAlarm(sensorDataList, "v6", alarmDetails);
    checkAndAlarm(sensorDataList, "v7", alarmDetails);
    checkAndAlarm(sensorDataList, "rh", alarmDetails);
    checkAndAlarm(sensorDataList, "tx", alarmDetails);
    checkAndAlarm(sensorDataList, "echo", alarmDetails);
    checkAndAlarm(sensorDataList, "rainD", alarmDetails);
    checkAndAlarm(sensorDataList, "speed", alarmDetails);

    String message = "Alarm check completed. " + alarmDetails.size() + " alarms triggered.";
    log.info(message);
    return new AlarmCheckResult(alarmDetails.size(), alarmDetails, message);
  }

  private void checkAndAlarm(
      List<SensorData> sensorDataList, String parameter, List<AlarmDetail> alarmDetails) {
    for (SensorData data : sensorDataList) {
      BigDecimal value = getSensorValue(data, parameter);
      if (value == null) continue;
      try {
        var threshold =
            alarmThresholdService.getThreshold(data.getStationId(), data.getCsq(), parameter);
        if (value.compareTo(threshold.getThresholdValue()) > 0) {
          String msg =
              String.format(
                  "Alarm triggered for station %s, observation time %s, parameter %s, csq %s: value %s exceeds threshold %s",
                  data.getStationId(),
                  data.getObsTime(),
                  parameter,
                  data.getCsq(),
                  value.toPlainString(),
                  threshold.getThresholdValue().toPlainString());
          log.warn("Alarm sent: {}", msg);

          // Create an alarm detail.
          AlarmDetail detail =
              new AlarmDetail(
                  data.getStationId(),
                  data.getObsTime().toString(),
                  data.getCsq(),
                  parameter,
                  value,
                  threshold.getThresholdValue(),
                  msg);
          alarmDetails.add(detail);

          // Create and send a Kafka alarm event.
          AlarmEvent event =
              new AlarmEvent(
                  data.getStationId(),
                  data.getCsq(),
                  parameter,
                  value,
                  threshold.getThresholdValue(),
                  data.getObsTime(),
                  msg);
          kafkaProducerService.sendAlarmEvent(event);
        }
      } catch (RuntimeException e) {
        log.debug(
            "No threshold configured for station {} parameter {} with csq {}. Skipping alarm check.",
            data.getStationId(),
            parameter,
            data.getCsq());
      }
    }
  }

  private BigDecimal getSensorValue(SensorData data, String parameter) {
    return switch (parameter) {
      case "v1" -> data.getV1();
      case "v2" -> data.getV2();
      case "v3" -> data.getV3();
      case "v4" -> data.getV4();
      case "v5" -> data.getV5();
      case "v6" -> data.getV6();
      case "v7" -> data.getV7();
      case "rh" -> data.getRh();
      case "tx" -> data.getTx();
      case "echo" -> data.getEcho();
      case "rainD" -> data.getRainD();
      case "speed" -> data.getSpeed();
      default -> null;
    };
  }
}
