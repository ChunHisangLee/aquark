package com.jack.aquark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RawDataItemDto {
  private String stationId;
  private String obsTime;

  @JsonProperty("CSQ")
  private String csq;

  private Sensor sensor;
  private Double rainD;

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
    private Double v1;
    private Double v2;
    private Double v3;
    private Double v4;
    private Double v5;
    private Double v6;
    private Double v7;
  }

  @Data
  public static class StickTxRh {
    private Double rh;
    private Double tx;
  }

  @Data
  public static class UltrasonicLevel {
    private Double echo;
  }

  @Data
  public static class WaterSpeedAquark {
    private Double speed;
  }
}
