package com.jack.aquark.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "hourly_aggregation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyAggregation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Date of aggregation
  @Column(name = "obs_date", nullable = false)
  private LocalDate obsDate;

  // Aggregated hour (0 - 23)
  @Column(name = "obs_hour", nullable = false)
  private int obsHour;

  @Column(name = "sensor_name", nullable = false)
  private String sensorName;

  // Sum of the sensor's values in this hour
  @Column(name = "sum_value")
  private Double sumValue;

  // Average of the sensor's values in this hour
  @Column(name = "avg_value")
  private Double avgValue;
}
