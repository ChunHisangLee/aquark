package com.jack.aquark.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "daily_aggregation",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"station_id", "obs_date", "csq"})})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class DailyAggregation extends BaseAggregation {}
