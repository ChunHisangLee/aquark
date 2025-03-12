package com.jack.aquark.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_aggregation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyAggregation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obs_date", nullable = false)
    private LocalDate obsDate;

    @Column(name = "sensor_name", nullable = false)
    private String sensorName;

    @Column(name = "sum_value")
    private Double sumValue;

    @Column(name = "avg_hourly_value")
    private Double avgHourlyValue;

    @Column(name = "avg_daily_value")
    private Double avgDailyValue;
}
