package com.jack.aquark.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.aquark.constant.MessagesConstants;
import com.jack.aquark.entity.AlarmThreshold;
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

    when(alarmThresholdService.getThreshold(eq("240627"), eq("31"), eq("v1")))
        .thenReturn(threshold);

    mockMvc
        .perform(
            get("/api/alarm")
                .param("stationId", "240627")
                .param("csq", "31")
                .param("parameter", "v1"))
        .andExpect(status().isOk())
        // Since the controller wraps a single object in a list, access the first element with $[0]
        .andExpect(jsonPath("$[0].stationId").value("240627"))
        .andExpect(jsonPath("$[0].csq").value("31"))
        .andExpect(jsonPath("$[0].parameter").value("v1"))
        // Expect numeric value 100.0 (JSON numbers are not formatted as strings)
        .andExpect(jsonPath("$[0].thresholdValue").value(100.0));
  }

  @Test
  void testGetThreshold_NotFound() throws Exception {
    when(alarmThresholdService.getThreshold(eq("240627"), eq("31"), eq("v1"))).thenReturn(null);

    mockMvc
        .perform(
            get("/api/alarm")
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

    when(alarmThresholdService.updateThreshold(any(AlarmThreshold.class))).thenReturn(true);

    mockMvc
        .perform(
            post("/api/alarm/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(threshold)))
        .andExpect(status().isOk())
        // Updated JSON keys: statusCode and statusMsg
        .andExpect(jsonPath("$.statusCode").value(MessagesConstants.STATUS_200))
        .andExpect(jsonPath("$.statusMsg").value("Request processed successfully."));
  }

  @Test
  void testUpdateThreshold_Failure() throws Exception {
    AlarmThreshold threshold = new AlarmThreshold();
    threshold.setStationId("240627");
    threshold.setCsq("31");
    threshold.setParameter("v1");
    threshold.setThresholdValue(new BigDecimal("150.00"));

    when(alarmThresholdService.updateThreshold(any(AlarmThreshold.class))).thenReturn(false);

    mockMvc
        .perform(
            post("/api/alarm/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(threshold)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.statusCode").value(MessagesConstants.STATUS_409))
        .andExpect(
            jsonPath("$.statusMsg")
                .value("Could not update threshold. Possibly a conflict or missing data."));
  }
}
