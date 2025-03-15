package com.jack.aquark.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.exception.ThresholdNotFoundException;
import com.jack.aquark.repository.AlarmThresholdRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlarmThresholdServiceImplTest {

  @Mock private AlarmThresholdRepository alarmThresholdRepository;

  @InjectMocks private AlarmThresholdServiceImpl alarmThresholdService;

  private AlarmThreshold threshold;

  @BeforeEach
  void setUp() {
    threshold = new AlarmThreshold();
    threshold.setStationId("stationX");
    threshold.setCsq("31");
    threshold.setParameter("v1");
    threshold.setThresholdValue(new BigDecimal("100.0"));
  }

  @Test
  void testGetThreshold_Found() {
    // Mock the repository to return the threshold
    when(alarmThresholdRepository.findByStationIdAndCsqAndParameter("stationX", "31", "v1"))
        .thenReturn(Optional.of(threshold));

    AlarmThreshold result = alarmThresholdService.getThreshold("stationX", "31", "v1");
    assertNotNull(result);
    assertEquals("stationX", result.getStationId());
  }

  @Test
  void testGetThreshold_NotFound() {
    when(alarmThresholdRepository.findByStationIdAndCsqAndParameter("stationX", "31", "v1"))
        .thenReturn(Optional.empty());

    assertThrows(
        ThresholdNotFoundException.class,
        () -> alarmThresholdService.getThreshold("stationX", "31", "v1"));
  }

  @Test
  void testUpdateThreshold() {
    // 1) Mock the repository to return the existing threshold
    when(alarmThresholdRepository.findByStationIdAndCsqAndParameter("stationX", "31", "v1"))
        .thenReturn(Optional.of(threshold));
    // 2) Also mock the save(...) method if needed
    when(alarmThresholdRepository.save(any(AlarmThreshold.class))).thenReturn(threshold);

    // 3) Create an updated threshold
    AlarmThreshold updatedThreshold = new AlarmThreshold();
    updatedThreshold.setStationId("stationX");
    updatedThreshold.setCsq("31");
    updatedThreshold.setParameter("v1");
    updatedThreshold.setThresholdValue(new BigDecimal("200.0"));

    boolean result = alarmThresholdService.updateThreshold(updatedThreshold);
    assertTrue(result);

    // Verify that the threshold's value was updated
    verify(alarmThresholdRepository).findByStationIdAndCsqAndParameter("stationX", "31", "v1");
    verify(alarmThresholdRepository).save(any(AlarmThreshold.class));
  }
}
