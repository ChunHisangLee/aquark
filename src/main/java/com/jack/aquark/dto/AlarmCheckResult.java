package com.jack.aquark.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlarmCheckResult {
  private int alarmCount;
  private List<AlarmDetail> alarmDetails;
  private String message;
}
