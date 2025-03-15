package com.jack.aquark.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jack.aquark.constant.MessagesConstants;
import com.jack.aquark.dto.AlarmCheckResult;
import com.jack.aquark.dto.AlarmDetail;
import com.jack.aquark.service.AlarmCheckingService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SensorAlarmController.class)
class SensorAlarmControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AlarmCheckingService alarmCheckingService;

  @Test
  void testCheckSensorAlarms_Success() throws Exception {
    // Prepare a sample alarm detail.
    AlarmDetail detail =
        new AlarmDetail(
            "240627",
            "2023-08-02T12:00:00",
            "31",
            "v1",
            new BigDecimal("200.00"),
            new BigDecimal("150.00"),
            "Alarm triggered for station 240627, parameter v1, csq 31: value 200.00 exceeds threshold 150.00");
    AlarmCheckResult result =
        new AlarmCheckResult(1, List.of(detail), "Alarm check completed. 1 alarms triggered.");

    when(alarmCheckingService.checkSensorAlarms(anyInt())).thenReturn(result);

    mockMvc
        .perform(
            get("/api/alarm/check")
                .param("intervalMinutes", "60")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.alarmCount").value(1))
        .andExpect(jsonPath("$.alarmDetails[0].stationId").value("240627"))
        .andExpect(jsonPath("$.alarmDetails[0].parameter").value("v1"))
        .andExpect(jsonPath("$.alarmDetails[0].sensorValue").value(200.00))
        .andExpect(jsonPath("$.alarmDetails[0].thresholdValue").value(150.00))
        .andExpect(jsonPath("$.message").value("Alarm check completed. 1 alarms triggered."));
  }

  @Test
  void testCheckSensorAlarms_Failure() throws Exception {
    // Simulate an exception thrown by the service.
    when(alarmCheckingService.checkSensorAlarms(anyInt()))
        .thenThrow(new RuntimeException("Service error"));

    mockMvc
        .perform(
            get("/api/alarm/check")
                .param("intervalMinutes", "60")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.statusCode").value(MessagesConstants.STATUS_500))
        .andExpect(jsonPath("$.statusMsg").value("Error fetching alarms statistics"));
  }
}
