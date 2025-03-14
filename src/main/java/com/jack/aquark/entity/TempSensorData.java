package com.jack.aquark.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
    name = "temp_sensor_data",
    uniqueConstraints = @UniqueConstraint(columnNames = {"station_id", "obs_time", "csq"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempSensorData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "station_id", nullable = false)
  private String stationId;

  @Column(name = "obs_time", nullable = false)
  private LocalDateTime obsTime;

  @Column(name = "csq", nullable = false)
  private String csq;

  @Column(name = "v1")
  private BigDecimal v1;

  @Column(name = "v2")
  private BigDecimal v2;

  @Column(name = "v3")
  private BigDecimal v3;

  @Column(name = "v4")
  private BigDecimal v4;

  @Column(name = "v5")
  private BigDecimal v5;

  @Column(name = "v6")
  private BigDecimal v6;

  @Column(name = "v7")
  private BigDecimal v7;

  @Column(name = "rh")
  private BigDecimal rh;

  @Column(name = "tx")
  private BigDecimal tx;

  @Column(name = "echo")
  private BigDecimal echo;

  @Column(name = "rain_d")
  private BigDecimal rainD;

  @Column(name = "speed")
  private BigDecimal speed;
}
