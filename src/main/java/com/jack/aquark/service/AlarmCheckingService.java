package com.jack.aquark.service;

import com.jack.aquark.dto.AlarmCheckResult;

public interface AlarmCheckingService {
  AlarmCheckResult checkSensorAlarms(int intervalMinutes);
}
