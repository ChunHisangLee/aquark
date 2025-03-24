package com.jack.aquark.service;

import com.jack.aquark.dto.AlarmCheckResult;

public interface SensorAlarmService {
  AlarmCheckResult checkSensorAlarms(int intervalMinutes);
}
