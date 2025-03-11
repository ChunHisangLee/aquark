package com.jack.aquark.scheduler;

import com.jack.aquark.service.AlarmThresholdService;
import com.jack.aquark.service.SensorDataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DataFetchScheduler {

  private final SensorDataService sensorDataService;
  private final AlarmThresholdService alarmThresholdService;

  // 這裡使用 application.yml 中設定的 cron 表達式
  @Scheduled(cron = "${scheduling.cron}")
  public void fetchDataTask() {
    log.info("開始呼叫外部 API 取得 sensor 資料...");
    // 假設我們從多個 URL 取得資料，這裡以其中一個 URL 為例
    String apiUrl = "https://app.aquark.com.tw/api/raw/Angle2024/240627";
    sensorDataService.fetchAndSaveSensorData(apiUrl);
    log.info("完成資料存檔。");
  }
}
