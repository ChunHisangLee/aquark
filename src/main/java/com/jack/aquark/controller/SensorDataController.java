package com.jack.aquark.controller;

import com.jack.aquark.constant.MessagesConstants;
import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.response.ApiResponseDto;
import com.jack.aquark.response.ErrorResponseDto;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Sensor Data API",
    description = "APIs for fetching, uploading, and querying sensor data records and statistics.")
@RestController
@RequestMapping("/api/sensor")
@AllArgsConstructor
@Slf4j
@Validated
public class SensorDataController {

  private final SensorDataService sensorDataService;
  private final AggregationService aggregationService;

  @Operation(
      summary = "Search Sensor Data by Hour",
      description =
          "Retrieve sensor data records within the specified hour range. Date-time format: yyyy-MM-dd HH:mm:ss.")
  @ApiResponses({
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_200,
        description = "Sensor data retrieved successfully",
        content = @Content(schema = @Schema(implementation = SensorData.class))),
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_500,
        description = "Internal Server Error",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/search")
  public ResponseEntity<ApiResponseDto<List<SensorData>>> searchSensorData(
      @Parameter(
              example = "2025-03-11 15:00:00",
              description = "Start date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          LocalDateTime start,
      @Parameter(
              example = "2025-03-11 23:00:00",
              description = "End date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          LocalDateTime end) {
    try {
      List<SensorData> data = aggregationService.getSensorDataByTimeRange(start, end);
      return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success(data));
    } catch (Exception e) {
      log.error("Error searching sensor data for range {} - {}", start, end, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponseDto.error(
                  new ErrorResponseDto(
                      "/api/sensor/search",
                      HttpStatus.INTERNAL_SERVER_ERROR,
                      "Error searching sensor data",
                      LocalDateTime.now())));
    }
  }

  @Operation(
      summary = "Get Hourly Statistics",
      description =
          "Retrieve aggregated hourly sensor data statistics for the specified date range. Date-time format: yyyy-MM-dd HH:mm:ss.")
  @ApiResponses({
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_200,
        description = "Hourly statistics retrieved successfully",
        content = @Content(schema = @Schema(implementation = HourlyAggregation.class))),
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_500,
        description = "Internal Server Error",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/statistics/hourly")
  public ResponseEntity<ApiResponseDto<List<HourlyAggregation>>> getHourlyStats(
      @Parameter(
              example = "2025-03-11 15:00:00",
              description = "Start date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          LocalDateTime start,
      @Parameter(
              example = "2025-03-11 23:00:00",
              description = "End date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          LocalDateTime end) {
    try {
      List<HourlyAggregation> stats = sensorDataService.getHourlyAverage(start, end);
      return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success(stats));
    } catch (Exception e) {
      log.error("Error fetching hourly statistics for range {} - {}", start, end, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponseDto.error(
                  new ErrorResponseDto(
                      "/api/sensor/statistics/hourly",
                      HttpStatus.INTERNAL_SERVER_ERROR,
                      "Error fetching hourly statistics",
                      LocalDateTime.now())));
    }
  }

  @Operation(
      summary = "Get Daily Statistics",
      description =
          "Retrieve aggregated daily sensor data statistics for the specified date range.")
  @ApiResponses({
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_200,
        description = "Daily statistics retrieved successfully",
        content = @Content(schema = @Schema(implementation = DailyAggregation.class))),
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_500,
        description = "Internal Server Error",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/statistics/daily")
  public ResponseEntity<ApiResponseDto<List<DailyAggregation>>> getDailyStats(
      @Parameter(
              example = "2025-03-11 00:00:00",
              description = "Start date/time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          LocalDateTime start,
      @Parameter(
              example = "2025-03-14 00:00:00",
              description = "End date/time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          LocalDateTime end) {
    try {
      List<DailyAggregation> stats = sensorDataService.getDailyAverage(start, end);
      return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success(stats));
    } catch (Exception e) {
      log.error("Error fetching daily statistics for range {} - {}", start, end, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponseDto.error(
                  new ErrorResponseDto(
                      "/api/sensor/statistics/daily",
                      HttpStatus.INTERNAL_SERVER_ERROR,
                      "Error fetching daily statistics",
                      LocalDateTime.now())));
    }
  }

  @Operation(
      summary = "Get Peak-Time Data",
      description =
          "Retrieve sensor data for the specified period that falls within peak hours (defined as 07:30 to 17:30 on weekdays, all day on Thursdays and Fridays, and off on weekends). Date-time format: yyyy-MM-dd HH:mm:ss.")
  @ApiResponses({
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_200,
        description = "Peak data statistics retrieved successfully",
        content = @Content(schema = @Schema(implementation = SensorData.class))),
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_500,
        description = "Internal Server Error",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/peak")
  public ResponseEntity<ApiResponseDto<List<SensorData>>> getPeakData(
      @Parameter(
              example = "2025-03-11 15:00:00",
              description = "Start date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          LocalDateTime start,
      @Parameter(
              example = "2025-03-11 23:00:00",
              description = "End date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          LocalDateTime end) {
    try {
      List<SensorData> result = sensorDataService.getPeakTimeData(start, end);
      return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success(result));
    } catch (Exception e) {
      log.error("Error fetching peak time data for range {} - {}", start, end, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponseDto.error(
                  new ErrorResponseDto(
                      "/api/sensor/peak",
                      HttpStatus.INTERNAL_SERVER_ERROR,
                      "Error fetching peak time data",
                      LocalDateTime.now())));
    }
  }

  @Operation(
      summary = "Get Off-Peak Data",
      description =
          "Retrieve sensor data for the specified period that falls outside peak hours. Date-time format: yyyy-MM-dd HH:mm:ss.")
  @ApiResponses({
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_200,
        description = "Off-Peak data statistics retrieved successfully",
        content = @Content(schema = @Schema(implementation = SensorData.class))),
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_500,
        description = "Internal Server Error",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/off-peak")
  public ResponseEntity<ApiResponseDto<List<SensorData>>> getOffPeakData(
      @Parameter(
              example = "2025-03-11 15:00:00",
              description = "Start date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          LocalDateTime start,
      @Parameter(
              example = "2025-03-11 23:00:00",
              description = "End date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          LocalDateTime end) {
    try {
      List<SensorData> result = sensorDataService.getOffPeakTimeData(start, end);
      return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success(result));
    } catch (Exception e) {
      log.error("Error fetching off-peak data for range {} - {}", start, end, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponseDto.error(
                  new ErrorResponseDto(
                      "/api/sensor/off-peak",
                      HttpStatus.INTERNAL_SERVER_ERROR,
                      "Error fetching off-peak data",
                      LocalDateTime.now())));
    }
  }
}
