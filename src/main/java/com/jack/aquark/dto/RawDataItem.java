package com.jack.aquark.dto;

import lombok.Data;

@Data
public class RawDataItem {

  private String stationId;
  private String obsTime; // We'll parse it into LocalDateTime manually
  private String cSQ;
  private Sensor sensor;
  private Double rainD;

  @Data
  public static class Sensor {
    private Volt volt;
    private StickTxRh stickTxRh;
    private UltrasonicLevel ultrasonicLevel;
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
