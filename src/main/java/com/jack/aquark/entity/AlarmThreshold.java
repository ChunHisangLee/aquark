package com.jack.aquark.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(
    name = "alarm_threshold",
    uniqueConstraints = @UniqueConstraint(columnNames = {"station_id", "csq", "parameter"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmThreshold {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "station_id", nullable = false)
  private String stationId;

  @Column(name = "csq", nullable = false)
  private String csq;

  // The sensor parameter (for example, "v1", "v2", "rh", etc.)
  @Column(name = "parameter", nullable = false)
  private String parameter;

  @Column(name = "threshold_value", nullable = false)
  private BigDecimal thresholdValue;
}
