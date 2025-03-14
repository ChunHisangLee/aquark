package com.jack.aquark.repository;

import com.jack.aquark.entity.TempSensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempSensorDataRepository extends JpaRepository<TempSensorData, Long> {
}
