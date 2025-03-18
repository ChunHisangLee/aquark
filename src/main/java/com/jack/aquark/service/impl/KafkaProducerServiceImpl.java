package com.jack.aquark.service.impl;

import com.jack.aquark.dto.AlarmEvent;
import com.jack.aquark.service.KafkaProducerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {
  private static final String TOPIC = "alarm-events";
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void sendAlarmEvent(AlarmEvent event) {
    log.info("Sending alarm event: {}", event);
    kafkaTemplate.send(TOPIC, event.getStationId(), event);
  }
}
