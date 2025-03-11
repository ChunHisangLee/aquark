package com.jack.aquark.controller;

import com.jack.aquark.dto.RawDataWrapper;
import com.jack.aquark.dto.Summaries;
import com.jack.aquark.service.SensorDataService;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "CRUD REST APIs for User",
        description = "CRUD REST APIs to CREATE, UPDATE, FETCH AND DELETE")
@RestController
@RequestMapping("/api/sensor")
public class SensorDataController {

  private final SensorDataService sensorDataService;

  public SensorDataController(SensorDataService sensorDataService) {
    this.sensorDataService = sensorDataService;
  }

  /** 1. Receive entire JSON with "raw" array */
  @PostMapping("/upload")
  public String uploadRawData(@RequestBody RawDataWrapper wrapper) {
    sensorDataService.saveRawData(wrapper);
    return "Uploaded & saved successfully";
  }

  /**
   * 2. Summaries (sum & average) in memory e.g.
   * /api/sensor/summaries?start=2025-01-10T00:00:00&end=2025-01-10T23:59:59
   */
  @GetMapping("/summaries")
  public Summaries getSummaries(
      @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime start,
      @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
    return sensorDataService.getSummaries(start, end);
  }
}
