package com.jack.aquark.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alarm_threshold")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmThreshold {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sensor_name", unique = true, nullable = false)
  private String sensorName;

  @Column(name = "threshold_value", nullable = false)
  private Double thresholdValue;
}
