package com.jack.aquark.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.SensorDataService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SensorDataController.class)
class SensorDataControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AggregationService aggregationService;

  @MockBean private SensorDataService sensorDataService;

  @Test
  void testSearchSensorData_Success() throws Exception {
    // given
    SensorData mockData = new SensorData();
    mockData.setStationId("testStation");
    mockData.setCsq("31");
    mockData.setObsTime(LocalDateTime.of(2025, 3, 11, 15, 0));

    Mockito.when(aggregationService.getSensorDataByTimeRange(any(), any()))
        .thenReturn(List.of(mockData));

    // when & then
    mockMvc
        .perform(
            get("/api/sensor/search").param("start", "2025-03-11 15").param("end", "2025-03-11 23"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].stationId").value("testStation"))
        .andExpect(jsonPath("$[0].csq").value("31"));

    verify(aggregationService)
        .getSensorDataByTimeRange(
            LocalDateTime.of(2025, 3, 11, 15, 0), LocalDateTime.of(2025, 3, 11, 23, 0));
  }

  @Test
  void testSearchSensorData_Failure() throws Exception {
    // given
    Mockito.when(aggregationService.getSensorDataByTimeRange(any(), any()))
        .thenThrow(new RuntimeException("DB error"));

    // when & then
    mockMvc
        .perform(
            get("/api/sensor/search")
                .param("start", "2025-03-11 15")
                .param("end", "2025-03-11 23")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void testGetHourlyStats_Success() throws Exception {
    // given
    HourlyAggregation agg = new HourlyAggregation();
    // Set properties on the aggregation as needed, e.g.:
    // agg.setSomeProperty(...);
    Mockito.when(sensorDataService.getHourlyAverage(any(), any())).thenReturn(List.of(agg));

    // when & then
    mockMvc
        .perform(
            get("/api/sensor/statistics/hourly")
                .param("start", "2025-03-11 15")
                .param("end", "2025-03-11 23"))
        .andExpect(status().isOk());

    verify(sensorDataService)
        .getHourlyAverage(
            LocalDateTime.of(2025, 3, 11, 15, 0), LocalDateTime.of(2025, 3, 11, 23, 0));
  }

  @Test
  void testGetHourlyStats_Failure() throws Exception {
    // given
    Mockito.when(sensorDataService.getHourlyAverage(any(), any()))
        .thenThrow(new RuntimeException("Service error"));

    // when & then
    mockMvc
        .perform(
            get("/api/sensor/statistics/hourly")
                .param("start", "2025-03-11 15")
                .param("end", "2025-03-11 23")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void testGetPeakData_Success() throws Exception {
    // given
    SensorData peakData = new SensorData();
    peakData.setStationId("peakStation");
    peakData.setCsq("45");
    peakData.setObsTime(LocalDateTime.of(2025, 3, 11, 18, 0));

    Mockito.when(sensorDataService.getPeakTimeData(any(), any())).thenReturn(List.of(peakData));

    // when & then
    mockMvc
        .perform(
            get("/api/sensor/peak").param("start", "2025-03-11 15").param("end", "2025-03-11 23"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].stationId").value("peakStation"))
        .andExpect(jsonPath("$[0].csq").value("45"));

    verify(sensorDataService)
        .getPeakTimeData(
            LocalDateTime.of(2025, 3, 11, 15, 0), LocalDateTime.of(2025, 3, 11, 23, 0));
  }

  @Test
  void testGetPeakData_Failure() throws Exception {
    // given
    Mockito.when(sensorDataService.getPeakTimeData(any(), any()))
        .thenThrow(new RuntimeException("Peak data error"));

    // when & then
    mockMvc
        .perform(
            get("/api/sensor/peak")
                .param("start", "2025-03-11 15")
                .param("end", "2025-03-11 23")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void testGetOffPeakData_Success() throws Exception {
    // given
    SensorData offPeakData = new SensorData();
    offPeakData.setStationId("offPeakStation");
    offPeakData.setCsq("20");
    offPeakData.setObsTime(LocalDateTime.of(2025, 3, 11, 20, 0));

    Mockito.when(sensorDataService.getOffPeakTimeData(any(), any()))
        .thenReturn(List.of(offPeakData));

    // when & then
    mockMvc
        .perform(
            get("/api/sensor/off-peak")
                .param("start", "2025-03-11 15")
                .param("end", "2025-03-11 23"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].stationId").value("offPeakStation"))
        .andExpect(jsonPath("$[0].csq").value("20"));

    verify(sensorDataService)
        .getOffPeakTimeData(
            LocalDateTime.of(2025, 3, 11, 15, 0), LocalDateTime.of(2025, 3, 11, 23, 0));
  }

  @Test
  void testGetOffPeakData_Failure() throws Exception {
    // given
    Mockito.when(sensorDataService.getOffPeakTimeData(any(), any()))
        .thenThrow(new RuntimeException("Off-peak data error"));

    // when & then
    mockMvc
        .perform(
            get("/api/sensor/off-peak")
                .param("start", "2025-03-11 15")
                .param("end", "2025-03-11 23")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }
}
