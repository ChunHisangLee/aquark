package com.jack.aquark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "sensor_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String stationId;

  private LocalDateTime obsTime;
  // v1 鋰電池電壓
  private Double v1;
  // v5 太陽能板 1 電壓
  private Double v5;
  // v6 太陽能板 2 電壓
  private Double v6;
  // rh 濕度%
  private Double rh;
  // tx 溫度℃
  private Double tx;
  // echo 水位空高 m
  private Double echo;
  // rain_d 日累積雨量 mm
  private Double rainD;
  // speed 表面流速 m/s
  private Double speed;
}
