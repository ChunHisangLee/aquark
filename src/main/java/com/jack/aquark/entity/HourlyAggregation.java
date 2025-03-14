package com.jack.aquark.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(
        name = "hourly_aggregation",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"station_id", "obs_date", "obs_hour", "csq"}
                )
        }
)
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

    // ---------- v1 ----------
    @Column(name = "v1_sum_value")
    private BigDecimal v1SumValue;

    @Column(name = "v1_avg_value")
    private BigDecimal v1AvgValue;

    // ---------- v2 ----------
    @Column(name = "v2_sum_value")
    private BigDecimal v2SumValue;

    @Column(name = "v2_avg_value")
    private BigDecimal v2AvgValue;

    // ---------- v3 ----------
    @Column(name = "v3_sum_value")
    private BigDecimal v3SumValue;

    @Column(name = "v3_avg_value")
    private BigDecimal v3AvgValue;

    // ---------- v4 ----------
    @Column(name = "v4_sum_value")
    private BigDecimal v4SumValue;

    @Column(name = "v4_avg_value")
    private BigDecimal v4AvgValue;

    // ---------- v5 ----------
    @Column(name = "v5_sum_value")
    private BigDecimal v5SumValue;

    @Column(name = "v5_avg_value")
    private BigDecimal v5AvgValue;

    // ---------- v6 ----------
    @Column(name = "v6_sum_value")
    private BigDecimal v6SumValue;

    @Column(name = "v6_avg_value")
    private BigDecimal v6AvgValue;

    // ---------- v7 ----------
    @Column(name = "v7_sum_value")
    private BigDecimal v7SumValue;

    @Column(name = "v7_avg_value")
    private BigDecimal v7AvgValue;

    // ---------- rh ----------
    @Column(name = "rh_sum_value")
    private BigDecimal rhSumValue;

    @Column(name = "rh_avg_value")
    private BigDecimal rhAvgValue;

    // ---------- tx ----------
    @Column(name = "tx_sum_value")
    private BigDecimal txSumValue;

    @Column(name = "tx_avg_value")
    private BigDecimal txAvgValue;

    // ---------- echo ----------
    @Column(name = "echo_sum_value")
    private BigDecimal echoSumValue;

    @Column(name = "echo_avg_value")
    private BigDecimal echoAvgValue;

    // ---------- rainD ----------
    @Column(name = "rainD_sum_value")
    private BigDecimal rainDSumValue;

    @Column(name = "rainD_avg_value")
    private BigDecimal rainDAvgValue;

    // ---------- speed ----------
    @Column(name = "speed_sum_value")
    private BigDecimal speedSumValue;

    @Column(name = "speed_avg_value")
    private BigDecimal speedAvgValue;
}
