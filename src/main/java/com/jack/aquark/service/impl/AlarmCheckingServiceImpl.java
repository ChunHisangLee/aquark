package com.jack.aquark.service.impl;

import com.jack.aquark.dto.AlarmCheckResult;
import com.jack.aquark.dto.AlarmDetail;
import com.jack.aquark.dto.AlarmEvent;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.exception.ThresholdNotFoundException;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.AlarmCheckingService;
import com.jack.aquark.service.AlarmThresholdService;
import com.jack.aquark.service.KafkaProducerService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AlarmCheckingServiceImpl implements AlarmCheckingService {

  private static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");

  private final AggregationService aggregationService;
  private final AlarmThresholdService alarmThresholdService;
  private final KafkaProducerService kafkaProducerService;

  @Override
  public AlarmCheckResult checkSensorAlarms(int intervalMinutes) {
    log.info("Checking sensor alarms for the last {} minutes...", intervalMinutes);

    // Get current time in Taipei time zone
    ZonedDateTime nowZoned = ZonedDateTime.now(TAIPEI_ZONE);
    ZonedDateTime startZoned = nowZoned.minusMinutes(intervalMinutes);

    // Convert to LocalDateTime for repository usage
    LocalDateTime now = nowZoned.toLocalDateTime();
    LocalDateTime startTime = startZoned.toLocalDateTime();

    log.info("StartTime: {}, EndTime: {}", startTime, now);

    // Fetch sensor data from your aggregator or raw data
    List<SensorData> sensorDataList = aggregationService.getSensorDataByTimeRange(startTime, now);
    List<AlarmDetail> alarmDetails = new ArrayList<>();

    // Check all relevant parameters
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
      if (value == null) {
        // No sensor reading for this parameter, skip
        continue;
      }

      try {
        // Try to fetch threshold
        var threshold =
            alarmThresholdService.getThreshold(data.getStationId(), data.getCsq(), parameter);

        // If the sensor value exceeds the threshold, create an alarm
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

          // Add an AlarmDetail to the result
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

          // Send Kafka event
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

      } catch (ThresholdNotFoundException ex) {
        // Specifically handle missing threshold
        log.debug(
            "No threshold found for station={}, parameter={}, csq={}. Skipping alarm check.",
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
