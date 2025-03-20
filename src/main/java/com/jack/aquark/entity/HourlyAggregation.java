package com.jack.aquark.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "hourly_aggregation",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"station_id", "obs_date", "obs_hour", "csq"})
    })
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HourlyAggregation extends BaseAggregation {

  @Column(name = "obs_hour", nullable = false)
  private int obsHour;
}
