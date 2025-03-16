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

public class SensorDataServiceImplTest {

  @Mock private SensorDataRepository sensorDataRepository;

  @Mock private TempSensorDataRepository tempSensorDataRepository;

  @Mock private HourlyAggregationRepository hourlyAggregationRepository;

  @Mock private AggregationService aggregationService;

  @InjectMocks private SensorDataServiceImpl sensorDataService;

  private AutoCloseable closeable;

  @BeforeEach
  public void setup() {
    closeable = MockitoAnnotations.openMocks(this);
    // sensorDataService is auto-created with the @InjectMocks annotation.
  }

  @AfterEach
  public void tearDown() throws Exception {
    closeable.close();
  }

  @Test
  public void testFetchAndSaveSensorData_withValidData() {
    // Create a dummy RawDataItemDto with required fields.
    RawDataItemDto item = new RawDataItemDto();
    item.setObsTime("2025-03-16 10:00:00");
    item.setStationId("station1");
    item.setCsq("csq1");
    item.setRainD(BigDecimal.ZERO);

    // Prepare a dummy sensor object with nested volt data.
    RawDataItemDto.Sensor sensor = new RawDataItemDto.Sensor();
    RawDataItemDto.Volt volt = new RawDataItemDto.Volt();
    volt.setV1(BigDecimal.ONE);
    volt.setV2(BigDecimal.valueOf(2.0));
    volt.setV3(BigDecimal.valueOf(3.0));
    volt.setV4(BigDecimal.valueOf(4.0));
    volt.setV5(BigDecimal.valueOf(5.0));
    volt.setV6(BigDecimal.valueOf(6.0));
    volt.setV7(BigDecimal.valueOf(7.0));
    sensor.setVolt(volt);
    // For simplicity, other nested objects (stickTxRh, ultrasonicLevel, waterSpeedAquark) are left
    // null.
    item.setSensor(sensor);

    // Wrap the item in a RawDataWrapperDto.
    RawDataWrapperDto wrapper = new RawDataWrapperDto();
    wrapper.setRaw(Collections.singletonList(item));

    // Spy on the service to override fetchRawDataFromUrl.
    SensorDataServiceImpl spyService = spy(sensorDataService);
    doReturn(wrapper).when(spyService).fetchRawDataFromUrl(anyString());

    // Simulate that no duplicate record exists.
    when(sensorDataRepository.existsByStationIdAndObsTimeAndCsq(
            eq("station1"), any(LocalDateTime.class), eq("csq1")))
        .thenReturn(false);

    spyService.fetchAndSaveSensorData("http://dummyurl");

    // Verify that sensor data and temporary sensor data are saved.
    verify(sensorDataRepository, times(1)).save(any(SensorData.class));
    verify(tempSensorDataRepository, times(1)).save(any(TempSensorData.class));
  }

  @Test
  public void testFetchAndSaveSensorData_withDuplicateData() {
    // Create a fake RawDataItemDto.
    RawDataItemDto item = new RawDataItemDto();
    item.setObsTime("2025-03-16 11:00:00");
    item.setStationId("station1");
    item.setCsq("csq1");
    item.setRainD(BigDecimal.ZERO);
    RawDataItemDto.Sensor sensor = new RawDataItemDto.Sensor();
    item.setSensor(sensor);

    RawDataWrapperDto wrapper = new RawDataWrapperDto();
    wrapper.setRaw(Collections.singletonList(item));

    SensorDataServiceImpl spyService = spy(sensorDataService);
    doReturn(wrapper).when(spyService).fetchRawDataFromUrl(anyString());

    // Simulate that a duplicate record exists.
    when(sensorDataRepository.existsByStationIdAndObsTimeAndCsq(
            eq("station1"), any(LocalDateTime.class), eq("csq1")))
        .thenReturn(true);

    spyService.fetchAndSaveSensorData("http://dummyurl");

    // Verify that save methods are NOT called due to duplicate check.
    verify(sensorDataRepository, never()).save(any(SensorData.class));
    verify(tempSensorDataRepository, never()).save(any(TempSensorData.class));
  }

  @Test
  public void testGetHourlyAverage() {
    LocalDateTime start = LocalDateTime.of(2025, 3, 15, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 16, 23, 59);
    HourlyAggregation agg = new HourlyAggregation();
    List<HourlyAggregation> list = Collections.singletonList(agg);

    when(hourlyAggregationRepository.findByObsDateBetween(
            eq(start.toLocalDate()), eq(end.toLocalDate())))
        .thenReturn(list);

    List<HourlyAggregation> result = sensorDataService.getHourlyAverage(start, end);
    assertEquals(1, result.size());
    verify(hourlyAggregationRepository, times(1))
        .findByObsDateBetween(eq(start.toLocalDate()), eq(end.toLocalDate()));
  }

  @Test
  public void testGetPeakTimeData() {
    // Create sample SensorData records with various observation times.
    SensorData dataPeakMonday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 17, 8, 0)) // Monday at 8:00 (peak)
            .build();
    SensorData dataOffPeakMonday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 17, 6, 0)) // Monday at 6:00 (off peak)
            .build();
    SensorData dataPeakThursday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 20, 12, 0)) // Thursday (always peak)
            .build();
    SensorData dataOffPeakSaturday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 22, 10, 0)) // Saturday (off peak)
            .build();

    List<SensorData> allData =
        Arrays.asList(dataPeakMonday, dataOffPeakMonday, dataPeakThursday, dataOffPeakSaturday);
    when(aggregationService.getSensorDataByTimeRange(
            any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(allData);

    List<SensorData> peakData =
        sensorDataService.getPeakTimeData(LocalDateTime.now(), LocalDateTime.now());

    // Expect only data from Monday at 8:00 and Thursday at 12:00 to be considered peak.
    assertTrue(peakData.contains(dataPeakMonday));
    assertTrue(peakData.contains(dataPeakThursday));
    assertFalse(peakData.contains(dataOffPeakMonday));
    assertFalse(peakData.contains(dataOffPeakSaturday));
  }

  @Test
  public void testGetOffPeakTimeData() {
    // Create sample SensorData records.
    SensorData dataPeakTuesday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 18, 10, 0)) // Tuesday at 10:00 (peak)
            .build();
    SensorData dataOffPeakTuesday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 18, 6, 0)) // Tuesday at 6:00 (off peak)
            .build();
    SensorData dataPeakFriday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 21, 15, 0)) // Friday (always peak)
            .build();
    SensorData dataOffPeakSunday =
        SensorData.builder()
            .obsTime(LocalDateTime.of(2025, 3, 23, 14, 0)) // Sunday (off peak)
            .build();

    List<SensorData> allData =
        Arrays.asList(dataPeakTuesday, dataOffPeakTuesday, dataPeakFriday, dataOffPeakSunday);
    when(aggregationService.getSensorDataByTimeRange(
            any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(allData);

    List<SensorData> offPeakData =
        sensorDataService.getOffPeakTimeData(LocalDateTime.now(), LocalDateTime.now());

    // Expect only Tuesday at 6:00 and Sunday at 14:00 to be off-peak.
    assertTrue(offPeakData.contains(dataOffPeakTuesday));
    assertTrue(offPeakData.contains(dataOffPeakSunday));
    assertFalse(offPeakData.contains(dataPeakTuesday));
    assertFalse(offPeakData.contains(dataPeakFriday));
  }
}
