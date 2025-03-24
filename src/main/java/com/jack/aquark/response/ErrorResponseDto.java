package com.jack.aquark.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Schema(name = "ErrorResponse", description = "Schema to hold error response information")
@Data
@AllArgsConstructor
public class ErrorResponseDto {
  @Schema(description = "Path of the API", example = "/api/alarm/update")
  private String apiPath;

  @Schema(description = "HTTP status code", example = "500")
  private HttpStatus errorCode;

  @Schema(description = "Error message", example = "Internal Server Error")
  private String errorMessage;

  @Schema(description = "Timestamp of the error", example = "2023-01-01T00:00:00")
  private LocalDateTime errorTime;
}
