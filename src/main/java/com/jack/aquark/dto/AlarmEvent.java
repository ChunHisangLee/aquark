package com.jack.aquark.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmEvent {
    private String stationId;
    private String csq;
    private String parameter;
    private BigDecimal sensorValue;
    private BigDecimal thresholdValue;
    private LocalDateTime obsTime;
    private String message;
}
