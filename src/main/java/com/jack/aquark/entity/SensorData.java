package com.jack.aquark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "sensor_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String stationId;

  private LocalDateTime obsTime;

  private String csq;

  // 電壓
  private Double v1;
  private Double v2;
  private Double v3;
  private Double v4;
  private Double v5;
  private Double v6;
  private Double v7;

  // 濕度%與溫度℃
  private Double rh;
  private Double tx;

  // 水位空高
  private Double echo;

  // 日累積雨量
  private Double rainD;

  // speed 表面流速 m/s
  private Double speed;
}
