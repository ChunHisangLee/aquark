package com.jack.aquark.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlarmDetail {
  private String stationId;
  private String obsTime;
  private String csq;
  private String parameter;
  private BigDecimal sensorValue;
  private BigDecimal thresholdValue;
  private String message;
}
