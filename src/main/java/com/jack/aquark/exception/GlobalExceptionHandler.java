package com.jack.aquark.exception;

import com.jack.aquark.response.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ThresholdNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleThresholdNotFound(
      ThresholdNotFoundException exception, WebRequest webRequest) {
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(
            webRequest.getDescription(false),
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            LocalDateTime.now());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(DataFetchException.class)
  public ResponseEntity<ErrorResponseDto> handleDataFetchException(
      DataFetchException exception, WebRequest webRequest) {
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(
            webRequest.getDescription(false),
            HttpStatus.SERVICE_UNAVAILABLE,
            exception.getMessage(),
            LocalDateTime.now());
    return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(DataParseException.class)
  public ResponseEntity<ErrorResponseDto> handleDataParseException(
      DataParseException exception, WebRequest webRequest) {
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(
            webRequest.getDescription(false),
            HttpStatus.BAD_GATEWAY,
            exception.getMessage(),
            LocalDateTime.now());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleOtherExceptions(
      Exception exception, WebRequest webRequest) {
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(
            webRequest.getDescription(false),
            HttpStatus.INTERNAL_SERVER_ERROR,
            exception.getMessage(),
            LocalDateTime.now());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
