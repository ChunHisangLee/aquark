package com.jack.aquark.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(
    name = "hourly_aggregation",
    uniqueConstraints =
        @UniqueConstraint(
            columnNames = {"station_id", "obs_date", "obs_hour", "csq", "sensor_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyAggregation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "station_id", nullable = false)
  private String stationId;

  @Column(name = "obs_date", nullable = false)
  private LocalDate obsDate;

  @Column(name = "obs_hour", nullable = false)
  private int obsHour;

  @Column(name = "csq", nullable = false)
  private String csq;

  // The measurement parameter (for example: "v1", "rh", etc.)
  @Column(name = "sensor_name", nullable = false)
  private String sensorName;

  @Column(name = "sum_value")
  private BigDecimal sumValue;

  @Column(name = "avg_value")
  private BigDecimal avgValue;
}
