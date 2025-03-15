package com.jack.aquark.controller;

import com.jack.aquark.constant.MessagesConstants;
import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.response.ResponseDto;
import com.jack.aquark.service.AlarmThresholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Alarm Threshold API",
    description = "REST APIs for retrieving and updating sensor alarm thresholds")
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AlarmThresholdController {

  private final AlarmThresholdService alarmThresholdService;

  @Operation(
      summary = "Get Alarm Threshold",
      description = "Retrieves the alarm threshold for a given station, csq, and sensor parameter.")
  @ApiResponses({
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_200,
        description = "Threshold retrieved successfully",
        content = @Content(schema = @Schema(implementation = AlarmThreshold.class))),
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_404,
        description = "Threshold not found for the given combination",
        content = @Content(schema = @Schema(implementation = String.class)))
  })
  @GetMapping
  public ResponseEntity<AlarmThreshold> getThreshold(
      @Parameter(example = "240627", description = "The station ID") @RequestParam String stationId,
      @Parameter(example = "31", description = "The CSQ value") @RequestParam String csq,
      @Parameter(example = "v1", description = "The sensor parameter (e.g., 'v1', 'rh', etc.)")
          @RequestParam
          String parameter) {
    AlarmThreshold threshold = alarmThresholdService.getThreshold(stationId, csq, parameter);
    if (threshold == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return ResponseEntity.ok(threshold);
  }

  @Operation(
      summary = "Update Alarm Threshold",
      description =
          "Creates or updates the alarm threshold for a given station, csq, and sensor parameter.")
  @ApiResponses({
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_200,
        description = "Threshold updated successfully",
        content = @Content(schema = @Schema(implementation = AlarmThreshold.class))),
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_417,
        description = "Internal Server Error",
        content = @Content(schema = @Schema(implementation = String.class)))
  })
  @PostMapping("/update")
  public ResponseEntity<ResponseDto> updateThreshold(@Valid @RequestBody AlarmThreshold threshold) {

    boolean isUpdated = alarmThresholdService.updateThreshold(threshold);

    if (isUpdated) {
      log.info("Alarm threshold updated successfully.");
      return ResponseEntity.status(HttpStatus.OK)
          .body(new ResponseDto(MessagesConstants.STATUS_200, "Request processed successfully."));
    } else {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
          .body(
              new ResponseDto(
                  MessagesConstants.STATUS_417,
                  "Update operation failed. Please try again or contact Dev team."));
    }
  }
}
