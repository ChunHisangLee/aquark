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

  @Column(name = "station_id")
  private String stationId;

  @Column(name = "obs_time", nullable = false)
  private LocalDateTime obsTime;

  @Column(name = "csq")
  private String csq;

  @Column(name = "v1")
  private Double v1;

  @Column(name = "v2")
  private Double v2;

  @Column(name = "v3")
  private Double v3;

  @Column(name = "v4")
  private Double v4;

  @Column(name = "v5")
  private Double v5;

  @Column(name = "v6")
  private Double v6;

  @Column(name = "v7")
  private Double v7;

  @Column(name = "rh")
  private Double rh;

  @Column(name = "tx")
  private Double tx;

  @Column(name = "echo")
  private Double echo;

  @Column(name = "rain_d")
  private Double rainD;

  @Column(name = "speed")
  private Double speed;
}
