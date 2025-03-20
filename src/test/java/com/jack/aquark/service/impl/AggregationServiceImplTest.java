package com.jack.aquark.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.repository.DailyAggregationRepository;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregationServiceImplTest {

  @Mock private HourlyAggregationRepository hourlyAggregationRepository;

  @Mock private DailyAggregationRepository dailyAggregationRepository;

  @Mock private TempSensorDataRepository tempSensorDataRepository;

  @InjectMocks private AggregationServiceImpl aggregationService;

  @Test
  void testAggregateHourlyData_NoData() {
    when(tempSensorDataRepository.findAll()).thenReturn(Collections.emptyList());

    aggregationService.aggregateHourlyData();

    verify(tempSensorDataRepository).findAll();
    verify(hourlyAggregationRepository, never()).save(any(HourlyAggregation.class));
  }

  @Test
  void testAggregateHourlyData_WithData() {
    TempSensorData temp1 = new TempSensorData();
    temp1.setStationId("stationA");
    temp1.setObsTime(LocalDateTime.of(2025, 3, 11, 10, 15));
    temp1.setCsq("31");
    temp1.setV1(new BigDecimal("10.0"));

    TempSensorData temp2 = new TempSensorData();
    temp2.setStationId("stationA");
    temp2.setObsTime(LocalDateTime.of(2025, 3, 11, 10, 30));
    temp2.setCsq("31");
    temp2.setV1(new BigDecimal("20.0"));

    when(tempSensorDataRepository.findAll()).thenReturn(Arrays.asList(temp1, temp2));
    when(hourlyAggregationRepository.findByStationIdAndObsDateAndObsHourAndCsq(
            anyString(), any(LocalDate.class), anyInt(), anyString()))
        .thenReturn(Optional.empty());

    aggregationService.aggregateHourlyData();

    verify(tempSensorDataRepository).findAll();
    verify(hourlyAggregationRepository, times(1)).save(any(HourlyAggregation.class));
  }

  @Test
  void testAggregateDailyData_WithData() {
    HourlyAggregation ha1 = new HourlyAggregation();
    ha1.setStationId("stationB");
    ha1.setObsDate(LocalDate.of(2025, 3, 11));
    ha1.setCsq("31");
    ha1.setV1SumValue(new BigDecimal("10.0"));
    ha1.setV1AvgValue(new BigDecimal("10.0"));

    HourlyAggregation ha2 = new HourlyAggregation();
    ha2.setStationId("stationB");
    ha2.setObsDate(LocalDate.of(2025, 3, 11));
    ha2.setCsq("31");
    ha2.setV1SumValue(new BigDecimal("20.0"));
    ha2.setV1AvgValue(new BigDecimal("20.0"));

    when(hourlyAggregationRepository.findAll()).thenReturn(Arrays.asList(ha1, ha2));
    when(dailyAggregationRepository.findByStationIdAndObsDateAndCsq(
            anyString(), any(LocalDate.class), anyString()))
        .thenReturn(Optional.empty());

    aggregationService.aggregateDailyData();

    verify(hourlyAggregationRepository).findAll();
    verify(dailyAggregationRepository, atLeastOnce()).save(any(DailyAggregation.class));
  }
}
