package com.jack.aquark.service;

import com.jack.aquark.entity.AlarmThreshold;
import java.util.List;

public interface AlarmThresholdService {
  AlarmThreshold getThreshold(String stationId, String csq, String parameter);

  AlarmThreshold updateThreshold(AlarmThreshold threshold);

  boolean exists(String stationId, String csq, String parameter);

  AlarmThreshold saveNewThreshold(AlarmThreshold threshold);

  List<AlarmThreshold> getAllThresholds();
}
