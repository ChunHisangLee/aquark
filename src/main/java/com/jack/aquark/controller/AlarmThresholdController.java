package com.jack.aquark.controller;

import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.service.AlarmThresholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Alarm Threshold API", description = "REST APIs for retrieving and updating sensor alarm thresholds")
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmThresholdController {

  private final AlarmThresholdService alarmThresholdService;

  @Operation(
          summary = "Get Alarm Threshold",
          description = "Retrieves the alarm threshold for a given sensor type.")
  @ApiResponses({
          @ApiResponse(
                  responseCode = "200",
                  description = "Threshold retrieved successfully",
                  content = @Content(schema = @Schema(implementation = AlarmThreshold.class))),
          @ApiResponse(
                  responseCode = "404",
                  description = "Threshold not found for the given sensor type",
                  content = @Content(schema = @Schema(implementation = String.class)))
  })
  @GetMapping("/{sensorType}")
  public ResponseEntity<AlarmThreshold> getThreshold(
          @Parameter(
                  example = "v1",
                  description = "The sensor type (e.g., 'v1', 'rh', etc.) for which to retrieve the threshold")
          @PathVariable String sensorName) {
    AlarmThreshold threshold = alarmThresholdService.getThreshold(sensorName);
    return ResponseEntity.ok(threshold);
  }

  @Operation(
          summary = "Update Alarm Threshold",
          description = "Creates or updates the alarm threshold for a given sensor type.")
  @ApiResponses({
          @ApiResponse(
                  responseCode = "200",
                  description = "Threshold updated successfully",
                  content = @Content(schema = @Schema(implementation = AlarmThreshold.class))),
          @ApiResponse(
                  responseCode = "500",
                  description = "Internal Server Error",
                  content = @Content(schema = @Schema(implementation = String.class)))
  })
  @PostMapping("/update")
  public ResponseEntity<AlarmThreshold> updateThreshold(
          @Parameter(
                  description = "Alarm threshold object containing the sensor type and its threshold value",
                  required = true,
                  schema = @Schema(implementation = AlarmThreshold.class))
          @RequestBody AlarmThreshold threshold) {
    AlarmThreshold updatedThreshold = alarmThresholdService.updateThreshold(threshold);
    return ResponseEntity.status(HttpStatus.OK).body(updatedThreshold);
  }
}
