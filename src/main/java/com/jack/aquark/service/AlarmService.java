package com.jack.aquark.service;

import com.jack.aquark.entity.SensorData;

public interface AlarmService {
  void checkAndTriggerAlarms(SensorData data);
}
