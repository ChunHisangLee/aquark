package com.jack.aquark.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.exception.ThresholdNotFoundException;
import com.jack.aquark.service.AlarmThresholdService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AlarmThresholdController.class)
class AlarmThresholdControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AlarmThresholdService alarmThresholdService;

  @Test
  void testGetThreshold_Success() throws Exception {
    // Prepare a sample threshold with thresholdValue 100.0
    AlarmThreshold threshold = new AlarmThreshold();
    threshold.setStationId("240627");
    threshold.setCsq("31");
    threshold.setParameter("v1");
    threshold.setThresholdValue(new BigDecimal("100.0"));

    when(alarmThresholdService.getThreshold("240627", "31", "v1")).thenReturn(threshold);

    mockMvc
        .perform(
            get("/api/alarm/get")
                .param("stationId", "240627")
                .param("csq", "31")
                .param("parameter", "v1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].stationId").value("240627"))
        .andExpect(jsonPath("$.data[0].csq").value("31"))
        .andExpect(jsonPath("$.data[0].parameter").value("v1"))
        // 100.0 will be returned as a number
        .andExpect(jsonPath("$.data[0].thresholdValue").value(100.0));
  }

  @Test
  void testGetThreshold_NotFound() throws Exception {
    // Simulate service throwing ThresholdNotFoundException
    when(alarmThresholdService.getThreshold("240627", "31", "v1"))
        .thenThrow(
            new ThresholdNotFoundException(
                "Threshold not found for station 240627, csq 31, parameter v1"));

    mockMvc
        .perform(
            get("/api/alarm/get")
                .param("stationId", "240627")
                .param("csq", "31")
                .param("parameter", "v1"))
        .andExpect(status().isNotFound());
  }

  @Test
  void testUpdateThreshold_Success() throws Exception {
    AlarmThreshold threshold = new AlarmThreshold();
    threshold.setStationId("240627");
    threshold.setCsq("31");
    threshold.setParameter("v1");
    threshold.setThresholdValue(new BigDecimal("150.00"));

    // Simulate service returning the updated threshold object
    when(alarmThresholdService.updateThreshold(any(AlarmThreshold.class))).thenReturn(threshold);

    mockMvc
        .perform(
            put("/api/alarm/update") // Changed from POST to PUT
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(threshold)))
        .andExpect(status().isOk())
        // Validate response data fields
        .andExpect(jsonPath("$.data.stationId").value("240627"))
        .andExpect(jsonPath("$.data.csq").value("31"))
        .andExpect(jsonPath("$.data.parameter").value("v1"))
        .andExpect(jsonPath("$.data.thresholdValue").value(150.00));
  }

  @Test
  void testUpdateThreshold_Failure() throws Exception {
    AlarmThreshold threshold = new AlarmThreshold();
    threshold.setStationId("240627");
    threshold.setCsq("31");
    threshold.setParameter("v1");
    threshold.setThresholdValue(new BigDecimal("150.00"));

    // Simulate service throwing an exception to indicate update failure
    when(alarmThresholdService.updateThreshold(any(AlarmThreshold.class)))
        .thenThrow(new RuntimeException("Update failed"));

    mockMvc
        .perform(
            put("/api/alarm/update") // Changed from POST to PUT
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(threshold)))
        .andExpect(status().isInternalServerError());
  }
}
