package com.jack.aquark.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** A wrapper for either a success response (data) or an error (error). */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

  private final boolean success;
  private final T data;
  private final ErrorResponseDto error;

  public static <T> ApiResponseDto<T> success(T data) {
    return new ApiResponseDto<>(true, data, null);
  }

  public static <T> ApiResponseDto<T> error(ErrorResponseDto error) {
    return new ApiResponseDto<>(false, null, error);
  }
}
