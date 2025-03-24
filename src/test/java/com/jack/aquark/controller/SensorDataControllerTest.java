package com.jack.aquark.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.SensorDataService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SensorDataController.class)
class SensorDataControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private SensorDataService sensorDataService;

  @MockBean private AggregationService aggregationService;

  @Test
  void testSearchSensorData_Success() throws Exception {
    // Prepare test data for the /search endpoint.
    SensorData sensorData = new SensorData();
    sensorData.setStationId("station1");
    sensorData.setObsTime(LocalDateTime.of(2025, 3, 11, 16, 0, 0));
    sensorData.setCsq("csq1");
    List<SensorData> sensorDataList = Collections.singletonList(sensorData);

    when(aggregationService.getSensorDataByTimeRange(
            any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(sensorDataList);

    mockMvc
        .perform(
            get("/api/sensor/search")
                .param("start", "2025-03-11 15:00:00")
                .param("end", "2025-03-11 23:00:00")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        // Check inside the "data" field.
        .andExpect(jsonPath("$.data[0].stationId").value("station1"))
        .andExpect(jsonPath("$.data[0].csq").value("csq1"));
  }

  @Test
  void testGetHourlyStats_Success() throws Exception {
    HourlyAggregation agg = new HourlyAggregation();
    agg.setStationId("station1");
    List<HourlyAggregation> list = Collections.singletonList(agg);

    when(sensorDataService.getHourlyAverage(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(list);

    mockMvc
        .perform(
            get("/api/sensor/statistics/hourly")
                .param("start", "2025-03-11 15:00:00")
                .param("end", "2025-03-11 23:00:00")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        // Assert that the aggregation is wrapped inside "data".
        .andExpect(jsonPath("$.data[0].stationId").value("station1"));
  }

  @Test
  void testGetPeakData_Success() throws Exception {
    SensorData sensorData = new SensorData();
    sensorData.setStationId("station1");
    sensorData.setObsTime(LocalDateTime.of(2025, 3, 11, 16, 0, 0)); // Assume peak time.
    sensorData.setCsq("csq1");
    List<SensorData> list = Collections.singletonList(sensorData);

    when(sensorDataService.getPeakTimeData(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(list);

    mockMvc
        .perform(
            get("/api/sensor/peak")
                .param("start", "2025-03-11 15:00:00")
                .param("end", "2025-03-11 23:00:00")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        // Expect the result in "data".
        .andExpect(jsonPath("$.data[0].stationId").value("station1"));
  }

  @Test
  void testGetOffPeakData_Success() throws Exception {
    SensorData sensorData = new SensorData();
    sensorData.setStationId("station2");
    sensorData.setObsTime(LocalDateTime.of(2025, 3, 11, 12, 0, 0)); // Assume off-peak time.
    sensorData.setCsq("csq2");
    List<SensorData> list = Collections.singletonList(sensorData);

    when(sensorDataService.getOffPeakTimeData(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(list);

    mockMvc
        .perform(
            get("/api/sensor/off-peak")
                .param("start", "2025-03-11 15:00:00")
                .param("end", "2025-03-11 23:00:00")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        // Assert that the off-peak data is under "data".
        .andExpect(jsonPath("$.data[0].stationId").value("station2"));
  }
}
