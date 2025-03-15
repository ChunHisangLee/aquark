package com.jack.aquark.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jack.aquark.dto.RawDataItemDto;
import com.jack.aquark.dto.RawDataWrapperDto;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.SensorDataRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SensorDataServiceImplTest {

  @InjectMocks private SensorDataServiceImpl sensorDataServiceImpl;

  @Mock private SensorDataRepository sensorDataRepository;

  @Mock private TempSensorDataRepository tempSensorDataRepository;

  @Mock private HourlyAggregationRepository hourlyAggregationRepository;

  @BeforeEach
  void setUp() {}

  @Test
  void testFetchAndSaveSensorData_Success() {
    // Use a spy to override fetchRawDataFromUrl so that it does not perform an actual HTTP call.
    SensorDataServiceImpl spyService = spy(sensorDataServiceImpl);

    // Prepare a raw data item with valid values.
    RawDataItemDto item = new RawDataItemDto();
    item.setStationId("stationA");
    item.setCsq("31");
    item.setObsTime("2025-03-11 10:15:00");
    // In this example, we leave the sensor as null (so parse methods will produce a
    // SensorData/TempSensorData with null sensor fields).

    RawDataWrapperDto wrapper = new RawDataWrapperDto();
    wrapper.setRaw(List.of(item));

    // Stub the fetchRawDataFromUrl to return our prepared wrapper.
    doReturn(wrapper).when(spyService).fetchRawDataFromUrl(anyString());

    // Simulate that no duplicate exists.
    when(sensorDataRepository.existsByStationIdAndObsTimeAndCsq(
            eq("stationA"), eq(LocalDateTime.of(2025, 3, 11, 10, 15)), eq("31")))
        .thenReturn(false);

    // Call the method under test.
    spyService.fetchAndSaveSensorData("http://mock-api/sensor");

    // Verify that both sensorDataRepository and tempSensorDataRepository saved one record.
    verify(sensorDataRepository, times(1)).save(any(SensorData.class));
    verify(tempSensorDataRepository, times(1)).save(any(TempSensorData.class));
  }

  @Test
  void testFetchAndSaveSensorData_Duplicate() {
    // Test the case when the record already exists, so saving should be skipped.
    SensorDataServiceImpl spyService = spy(sensorDataServiceImpl);

    RawDataItemDto item = new RawDataItemDto();
    item.setStationId("stationA");
    item.setCsq("31");
    item.setObsTime("2025-03-11 10:15:00");
    item.setSensor(null);

    RawDataWrapperDto wrapper = new RawDataWrapperDto();
    wrapper.setRaw(List.of(item));

    doReturn(wrapper).when(spyService).fetchRawDataFromUrl(anyString());

    // Simulate duplicate exists.
    when(sensorDataRepository.existsByStationIdAndObsTimeAndCsq(
            eq("stationA"), eq(LocalDateTime.of(2025, 3, 11, 10, 15)), eq("31")))
        .thenReturn(true);

    spyService.fetchAndSaveSensorData("http://mock-api/sensor");

    // Verify that no save operations occur since the record is a duplicate.
    verify(sensorDataRepository, never()).save(any(SensorData.class));
    verify(tempSensorDataRepository, never()).save(any(TempSensorData.class));
  }

  @Test
  void testGetSensorDataByTimeRange() {
    // Prepare sample SensorData.
    LocalDateTime start = LocalDateTime.of(2025, 3, 11, 15, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 11, 23, 0);
    SensorData data = new SensorData();
    data.setStationId("stationB");
    data.setObsTime(LocalDateTime.of(2025, 3, 11, 16, 0));

    when(sensorDataRepository.findAllByObsTimeBetweenOrderByObsTimeAsc(start, end))
        .thenReturn(List.of(data));

    List<SensorData> result = sensorDataServiceImpl.getSensorDataByTimeRange(start, end);
    assertEquals(1, result.size());
    assertEquals("stationB", result.getFirst().getStationId());
    verify(sensorDataRepository).findAllByObsTimeBetweenOrderByObsTimeAsc(start, end);
  }

  @Test
  void testGetHourlyAverage() {
    // Prepare a sample HourlyAggregation.
    LocalDateTime start = LocalDateTime.of(2025, 3, 11, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 11, 23, 59);
    HourlyAggregation agg = new HourlyAggregation();
    agg.setStationId("stationC");
    agg.setObsDate(LocalDate.of(2025, 3, 11));
    agg.setCsq("31");

    when(hourlyAggregationRepository.findByObsDateBetween(
            eq(LocalDate.of(2025, 3, 11)), eq(LocalDate.of(2025, 3, 11))))
        .thenReturn(List.of(agg));

    List<HourlyAggregation> result = sensorDataServiceImpl.getHourlyAverage(start, end);
    assertEquals(1, result.size());
    assertEquals("stationC", result.getFirst().getStationId());
    verify(hourlyAggregationRepository)
        .findByObsDateBetween(LocalDate.of(2025, 3, 11), LocalDate.of(2025, 3, 11));
  }

  @Test
  void testGetPeakTimeData() {
    // Create SensorData with a peak time and an off-peak time.
    SensorData peakData = new SensorData();
    // Monday at 10:00 is considered peak (Monday: between 7:30 and 17:30)
    peakData.setObsTime(LocalDateTime.of(2025, 3, 10, 10, 0));
    peakData.setStationId("peakStation");

    SensorData offPeakData = new SensorData();
    // Saturday at 12:00 is off-peak
    offPeakData.setObsTime(LocalDateTime.of(2025, 3, 15, 12, 0));
    offPeakData.setStationId("offPeakStation");

    when(sensorDataRepository.findAllByObsTimeBetweenOrderByObsTimeAsc(any(), any()))
        .thenReturn(List.of(peakData, offPeakData));

    // When requesting data over a range that includes both days.
    List<SensorData> peakResults =
        sensorDataServiceImpl.getPeakTimeData(
            LocalDateTime.of(2025, 3, 9, 0, 0), LocalDateTime.of(2025, 3, 16, 0, 0));
    // Only the Monday record (peakData) should be returned.
    assertEquals(1, peakResults.size());
    assertEquals("peakStation", peakResults.getFirst().getStationId());
  }

  @Test
  void testGetOffPeakTimeData() {
    // Create SensorData with a peak time and an off-peak time.
    SensorData peakData = new SensorData();
    // Monday at 10:00 is considered peak.
    peakData.setObsTime(LocalDateTime.of(2025, 3, 10, 10, 0));
    peakData.setStationId("peakStation");

    SensorData offPeakData = new SensorData();
    // Saturday at 12:00 is off-peak.
    offPeakData.setObsTime(LocalDateTime.of(2025, 3, 15, 12, 0));
    offPeakData.setStationId("offPeakStation");

    when(sensorDataRepository.findAllByObsTimeBetweenOrderByObsTimeAsc(any(), any()))
        .thenReturn(List.of(peakData, offPeakData));

    List<SensorData> offPeakResults =
        sensorDataServiceImpl.getOffPeakTimeData(
            LocalDateTime.of(2025, 3, 9, 0, 0), LocalDateTime.of(2025, 3, 16, 0, 0));
    // Only the Saturday record (offPeakData) should be returned.
    assertEquals(1, offPeakResults.size());
    assertEquals("offPeakStation", offPeakResults.getFirst().getStationId());
  }
}
