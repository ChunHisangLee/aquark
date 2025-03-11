package com.jack.aquark.controller;

import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.service.AlarmThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmThresholdController {

  private final AlarmThresholdService alarmThresholdService;

  // Get a threshold by sensor type
  @GetMapping("/{sensorType}")
  public AlarmThreshold getThreshold(@PathVariable String sensorType) {
    return alarmThresholdService.getThreshold(sensorType);
  }

  // Update (or create) a threshold for a sensor
  @PostMapping("/update")
  public AlarmThreshold updateThreshold(@RequestBody AlarmThreshold threshold) {
    return alarmThresholdService.updateThreshold(threshold);
  }
}
