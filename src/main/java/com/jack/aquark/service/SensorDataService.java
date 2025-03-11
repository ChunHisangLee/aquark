package com.jack.aquark.service;

import com.jack.aquark.dto.RawDataWrapper;
import com.jack.aquark.dto.Summaries;
import java.time.LocalDateTime;

public interface SensorDataService {
  void saveRawData(RawDataWrapper wrapper);

  Summaries getSummaries(LocalDateTime start, LocalDateTime end);
}
