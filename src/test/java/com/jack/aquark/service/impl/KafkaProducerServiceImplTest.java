package com.jack.aquark.service.impl;

import static org.mockito.Mockito.*;

import com.jack.aquark.dto.AlarmEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaProducerServiceImplTest {

  private KafkaTemplate<String, Object> kafkaTemplate;
  private KafkaProducerServiceImpl kafkaProducerService;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setUp() {
    // Create a mock of KafkaTemplate with correct generic type
    kafkaTemplate = (KafkaTemplate<String, Object>) mock(KafkaTemplate.class);
    // Initialize the service with the mocked KafkaTemplate
    kafkaProducerService = new KafkaProducerServiceImpl(kafkaTemplate);
  }

  @Test
  void testSendAlarmEvent() {
    // Create a sample AlarmEvent instance and set properties as needed
    AlarmEvent event = new AlarmEvent();
    event.setStationId("station1");
    // Optionally set other properties of the event if required for your test

    // Call the method under test
    kafkaProducerService.sendAlarmEvent(event);

    // Verify that kafkaTemplate.send was called exactly once with the correct topic, key, and event
    verify(kafkaTemplate, times(1)).send("alarm-events", "station1", event);
  }
}
