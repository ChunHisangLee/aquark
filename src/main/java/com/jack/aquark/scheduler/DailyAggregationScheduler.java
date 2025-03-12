package com.jack.aquark.scheduler;

import com.jack.aquark.service.DailyAggregationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DailyAggregationScheduler {

    private final DailyAggregationService dailyAggregationService;

    // Example: Run daily at 02:00
    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateData() {
        log.info("Starting daily data aggregation...");
        dailyAggregationService.aggregateDailyData();
        log.info("Daily data aggregation complete.");
    }
}
