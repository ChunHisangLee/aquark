package com.jack.aquark.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.aquark.dto.RawDataItemDto;
import com.jack.aquark.dto.RawDataWrapperDto;
import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.entity.TempSensorData;
import com.jack.aquark.repository.DailyAggregationRepository;
import com.jack.aquark.repository.HourlyAggregationRepository;
import com.jack.aquark.repository.SensorDataRepository;
import com.jack.aquark.repository.TempSensorDataRepository;
import com.jack.aquark.service.AggregationService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class SensorDataServiceImplTest {

  @Mock private SensorDataRepository sensorDataRepository;
  @Mock private TempSensorDataRepository tempSensorDataRepository;
  @Mock private HourlyAggregationRepository hourlyAggregationRepository;
  @Mock private DailyAggregationRepository dailyAggregationRepository;
  @Mock private AggregationService aggregationService;

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

    RawDataItemDto.Sensor sensor = new RawDataItemDto.Sensor();
    RawDataItemDto.Volt volt = new RawDataItemDto.Volt();
    volt.setV1(BigDecimal.ONE);
    volt.setV2(BigDecimal.valueOf(2.0));
    sensor.setVolt(volt);
    item.setSensor(sensor);

    RawDataWrapperDto wrapper = new RawDataWrapperDto();
    wrapper.setRaw(Collections.singletonList(item));

    // 2) Real service, then partial mock
    SensorDataServiceImpl realService =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            dailyAggregationRepository,
            aggregationService,
            new ObjectMapper());

    SensorDataServiceImpl partialMock = spy(realService);

    // 3) Stub fetchRawDataFromUrl to return the wrapper
    doReturn(wrapper).when(partialMock).fetchRawDataFromUrl(anyString());

    // 4) Stub duplicate-check => false
    when(sensorDataRepository.existsByStationIdAndObsTimeAndCsq(
            eq("station1"), any(LocalDateTime.class), eq("csq1")))
        .thenReturn(false);

    // 5) Execute
    partialMock.fetchAndSaveSensorData("dummyurl");

    // 6) Verify saves
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

    // 2) Service + partial mock
    SensorDataServiceImpl realService =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            dailyAggregationRepository,
            aggregationService,
            new ObjectMapper());

    SensorDataServiceImpl partialMock = spy(realService);

    doReturn(wrapper).when(partialMock).fetchRawDataFromUrl(anyString());

    // Mark as duplicate
    when(sensorDataRepository.existsByStationIdAndObsTimeAndCsq(
            eq("station1"), any(LocalDateTime.class), eq("csq1")))
        .thenReturn(true);

    // 3) Execute
    partialMock.fetchAndSaveSensorData("dummyurl");

    // 4) Should skip saves
    verify(sensorDataRepository, never()).save(any(SensorData.class));
    verify(tempSensorDataRepository, never()).save(any(TempSensorData.class));
  }

  @Test
  void testGetHourlyAverage() {
    // 1) Setup
    LocalDateTime start = LocalDateTime.of(2025, 3, 15, 0, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 16, 23, 59, 59);

    // 2) Fake aggregator result
    HourlyAggregation agg = new HourlyAggregation();
    List<HourlyAggregation> fakeList = Collections.singletonList(agg);

    // 3) Stub repo
    when(hourlyAggregationRepository.findByObsDateBetween(start.toLocalDate(), end.toLocalDate()))
        .thenReturn(fakeList);

    // 4) Service
    SensorDataServiceImpl service =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            dailyAggregationRepository,
            aggregationService,
            new ObjectMapper());

    // 5) Execute
    List<HourlyAggregation> result = service.getHourlyAverage(start, end);

    // 6) Verify
    assertEquals(1, result.size());
    verify(hourlyAggregationRepository, times(1))
        .findByObsDateBetween(start.toLocalDate(), end.toLocalDate());
  }

  @Test
  void testGetDailyAverage() {
    // 1) Setup
    LocalDateTime start = LocalDateTime.of(2025, 3, 10, 0, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 12, 23, 59, 59);

    DailyAggregation dailyAgg = new DailyAggregation();
    List<DailyAggregation> fakeList = Collections.singletonList(dailyAgg);

    // 2) Stub daily repo
    when(dailyAggregationRepository.findByObsDateBetween(start.toLocalDate(), end.toLocalDate()))
        .thenReturn(fakeList);

    // 3) Service
    SensorDataServiceImpl service =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            dailyAggregationRepository,
            aggregationService,
            new ObjectMapper());

    // 4) Execute
    List<DailyAggregation> result = service.getDailyAverage(start, end);

    // 5) Verify
    assertEquals(1, result.size());
    verify(dailyAggregationRepository, times(1))
        .findByObsDateBetween(start.toLocalDate(), end.toLocalDate());
  }

  @Test
  void testGetPeakTimeData() {
    // 1) Sample data
    SensorData dataPeakMonday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 17, 8, 0, 0)) // Monday 08:00 => peak
            .build();
    SensorData dataOffPeakMonday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 17, 6, 0, 0)) // Monday 06:00 => off-peak
            .build();
    SensorData dataPeakThursday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 20, 12, 0, 0)) // Thursday => peak
            .build();
    SensorData dataOffPeakSaturday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 22, 10, 0, 0)) // Saturday => off-peak
            .build();

    List<SensorData> allData =
        Arrays.asList(dataPeakMonday, dataOffPeakMonday, dataPeakThursday, dataOffPeakSaturday);

    // 2) Range
    LocalDateTime rangeStart = LocalDateTime.of(2025, 3, 15, 0, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 3, 25, 0, 0, 0);

    // 3) Stub aggregator
    when(aggregationService.getSensorDataByTimeRange(rangeStart, rangeEnd)).thenReturn(allData);

    // 4) Service
    SensorDataServiceImpl service =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            dailyAggregationRepository,
            aggregationService,
            new ObjectMapper());

    // 5) Execute
    List<SensorData> peakData = service.getPeakTimeData(rangeStart, rangeEnd);

    // 6) Verify
    assertTrue(peakData.contains(dataPeakMonday), "Should contain Monday 08:00 (peak)");
    assertTrue(peakData.contains(dataPeakThursday), "Should contain Thursday 12:00 (peak)");
    assertFalse(peakData.contains(dataOffPeakMonday), "Should NOT contain Monday 06:00 (off-peak)");
    assertFalse(peakData.contains(dataOffPeakSaturday), "Should NOT contain Saturday (off-peak)");
  }

  @Test
  void testGetOffPeakTimeData() {
    // 1) Sample data
    SensorData dataPeakTuesday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 18, 10, 0, 0)) // Tuesday => peak
            .build();
    SensorData dataOffPeakTuesday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 18, 6, 0, 0)) // Tuesday => off-peak
            .build();
    SensorData dataPeakFriday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 21, 15, 0, 0)) // Friday => peak
            .build();
    SensorData dataOffPeakSunday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 23, 14, 0, 0)) // Sunday => off-peak
            .build();

    List<SensorData> allData =
        Arrays.asList(dataPeakTuesday, dataOffPeakTuesday, dataPeakFriday, dataOffPeakSunday);

    // 2) Range
    LocalDateTime rangeStart = LocalDateTime.of(2025, 3, 15, 0, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 3, 25, 0, 0, 0);

    // 3) Stub aggregator
    when(aggregationService.getSensorDataByTimeRange(rangeStart, rangeEnd)).thenReturn(allData);

    // 4) Service
    SensorDataServiceImpl service =
        new SensorDataServiceImpl(
            sensorDataRepository,
            tempSensorDataRepository,
            hourlyAggregationRepository,
            dailyAggregationRepository,
            aggregationService,
            new ObjectMapper());

    // 5) Execute
    List<SensorData> offPeakData = service.getOffPeakTimeData(rangeStart, rangeEnd);

    // 6) Verify
    assertTrue(offPeakData.contains(dataOffPeakTuesday), "Should contain Tuesday 06:00 (off-peak)");
    assertTrue(offPeakData.contains(dataOffPeakSunday), "Should contain Sunday 14:00 (off-peak)");
    assertFalse(offPeakData.contains(dataPeakTuesday), "Should NOT contain Tuesday 10:00 (peak)");
    assertFalse(offPeakData.contains(dataPeakFriday), "Should NOT contain Friday 15:00 (peak)");
  }
}
