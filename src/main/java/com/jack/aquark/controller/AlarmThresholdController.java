package com.jack.aquark.controller;

import com.jack.aquark.constant.MessagesConstants;
import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.exception.ThresholdNotFoundException;
import com.jack.aquark.response.ApiResponseDto;
import com.jack.aquark.response.ErrorResponseDto;
import com.jack.aquark.response.ResponseDto;
import com.jack.aquark.service.AlarmThresholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Alarm Threshold API",
    description = "APIs for retrieving and updating sensor alarm threshold configurations.")
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AlarmThresholdController {

  private final AlarmThresholdService alarmThresholdService;

  @Operation(
      summary = "Retrieve Alarm Threshold(s)",
      description =
          "Retrieve the alarm threshold configuration data. If stationId, CSQ, and sensor parameter "
              + "are provided, the response is filtered to return only the matching threshold. If any "
              + "parameter is missing, all alarm thresholds are returned.",
      responses = {
        @ApiResponse(
            responseCode = MessagesConstants.STATUS_200,
            description = "Threshold(s) retrieved successfully",
            content =
                @Content(
                    array = @ArraySchema(schema = @Schema(implementation = AlarmThreshold.class)))),
        @ApiResponse(
            responseCode = MessagesConstants.STATUS_404,
            description = "No threshold data found for the given parameters",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
      })
  @GetMapping("/get")
  public ResponseEntity<ApiResponseDto<List<AlarmThreshold>>> getThresholds(
      @Parameter(example = "240627", description = "Station ID") @RequestParam(required = false)
          String stationId,
      @Parameter(example = "31", description = "CSQ value") @RequestParam(required = false)
          String csq,
      @Parameter(example = "v1", description = "Sensor parameter (e.g. 'v1', 'rh', etc.)")
          @RequestParam(required = false)
          String parameter) {

    try {
      // If all three parameters are provided, retrieve one threshold
      if (stationId != null && csq != null && parameter != null) {
        AlarmThreshold threshold = alarmThresholdService.getThreshold(stationId, csq, parameter);
        // Defensive check in case the service unexpectedly returns null
        if (threshold == null) {
          throw new ThresholdNotFoundException(
              "Threshold not found for station "
                  + stationId
                  + ", csq "
                  + csq
                  + ", parameter "
                  + parameter);
        }
        return ResponseEntity.ok(ApiResponseDto.success(List.of(threshold)));
      } else {
        // Otherwise, return all thresholds
        List<AlarmThreshold> thresholds = alarmThresholdService.getAllThresholds();
        if (thresholds == null || thresholds.isEmpty()) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(
                  ApiResponseDto.error(
                      new ErrorResponseDto(
                          "/api/alarm/get",
                          MessagesConstants.STATUS_404,
                          "Threshold not found.",
                          LocalDateTime.now())));
        }
        return ResponseEntity.ok(ApiResponseDto.success(thresholds));
      }
    } catch (ThresholdNotFoundException e) {
      log.error("Threshold not found", e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponseDto.error(
                  new ErrorResponseDto(
                      "/api/alarm/get",
                      MessagesConstants.STATUS_404,
                      "Threshold not found.",
                      LocalDateTime.now())));
    } catch (Exception e) {
      log.error("Unexpected error retrieving threshold", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponseDto.error(
                  new ErrorResponseDto(
                      "/api/alarm/get",
                      MessagesConstants.STATUS_500,
                      "Internal server error.",
                      LocalDateTime.now())));
    }
  }

  @Operation(
      summary = "Update Alarm Threshold",
      description =
          "Creates or updates the alarm threshold configuration for a specified station, CSQ, and sensor parameter.",
      responses = {
        @ApiResponse(
            responseCode = MessagesConstants.STATUS_200,
            description = "Threshold updated successfully",
            content = @Content(schema = @Schema(implementation = AlarmThreshold.class))),
        @ApiResponse(
            responseCode = MessagesConstants.STATUS_409,
            description = "Update operation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
      })
  @PostMapping("/update")
  public ResponseEntity<ApiResponseDto<ResponseDto>> updateThreshold(
      @Valid @RequestBody AlarmThreshold threshold) {

    boolean isUpdated = alarmThresholdService.updateThreshold(threshold);
    if (isUpdated) {
      return ResponseEntity.status(HttpStatus.OK)
          .body(
              ApiResponseDto.success(
                  new ResponseDto(
                      MessagesConstants.STATUS_200, "Request processed successfully.")));
    } else {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              ApiResponseDto.error(
                  new ErrorResponseDto(
                      "/api/alarm/update",
                      MessagesConstants.STATUS_409,
                      "Could not update threshold. Possibly a conflict or missing data.",
                      LocalDateTime.now())));
    }
  }

  @Operation(
      summary = "Add a New Alarm Threshold",
      description =
          "Adds a new alarm threshold configuration if it doesn't already exist. Returns 409 if duplicate.",
      responses = {
        @ApiResponse(
            responseCode = MessagesConstants.STATUS_201,
            description = "Threshold created successfully",
            content = @Content(schema = @Schema(implementation = AlarmThreshold.class))),
        @ApiResponse(
            responseCode = MessagesConstants.STATUS_409,
            description = "Threshold already exists for the given station, CSQ, and parameter",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
      })
  @PostMapping("/add")
  public ResponseEntity<ApiResponseDto<AlarmThreshold>> addThreshold(
      @Valid @RequestBody AlarmThreshold threshold) {

    boolean exists =
        alarmThresholdService.exists(
            threshold.getStationId(), threshold.getCsq(), threshold.getParameter());
    if (exists) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              ApiResponseDto.error(
                  new ErrorResponseDto(
                      "/api/alarm/add",
                      MessagesConstants.STATUS_409,
                      "Threshold already exists for the given station, CSQ, and sensor parameter.",
                      LocalDateTime.now())));
    }

    AlarmThreshold created = alarmThresholdService.saveNewThreshold(threshold);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(created));
  }
}
