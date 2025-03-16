package com.jack.aquark.service;

import com.jack.aquark.dto.AlarmEvent;

public interface KafkaProducerService {
  void sendAlarmEvent(AlarmEvent event);
}
