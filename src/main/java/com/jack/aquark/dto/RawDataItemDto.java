package com.jack.aquark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RawDataItemDto {
  private String stationId;
  private String obsTime;

  @JsonProperty("CSQ")
  private String csq;

  private Sensor sensor;
  private BigDecimal rainD;

  @Data
  public static class Sensor {
    @JsonProperty("Volt")
    private Volt volt;

    @JsonProperty("StickTxRh")
    private StickTxRh stickTxRh;

    @JsonProperty("Ultrasonic_Level")
    private UltrasonicLevel ultrasonicLevel;

    @JsonProperty("Water_speed_aquark")
    private WaterSpeedAquark waterSpeedAquark;
  }

  @Data
  public static class Volt {
    private BigDecimal v1;
    private BigDecimal v2;
    private BigDecimal v3;
    private BigDecimal v4;
    private BigDecimal v5;
    private BigDecimal v6;
    private BigDecimal v7;
  }

  @Data
  public static class StickTxRh {
    private BigDecimal rh;
    private BigDecimal tx;
  }

  @Data
  public static class UltrasonicLevel {
    private BigDecimal echo;
  }

  @Data
  public static class WaterSpeedAquark {
    private BigDecimal speed;
  }
}
