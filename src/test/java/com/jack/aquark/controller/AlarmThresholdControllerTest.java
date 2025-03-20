package com.jack.aquark.controller;

import static org.mockito.ArgumentMatchers.any;
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

    when(alarmThresholdService.getThreshold("240627", "31", "v1")).thenReturn(threshold);

    mockMvc
        .perform(
            get("/api/alarm/get")
                .param("stationId", "240627")
                .param("csq", "31")
                .param("parameter", "v1"))
        .andExpect(status().isOk())
        // The controller wraps the list inside a "data" field.
        .andExpect(jsonPath("$.data[0].stationId").value("240627"))
        .andExpect(jsonPath("$.data[0].csq").value("31"))
        .andExpect(jsonPath("$.data[0].parameter").value("v1"))
        // Expect numeric value 100.0 (JSON numbers are not formatted as strings)
        .andExpect(jsonPath("$.data[0].thresholdValue").value(100.0));
  }

  @Test
  void testGetThreshold_NotFound() throws Exception {
    when(alarmThresholdService.getThreshold("240627", "31", "v1")).thenReturn(null);

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

    when(alarmThresholdService.updateThreshold(any(AlarmThreshold.class))).thenReturn(true);

    mockMvc
        .perform(
            post("/api/alarm/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(threshold)))
        .andExpect(status().isOk())
        // The success response is wrapped under "data".
        .andExpect(jsonPath("$.data.statusCode").value(MessagesConstants.STATUS_200))
        .andExpect(jsonPath("$.data.statusMsg").value("Request processed successfully."));
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
        // Since it's an error response, check under the "error" field instead of "data".
        .andExpect(jsonPath("$.error.errorCode").value(MessagesConstants.STATUS_409))
        .andExpect(
            jsonPath("$.error.errorMessage")
                .value("Could not update threshold. Possibly a conflict or missing data."));
  }
}
