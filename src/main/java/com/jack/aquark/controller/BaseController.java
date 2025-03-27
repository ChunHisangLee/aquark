package com.jack.aquark.controller;

import com.jack.aquark.response.ApiResponseDto;
import com.jack.aquark.response.ErrorResponseDto;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

  protected <T> ResponseEntity<ApiResponseDto<T>> respondOK(T data) {
    return ResponseEntity.ok(ApiResponseDto.success(data));
  }

  protected <T> ResponseEntity<ApiResponseDto<T>> respondCreated(T data) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(data));
  }

  protected <T> ResponseEntity<ApiResponseDto<T>> respondError(
      String path, HttpStatus status, String errorMessage) {

    ErrorResponseDto errorResponse =
        new ErrorResponseDto(path, status, errorMessage, LocalDateTime.now());
    return ResponseEntity.status(status).body(ApiResponseDto.error(errorResponse));
  }
}
