package com.jack.aquark.service.impl;

import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.exception.ThresholdNotFoundException;
import com.jack.aquark.repository.AlarmThresholdRepository;
import com.jack.aquark.service.AlarmThresholdService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AlarmThresholdServiceImpl implements AlarmThresholdService {
  private final AlarmThresholdRepository alarmThresholdRepository;

  @Override
  @Cacheable(value = "thresholds", key = "#stationId + '_' + #csq + '_' + #parameter")
  public AlarmThreshold getThreshold(String stationId, String csq, String parameter) {
    return alarmThresholdRepository
        .findByStationIdAndCsqAndParameter(stationId, csq, parameter)
        .orElseThrow(
            () ->
                new ThresholdNotFoundException(
                    "Threshold not found for station "
                        + stationId
                        + ", csq "
                        + csq
                        + ", parameter "
                        + parameter));
  }

  @Override
  @CacheEvict(
      value = "thresholds",
      key = "#threshold.stationId + '_' + #threshold.csq + '_' + #threshold.parameter")
  public boolean updateThreshold(AlarmThreshold threshold) {
    Optional<AlarmThreshold> existingOpt =
        alarmThresholdRepository.findByStationIdAndCsqAndParameter(
            threshold.getStationId(), threshold.getCsq(), threshold.getParameter());

    if (existingOpt.isEmpty()) {
      throw new ThresholdNotFoundException(
          "Threshold not found for station "
              + threshold.getStationId()
              + ", csq "
              + threshold.getCsq()
              + ", parameter "
              + threshold.getParameter());
    }

    AlarmThreshold existing = existingOpt.get();
    existing.setThresholdValue(threshold.getThresholdValue());
    AlarmThreshold saved = alarmThresholdRepository.save(existing);
    log.info(
        "Threshold updated for station {} parameter {} csq {}: {}",
        saved.getStationId(),
        saved.getParameter(),
        saved.getCsq(),
        saved.getThresholdValue().toPlainString());
    return true;
  }

  @Override
  public boolean exists(String stationId, String csq, String parameter) {
    return alarmThresholdRepository
        .findByStationIdAndCsqAndParameter(stationId, csq, parameter)
        .isPresent();
  }

  @Override
  @CachePut(
      value = "thresholds",
      key = "#result.stationId + '_' + #result.csq + '_' + #result.parameter")
  public AlarmThreshold saveNewThreshold(AlarmThreshold threshold) {
    return alarmThresholdRepository.save(threshold);
  }

  @Override
  public List<AlarmThreshold> getAllThresholds() {
    return alarmThresholdRepository.findAll();
  }
}
