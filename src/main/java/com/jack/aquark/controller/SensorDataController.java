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
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Sensor Data API",
    description = "APIs for fetching, uploading, and querying sensor data records and statistics.")
@RestController
@RequestMapping("/api/sensor")
@AllArgsConstructor
@Validated
public class SensorDataController extends BaseController {

  private final SensorDataService sensorDataService;
  private final AggregationService aggregationService;

  @Operation(
      summary = "Search Sensor Data by Hour",
      description =
          "Retrieve sensor data records within the specified hour range. Date-time format: yyyy-MM-dd HH:mm:ss.",
      responses = {
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

    List<SensorData> data = aggregationService.getSensorDataByTimeRange(start, end);
    return respondOK(data);
  }

  @Operation(
      summary = "Get Hourly Statistics",
      description =
          "Retrieve aggregated hourly sensor data statistics for the specified date range. "
              + "Date-time format: yyyy-MM-dd HH:mm:ss.",
      responses = {
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

    List<HourlyAggregation> stats = sensorDataService.getHourlyAverage(start, end);
    return respondOK(stats);
  }

  @Operation(
      summary = "Get Daily Statistics",
      description =
          "Retrieve aggregated daily sensor data statistics for the specified date range.",
      responses = {
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

    List<DailyAggregation> stats = sensorDataService.getDailyAverage(start, end);
    return respondOK(stats);
  }

  @Operation(
      summary = "Get Peak-Time Data",
      description =
          "Retrieve sensor data for the specified period that falls within peak hours "
              + "(defined as 07:30 to 17:30 on weekdays, all day on Thursdays and Fridays, "
              + "and off on weekends). Date-time format: yyyy-MM-dd HH:mm:ss.",
      responses = {
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

    List<SensorData> result = sensorDataService.getPeakTimeData(start, end);
    return respondOK(result);
  }

  @Operation(
      summary = "Get Off-Peak Data",
      description =
          "Retrieve sensor data for the specified period that falls outside peak hours. "
              + "Date-time format: yyyy-MM-dd HH:mm:ss.",
      responses = {
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

    List<SensorData> result = sensorDataService.getOffPeakTimeData(start, end);
    return respondOK(result);
  }
}
