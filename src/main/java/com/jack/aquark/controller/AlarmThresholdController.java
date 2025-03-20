package com.jack.aquark.controller;

import com.jack.aquark.constant.MessagesConstants;
import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.response.ApiResponseDto;
import com.jack.aquark.response.ErrorResponseDto;
import com.jack.aquark.response.ResponseDto;
import com.jack.aquark.service.AlarmThresholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
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
          "Retrieve the alarm threshold configuration data. If stationId, CSQ, and sensor parameter are provided, "
              + "the response is filtered to return only the matching threshold. If any parameter is missing, "
              + "all alarm thresholds are returned.")
  @ApiResponses({
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

    List<AlarmThreshold> thresholds;

    if (stationId != null && csq != null && parameter != null) {
      AlarmThreshold threshold = alarmThresholdService.getThreshold(stationId, csq, parameter);

      if (threshold == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                ApiResponseDto.error(
                    new ErrorResponseDto(
                        "/api/alarm/get",
                        HttpStatus.NOT_FOUND,
                        "Threshold not found.",
                        LocalDateTime.now())));
      }
      thresholds = List.of(threshold);
    } else {
      thresholds = alarmThresholdService.getAllThresholds();

      if (thresholds == null || thresholds.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                ApiResponseDto.error(
                    new ErrorResponseDto(
                        "/api/alarm/get",
                        HttpStatus.NOT_FOUND,
                        "Threshold not found.",
                        LocalDateTime.now())));
      }
    }

    return ResponseEntity.ok(ApiResponseDto.success(thresholds));
  }

  @Operation(
      summary = "Update Alarm Threshold",
      description =
          "Creates or updates the alarm threshold configuration for a specified station, CSQ, and sensor parameter.")
  @ApiResponses({
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_200,
        description = "Threshold updated successfully",
        content = @Content(schema = @Schema(implementation = AlarmThreshold.class))),
    @ApiResponse(
        responseCode = MessagesConstants.STATUS_409,
        description = "Update operation failed",
        content = @Content(schema = @Schema(implementation = String.class)))
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
                      HttpStatus.CONFLICT,
                      "Could not update threshold. Possibly a conflict or missing data.",
                      LocalDateTime.now())));
    }
  }

  @Operation(
      summary = "Add a New Alarm Threshold",
      description =
          "Adds a new alarm threshold configuration if it doesn't already exist. Returns 409 if duplicate.")
  @ApiResponses({
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
                      HttpStatus.CONFLICT,
                      "Threshold already exists for the given station, CSQ, and sensor parameter.",
                      LocalDateTime.now())));
    }

    AlarmThreshold created = alarmThresholdService.saveNewThreshold(threshold);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(created));
  }
}
