package com.jack.aquark.controller;

import com.jack.aquark.dto.RawDataWrapper;
import com.jack.aquark.dto.Summaries;
import com.jack.aquark.entity.SensorData;
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

@Tag(name = "Sensor Data API", description = "REST APIs for fetching, uploading, and querying sensor data")
@RestController
@RequestMapping("/api/sensor")
@AllArgsConstructor
@Slf4j
public class SensorDataController {

  private final SensorDataService sensorDataService;

  @Operation(
          summary = "Fetch and Upload Raw Data",
          description = "Fetches raw sensor data from an external API using the given stationId, saves it to the database, and returns a confirmation message.")
  @ApiResponses({
          @ApiResponse(
                  responseCode = "200",
                  description = "Fetched and saved successfully for the given stationId"),
          @ApiResponse(
                  responseCode = "500",
                  description = "Internal Server Error",
                  content = @Content(schema = @Schema(implementation = String.class)))
  })
  @GetMapping("/fetchAndUpload")
  public ResponseEntity<String> fetchAndUploadRawData(@RequestParam String stationId) {
    try {
      // Compose URL dynamically using the provided stationId
      String url = "https://app.aquark.com.tw/api/raw/Angle2024/" + stationId;
      RawDataWrapper wrapper = sensorDataService.fetchRawDataFromUrl(url);
      sensorDataService.saveRawData(wrapper);
      return ResponseEntity.ok("Fetched and saved successfully for station: " + stationId);
    } catch (Exception e) {
      log.error("Error fetching and uploading raw data for station: {}", stationId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Error fetching and uploading raw data");
    }
  }

  @Operation(
          summary = "Search Sensor Data",
          description = "Search sensor data records within a specified time range.")
  @ApiResponses({
          @ApiResponse(
                  responseCode = "200",
                  description = "Sensor data retrieved successfully",
                  content = @Content(schema = @Schema(implementation = SensorData.class))),
          @ApiResponse(
                  responseCode = "400",
                  description = "Bad Request: Incorrect date format",
                  content = @Content(schema = @Schema(implementation = String.class)))
  })
  @GetMapping("/search")
  public ResponseEntity<List<SensorData>> searchSensorData(
          @Parameter(
                  example = "2025-03-11 15:48:59",
                  description = "Start date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
          @Parameter(
                  example = "2025-03-11 23:59:59",
                  description = "End date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
    List<SensorData> data = sensorDataService.getSensorDataBetween(start, end);
    return ResponseEntity.ok(data);
  }

  @Operation(
          summary = "Get Hourly Statistics",
          description = "Retrieves hourly average sensor data (for example, for v1) for a specified time range.")
  @ApiResponses({
          @ApiResponse(
                  responseCode = "200",
                  description = "Hourly averages retrieved successfully",
                  content = @Content(schema = @Schema(implementation = Object[].class))),
          @ApiResponse(
                  responseCode = "400",
                  description = "Bad Request: Incorrect date format",
                  content = @Content(schema = @Schema(implementation = String.class)))
  })
  @GetMapping("/statistics/hourly")
  public ResponseEntity<List<Object[]>> getHourlyStats(
          @Parameter(
                  example = "2025-03-11 15:48:59",
                  description = "Start date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
          @Parameter(
                  example = "2025-03-11 23:59:59",
                  description = "End date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
    List<Object[]> stats = sensorDataService.getHourlyAverage(start, end);
    return ResponseEntity.ok(stats);
  }

  @Operation(
          summary = "Get Summaries",
          description = "Calculates the sum and average of sensor data within a specified time range. This includes totals for voltage fields, RH, TX, echo, speed, and rain.")
  @ApiResponses({
          @ApiResponse(
                  responseCode = "200",
                  description = "Summaries calculated successfully",
                  content = @Content(schema = @Schema(implementation = Summaries.class))),
          @ApiResponse(
                  responseCode = "400",
                  description = "Bad Request: Incorrect date format",
                  content = @Content(schema = @Schema(implementation = String.class)))
  })
  @GetMapping("/summaries")
  public ResponseEntity<Summaries> getSummaries(
          @Parameter(
                  example = "2025-03-11 15:48:59",
                  description = "Start date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
          @Parameter(
                  example = "2025-03-11 23:59:59",
                  description = "End date and time in the format yyyy-MM-dd HH:mm:ss")
          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
    Summaries summaries = sensorDataService.getSummaries(start, end);
    return ResponseEntity.ok(summaries);
  }
}
