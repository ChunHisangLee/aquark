package com.jack.aquark.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensor_threshold")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorThreshold {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String sensorName;

  private Double thresholdValue;
}
