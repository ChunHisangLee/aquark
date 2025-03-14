package com.jack.aquark.service.impl;

import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.repository.AlarmThresholdRepository;
import com.jack.aquark.service.AlarmThresholdService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AlarmThresholdServiceImpl implements AlarmThresholdService {
  private final AlarmThresholdRepository alarmThresholdRepository;

  @Override
  public AlarmThreshold getThreshold(String stationId, String csq, String parameter) {
    return alarmThresholdRepository
        .findByStationIdAndCsqAndParameter(stationId, csq, parameter)
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Threshold not found for station "
                        + stationId
                        + ", csq "
                        + csq
                        + ", parameter "
                        + parameter));
  }

  @Override
  public boolean updateThreshold(AlarmThreshold threshold) {
    AlarmThreshold saved = alarmThresholdRepository.save(threshold);
    log.info(
        "Threshold updated for station {} parameter {} csq {}: {}",
        saved.getStationId(),
        saved.getParameter(),
        saved.getCsq(),
        saved.getThresholdValue().toPlainString());
    return true;
  }
}
