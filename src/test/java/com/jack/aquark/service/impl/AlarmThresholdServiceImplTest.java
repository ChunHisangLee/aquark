package com.jack.aquark.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.exception.ThresholdNotFoundException;
import com.jack.aquark.repository.AlarmThresholdRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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
    threshold.setStationId("240709");
    threshold.setCsq("31");
    threshold.setParameter("v1");
    threshold.setThresholdValue(new BigDecimal("100.0"));
  }

  @Test
  void testGetThreshold_Found() {
    // Given the repository returns the threshold
    when(alarmThresholdRepository.findByStationIdAndCsqAndParameter("240709", "31", "v1"))
        .thenReturn(Optional.of(threshold));

    AlarmThreshold result = alarmThresholdService.getThreshold("240709", "31", "v1");
    assertNotNull(result, "Expected a non-null threshold");
    assertEquals("240709", result.getStationId());
    assertEquals(new BigDecimal("100.0"), result.getThresholdValue());
  }

  @Test
  void testGetThreshold_NotFound() {
    when(alarmThresholdRepository.findByStationIdAndCsqAndParameter("240709", "31", "v1"))
        .thenReturn(Optional.empty());

    assertThrows(
        ThresholdNotFoundException.class,
        () -> alarmThresholdService.getThreshold("240709", "31", "v1"),
        "Expected ThresholdNotFoundException when threshold is not found");
  }

  @Test
  void testUpdateThreshold() {
    // Given the repository returns the existing threshold
    when(alarmThresholdRepository.findByStationIdAndCsqAndParameter("240709", "31", "v1"))
        .thenReturn(Optional.of(threshold));
    // And when saving, return the updated threshold
    when(alarmThresholdRepository.save(any(AlarmThreshold.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Create an updated threshold object with a new threshold value
    AlarmThreshold updatedThreshold = new AlarmThreshold();
    updatedThreshold.setStationId("240709");
    updatedThreshold.setCsq("31");
    updatedThreshold.setParameter("v1");
    updatedThreshold.setThresholdValue(new BigDecimal("200.0"));

    boolean result = alarmThresholdService.updateThreshold(updatedThreshold);
    assertTrue(result, "Update should return true");
    // Verify that the update changes the threshold value to 200.0
    assertEquals(new BigDecimal("200.0"), threshold.getThresholdValue());

    verify(alarmThresholdRepository).findByStationIdAndCsqAndParameter("240709", "31", "v1");
    verify(alarmThresholdRepository).save(any(AlarmThreshold.class));
  }

  @Test
  void testExists() {
    // When repository returns a present Optional, exists() should return true.
    when(alarmThresholdRepository.findByStationIdAndCsqAndParameter("240709", "31", "v1"))
        .thenReturn(Optional.of(threshold));
    assertTrue(alarmThresholdService.exists("240709", "31", "v1"));

    // When repository returns an empty Optional, exists() should return false.
    when(alarmThresholdRepository.findByStationIdAndCsqAndParameter("240709", "31", "v1"))
        .thenReturn(Optional.empty());
    assertFalse(alarmThresholdService.exists("240709", "31", "v1"));
  }

  @Test
  void testSaveNewThreshold() {
    // When saving a new threshold, repository should return the saved object.
    when(alarmThresholdRepository.save(any(AlarmThreshold.class))).thenReturn(threshold);
    AlarmThreshold saved = alarmThresholdService.saveNewThreshold(threshold);
    assertNotNull(saved);
    verify(alarmThresholdRepository).save(threshold);
  }

  @Test
  void testGetAllThresholds() {
    List<AlarmThreshold> list = Arrays.asList(threshold, new AlarmThreshold());
    when(alarmThresholdRepository.findAll()).thenReturn(list);
    List<AlarmThreshold> result = alarmThresholdService.getAllThresholds();
    assertEquals(2, result.size());
    verify(alarmThresholdRepository).findAll();
  }
}
