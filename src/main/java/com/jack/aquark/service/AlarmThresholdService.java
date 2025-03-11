package com.jack.aquark.service;

import com.jack.aquark.entity.AlarmThreshold;

public interface AlarmThresholdService {
  AlarmThreshold getThreshold(String sensorType);
  AlarmThreshold updateThreshold(AlarmThreshold threshold);
}
