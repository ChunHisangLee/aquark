package com.jack.aquark.controller;

import com.jack.aquark.constant.MessagesConstants;
import com.jack.aquark.dto.AlarmCheckResult;
import com.jack.aquark.response.ApiResponseDto;
import com.jack.aquark.response.ErrorResponseDto;
import com.jack.aquark.service.SensorAlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Alarm API",
    description = "APIs for checking sensor alarms and triggering notifications.")
@RestController
@RequestMapping("/api/alarm")
@AllArgsConstructor
public class SensorAlarmController extends BaseController {

  private final SensorAlarmService sensorAlarmService;

  @Operation(
      summary = "Check Sensor Alarms",
      description =
          "Checks sensor data over the specified interval (in minutes) and triggers alarms if sensor "
              + "readings exceed thresholds. Returns detailed alarm check results.",
      responses = {
        @ApiResponse(
            responseCode = MessagesConstants.STATUS_200,
            description = "Alarm check completed successfully",
            content = @Content(schema = @Schema(implementation = AlarmCheckResult.class))),
        @ApiResponse(
            responseCode = MessagesConstants.STATUS_500,
            description = "Internal Server Error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
      })
  @GetMapping("/check")
  public ResponseEntity<ApiResponseDto<AlarmCheckResult>> checkSensorAlarms(
      @RequestParam(name = "intervalMinutes", defaultValue = "60") int intervalMinutes) {

    AlarmCheckResult result = sensorAlarmService.checkSensorAlarms(intervalMinutes);
    return respondOK(result);
  }
}
