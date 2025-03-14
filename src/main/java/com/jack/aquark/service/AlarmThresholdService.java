package com.jack.aquark.service;

import com.jack.aquark.entity.AlarmThreshold;

public interface AlarmThresholdService {
  AlarmThreshold getThreshold(String stationId, String csq, String parameter);

  boolean updateThreshold(AlarmThreshold threshold);
}
