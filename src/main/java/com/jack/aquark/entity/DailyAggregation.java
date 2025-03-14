package com.jack.aquark.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "daily_aggregation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyAggregation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Date of aggregation (the day the data belongs to)
  @Column(name = "obs_date", nullable = false)
  private LocalDate obsDate;

  // The hour of the day for this record (0-23)
  @Column(name = "obs_hour", nullable = false)
  private int obsHour;

  @Column(name = "sensor_name", nullable = false)
  private String sensorName;

  // Sum for that hour (could be the same as the hourly aggregation)
  @Column(name = "sum_value")
  private BigDecimal sumValue;

  // Average for that hour
  @Column(name = "avg_value")
  private BigDecimal avgValue;
}
