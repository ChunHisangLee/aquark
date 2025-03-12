package com.jack.aquark.service.impl;

import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.repository.DailyAggregationRepository;
import com.jack.aquark.service.DailyAggregationService;
import com.jack.aquark.service.SensorDataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class DailyAggregationServiceImpl implements DailyAggregationService {

    private final SensorDataService sensorDataService;
    private final DailyAggregationRepository dailyAggregationRepository;

    @Override
    public void aggregateDailyData() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 1) Get all sensor data from yesterday
        List<SensorData> dataList = sensorDataService.getSensorDataByDate(yesterday);

        // 2) Group by sensor field (e.g., "v1", "v2", etc.)
        //    For demonstration, let's do a quick example for "v1" only
        List<Double> v1Values = dataList.stream()
                .map(SensorData::getV1)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        double sumV1 = v1Values.stream().mapToDouble(Double::doubleValue).sum();
        double avgV1 = v1Values.isEmpty() ? 0 : sumV1 / v1Values.size();

        // Hourly average could be computed by grouping data by hour, etc.
        // For simplicity, let's do a quick placeholder:
        double hourlyAvgV1 = avgV1; // real logic: compute average per hour

        // 3) Save results to daily_aggregation
        DailyAggregation aggregation = DailyAggregation.builder()
                .obsDate(yesterday)
                .sensorName("v1")
                .sumValue(sumV1)
                .avgHourlyValue(hourlyAvgV1)
                .avgDailyValue(avgV1)
                .build();

        dailyAggregationRepository.save(aggregation);
        log.info("Daily aggregation saved for date: {} sensor: v1", yesterday);
    }
}
