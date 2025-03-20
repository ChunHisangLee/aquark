package com.jack.aquark.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "temp_sensor_data",
    uniqueConstraints = @UniqueConstraint(columnNames = {"station_id", "obs_time", "csq"}))
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class TempSensorData extends BaseSensorData {}
