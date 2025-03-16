package com.jack.aquark.controller;

import com.jack.aquark.constant.MessagesConstants;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.response.ResponseDto;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.SensorDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Sensor Data API",
    description = "REST APIs for fetching, uploading, and querying sensor data")
@RestController
@RequestMapping("/api/sensor")
@AllArgsConstructor
@Slf4j
public class SensorDataController {

  private final SensorDataService sensorDataService;
  private final AggregationService aggregationService;

  @Operation(
      summary = "Search Sensor Data by Hour",
      description = "Search sensor data records for a given hour range (minutes are ignored).")
  @ApiResponses({
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_200,
        description = "Sensor data retrieved successfully",
        content = @Content(schema = @Schema(implementation = SensorData.class))),
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_500,
        description = "Internal Server Error",
        content = @Content(schema = @Schema(implementation = String.class)))
  })
  @GetMapping("/search")
  public ResponseEntity<?> searchSensorData(
      @Parameter(
              example = "2025-03-11 15",
              description = "Start date and hour in the format yyyy-MM-dd HH")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH")
          LocalDateTime start,
      @Parameter(
              example = "2025-03-11 23",
              description = "End date and hour in the format yyyy-MM-dd HH")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH")
          LocalDateTime end) {
    try {
      List<SensorData> data = aggregationService.getSensorDataByTimeRange(start, end);
      return ResponseEntity.ok(data);
    } catch (Exception e) {
      log.error("Error searching sensor data for range {} - {}", start, end, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ResponseDto(MessagesConstants.STATUS_500, "Error searching sensor data"));
    }
  }

  @Operation(
      summary = "Get Hourly Statistics",
      description = "Retrieves hourly average sensor data for a specified hour range.")
  @ApiResponses({
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_200,
        description = "Hourly averages retrieved successfully",
        content = @Content(schema = @Schema(implementation = Object[].class))),
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_500,
        description = "Internal Server Error",
        content = @Content(schema = @Schema(implementation = String.class)))
  })
  @GetMapping("/statistics/hourly")
  public ResponseEntity<?> getHourlyStats(
      @Parameter(
              example = "2025-03-11 15",
              description = "Start date and hour in the format yyyy-MM-dd HH")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH")
          LocalDateTime start,
      @Parameter(
              example = "2025-03-11 23",
              description = "End date and hour in the format yyyy-MM-dd HH")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH")
          LocalDateTime end) {
    try {
      List<HourlyAggregation> stats = sensorDataService.getHourlyAverage(start, end);
      return ResponseEntity.ok(stats);
    } catch (Exception e) {
      log.error("Error fetching hourly statistics for range {} - {}", start, end, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ResponseDto(MessagesConstants.STATUS_500, "Error fetching hourly statistics"));
    }
  }

  @Operation(summary = "Get peak-time data by hour in range")
  @GetMapping("/peak")
  public ResponseEntity<?> getPeakData(
      @Parameter(
              example = "2025-03-11 15",
              description = "Start date and hour in the format yyyy-MM-dd HH")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH")
          LocalDateTime start,
      @Parameter(
              example = "2025-03-11 23",
              description = "End date and hour in the format yyyy-MM-dd HH")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH")
          LocalDateTime end) {
    try {
      List<SensorData> result = sensorDataService.getPeakTimeData(start, end);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error fetching peak time data for range {} - {}", start, end, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ResponseDto(MessagesConstants.STATUS_500, "Error fetching peak time data"));
    }
  }

  @Operation(summary = "Get off-peak data by hour in range")
  @GetMapping("/off-peak")
  public ResponseEntity<?> getOffPeakData(
      @Parameter(
              example = "2025-03-11 15",
              description = "Start date and hour in the format yyyy-MM-dd HH")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH")
          LocalDateTime start,
      @Parameter(
              example = "2025-03-11 23",
              description = "End date and hour in the format yyyy-MM-dd HH")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH")
          LocalDateTime end) {
    try {
      List<SensorData> result = sensorDataService.getOffPeakTimeData(start, end);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error fetching off-peak data for range {} - {}", start, end, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ResponseDto(MessagesConstants.STATUS_500, "Error fetching off-peak data"));
    }
  }
}
