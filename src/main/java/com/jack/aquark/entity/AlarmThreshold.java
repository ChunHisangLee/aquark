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

  // sensorType 可設定為 "v1", "rh", "tx", "echo", "rainD" 等
  @Column(unique = true)
  private String sensorType;

  private Double threshold;
}
