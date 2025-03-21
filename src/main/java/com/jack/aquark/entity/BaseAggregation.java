package com.jack.aquark.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Common fields for DailyAggregation and HourlyAggregation. MappedSuperclass means these fields map
 * to columns in child entities, but there's no separate 'base_aggregation' table.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Required by JPA
public abstract class BaseAggregation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "station_id", nullable = false)
  private String stationId;

  @Column(name = "obs_date", nullable = false)
  private LocalDate obsDate;

  @Column(name = "csq", nullable = false)
  private String csq;

  @Column(name = "v1_sum_value")
  private BigDecimal v1SumValue;

  @Column(name = "v1_avg_value")
  private BigDecimal v1AvgValue;

  @Column(name = "v2_sum_value")
  private BigDecimal v2SumValue;

  @Column(name = "v2_avg_value")
  private BigDecimal v2AvgValue;

  @Column(name = "v3_sum_value")
  private BigDecimal v3SumValue;

  @Column(name = "v3_avg_value")
  private BigDecimal v3AvgValue;

  @Column(name = "v4_sum_value")
  private BigDecimal v4SumValue;

  @Column(name = "v4_avg_value")
  private BigDecimal v4AvgValue;

  @Column(name = "v5_sum_value")
  private BigDecimal v5SumValue;

  @Column(name = "v5_avg_value")
  private BigDecimal v5AvgValue;

  @Column(name = "v6_sum_value")
  private BigDecimal v6SumValue;

  @Column(name = "v6_avg_value")
  private BigDecimal v6AvgValue;

  @Column(name = "v7_sum_value")
  private BigDecimal v7SumValue;

  @Column(name = "v7_avg_value")
  private BigDecimal v7AvgValue;

  @Column(name = "rh_sum_value")
  private BigDecimal rhSumValue;

  @Column(name = "rh_avg_value")
  private BigDecimal rhAvgValue;

  @Column(name = "tx_sum_value")
  private BigDecimal txSumValue;

  @Column(name = "tx_avg_value")
  private BigDecimal txAvgValue;

  @Column(name = "echo_sum_value")
  private BigDecimal echoSumValue;

  @Column(name = "echo_avg_value")
  private BigDecimal echoAvgValue;

  @Column(name = "rainD_sum_value")
  private BigDecimal rainDSumValue;

  @Column(name = "rainD_avg_value")
  private BigDecimal rainDAvgValue;

  @Column(name = "speed_sum_value")
  private BigDecimal speedSumValue;

  @Column(name = "speed_avg_value")
  private BigDecimal speedAvgValue;
}
