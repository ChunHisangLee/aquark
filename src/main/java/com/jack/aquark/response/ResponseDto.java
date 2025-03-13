package com.jack.aquark.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(name = "Response", description = "Schema to hold successful response information")
@Data
@AllArgsConstructor
public class ResponseDto {
  @Schema(description = "Status code", example = "200")
  private String statusCode;

  @Schema(description = "Status message", example = "Success")
  private String statusMsg;
}
