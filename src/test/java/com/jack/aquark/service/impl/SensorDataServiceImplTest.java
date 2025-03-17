package com.jack.aquark.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.aquark.dto.RawDataItemDto;
import com.jack.aquark.dto.RawDataWrapperDto;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.SensorDataRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import com.jack.aquark.service.AggregationService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

/**
 * Example test class for SensorDataServiceImpl that: 1) Partially mocks fetchRawDataFromUrl(...) so
 * we can provide test data. 2) Confirms the repository saves are invoked for valid data and not
 * invoked for duplicates. 3) Uses sufficiently broad time ranges for peak/off-peak tests to ensure
 * test records fall inside the range.
 */
public class SensorDataServiceImplTest {

  @Mock private SensorDataRepository sensorDataRepository;
  @Mock private TempSensorDataRepository tempSensorDataRepository;
  @Mock private HourlyAggregationRepository hourlyAggregationRepository;
  @Mock private AggregationService aggregationService;

  // We won't use @InjectMocks here, because we want to create a partial mock of
  // SensorDataServiceImpl
  private AutoCloseable closeable;

  @BeforeEach
  void init() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void close() throws Exception {
    closeable.close();
  }

  @Test
  void testFetchAndSaveSensorData_withValidData() {
    // 1) Prepare test data
    RawDataItemDto item = new RawDataItemDto();
    item.setObsTime("2025-03-16 10:00:00");
    item.setStationId("station1");
    item.setCsq("csq1");
    item.setRainD(BigDecimal.ZERO);

    // Prepare nested sensor fields
    RawDataItemDto.Sensor sensor = new RawDataItemDto.Sensor();
    RawDataItemDto.Volt volt = new RawDataItemDto.Volt();
    volt.setV1(BigDecimal.ONE);
    volt.setV2(BigDecimal.valueOf(2.0));
    sensor.setVolt(volt);
    item.setSensor(sensor);

    RawDataWrapperDto wrapper = new RawDataWrapperDto();
    wrapper.setRaw(Collections.singletonList(item));

    // 2) Build the real service instance, but then create a partial mock of it
    //    so we can override fetchRawDataFromUrl(...)
    SensorDataServiceImpl realService =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            new ObjectMapper(),
            aggregationService);
    SensorDataServiceImpl partialMock = Mockito.spy(realService);

    // 3) Stub out fetchRawDataFromUrl(...) to return the wrapper
    Mockito.doReturn(wrapper).when(partialMock).fetchRawDataFromUrl(anyString());

    // 4) Also stub the duplicate-check
    when(sensorDataRepository.existsByStationIdAndObsTimeAndCsq(
            eq("station1"), any(LocalDateTime.class), eq("csq1")))
        .thenReturn(false);

    // 5) Call the partial mock
    partialMock.fetchAndSaveSensorData("dummyurl");

    // 6) Verify that we actually saved sensor + temp data
    verify(sensorDataRepository, times(1)).save(any(SensorData.class));
    verify(tempSensorDataRepository, times(1)).save(any(TempSensorData.class));
  }

  @Test
  void testFetchAndSaveSensorData_withDuplicateData() {
    // 1) Test data
    RawDataItemDto item = new RawDataItemDto();
    item.setObsTime("2025-03-16 11:00:00");
    item.setStationId("station1");
    item.setCsq("csq1");
    item.setRainD(BigDecimal.ZERO);

    RawDataWrapperDto wrapper = new RawDataWrapperDto();
    wrapper.setRaw(Collections.singletonList(item));

    // 2) Build partial mock
    SensorDataServiceImpl realService =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            new ObjectMapper(),
            aggregationService);
    SensorDataServiceImpl partialMock = Mockito.spy(realService);

    Mockito.doReturn(wrapper).when(partialMock).fetchRawDataFromUrl(anyString());

    // Mark the record as a duplicate
    when(sensorDataRepository.existsByStationIdAndObsTimeAndCsq(
            eq("station1"), any(LocalDateTime.class), eq("csq1")))
        .thenReturn(true);

    // 3) Execute
    partialMock.fetchAndSaveSensorData("dummyurl");

    // 4) Because it's duplicate, verify no saves occur
    verify(sensorDataRepository, never()).save(any(SensorData.class));
    verify(tempSensorDataRepository, never()).save(any(TempSensorData.class));
  }

  @Test
  void testGetHourlyAverage() {
    // 1) Setup date range
    LocalDateTime start = LocalDateTime.of(2025, 3, 15, 0, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 16, 23, 59, 59);

    // 2) Fake result
    HourlyAggregation agg = new HourlyAggregation();
    List<HourlyAggregation> list = Collections.singletonList(agg);

    // 3) Stub
    when(hourlyAggregationRepository.findByObsDateBetween(
            eq(start.toLocalDate()), eq(end.toLocalDate())))
        .thenReturn(list);

    // 4) Build the real service (no partial mock needed here)
    SensorDataServiceImpl service =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            new ObjectMapper(),
            aggregationService);

    List<HourlyAggregation> result = service.getHourlyAverage(start, end);

    // 5) Check
    assertEquals(1, result.size());
    verify(hourlyAggregationRepository, times(1))
        .findByObsDateBetween(eq(start.toLocalDate()), eq(end.toLocalDate()));
  }

  @Test
  void testGetPeakTimeData() {
    // 1) Create sample SensorData
    SensorData dataPeakMonday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 17, 8, 0, 0)) // Monday 08:00 (peak)
            .build();
    SensorData dataOffPeakMonday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 17, 6, 0, 0)) // Monday 06:00 (off-peak)
            .build();
    SensorData dataPeakThursday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 20, 12, 0, 0)) // Thursday 12:00 (peak)
            .build();
    SensorData dataOffPeakSaturday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 22, 10, 0, 0)) // Saturday (off-peak)
            .build();

    List<SensorData> allData =
        Arrays.asList(dataPeakMonday, dataOffPeakMonday, dataPeakThursday, dataOffPeakSaturday);

    // 2) Provide a wide date range
    LocalDateTime rangeStart = LocalDateTime.of(2025, 3, 15, 0, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 3, 25, 0, 0, 0);

    // 3) Stub aggregator
    when(aggregationService.getSensorDataByTimeRange(eq(rangeStart), eq(rangeEnd)))
        .thenReturn(allData);

    // 4) Build real service
    SensorDataServiceImpl service =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            new ObjectMapper(),
            aggregationService);

    // 5) Execute
    List<SensorData> peakData = service.getPeakTimeData(rangeStart, rangeEnd);

    // 6) Verify
    assertTrue(peakData.contains(dataPeakMonday), "Should contain Monday 08:00");
    assertTrue(peakData.contains(dataPeakThursday), "Should contain Thursday 12:00");
    assertFalse(peakData.contains(dataOffPeakMonday), "Should not contain Monday 06:00");
    assertFalse(peakData.contains(dataOffPeakSaturday), "Should not contain Sat 10:00");
  }

  @Test
  void testGetOffPeakTimeData() {
    // 1) Create sample data
    SensorData dataPeakTuesday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 18, 10, 0, 0)) // Tuesday 10:00 (peak)
            .build();
    SensorData dataOffPeakTuesday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 18, 6, 0, 0)) // Tuesday 06:00 (off-peak)
            .build();
    SensorData dataPeakFriday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 21, 15, 0, 0)) // Friday 15:00 (peak)
            .build();
    SensorData dataOffPeakSunday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 23, 14, 0, 0)) // Sunday 14:00 (off-peak)
            .build();

    List<SensorData> allData =
        Arrays.asList(dataPeakTuesday, dataOffPeakTuesday, dataPeakFriday, dataOffPeakSunday);

    // 2) Broad date range
    LocalDateTime rangeStart = LocalDateTime.of(2025, 3, 15, 0, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 3, 25, 0, 0, 0);

    // 3) Stub aggregator
    when(aggregationService.getSensorDataByTimeRange(eq(rangeStart), eq(rangeEnd)))
        .thenReturn(allData);

    // 4) Build
    SensorDataServiceImpl service =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            new ObjectMapper(),
            aggregationService);

    // 5) Execute
    List<SensorData> offPeakData = service.getOffPeakTimeData(rangeStart, rangeEnd);

    // 6) Verify
    assertTrue(offPeakData.contains(dataOffPeakTuesday), "Should contain Tuesday 06:00 (off-peak)");
    assertTrue(offPeakData.contains(dataOffPeakSunday), "Should contain Sunday 14:00 (off-peak)");
    assertFalse(offPeakData.contains(dataPeakTuesday), "Should not contain Tuesday 10:00 (peak)");
    assertFalse(offPeakData.contains(dataPeakFriday), "Should not contain Friday 15:00 (peak)");
  }
}
