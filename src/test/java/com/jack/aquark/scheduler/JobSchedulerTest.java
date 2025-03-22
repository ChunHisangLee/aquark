package com.jack.aquark.scheduler;

import static org.mockito.Mockito.*;

import com.jack.aquark.config.ApiUrlProperties;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.SensorDataService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JobSchedulerTest {

    private SensorDataService sensorDataService;
  private AggregationService aggregationService;
  private JobScheduler jobScheduler;

  @BeforeEach
  public void setup() {
    // Mock the dependent beans
      ApiUrlProperties apiUrlProperties = mock(ApiUrlProperties.class);
    sensorDataService = mock(SensorDataService.class);
    aggregationService = mock(AggregationService.class);

    // Simulate a scenario where there are two API URLs
    when(apiUrlProperties.getUrls())
        .thenReturn(Arrays.asList("http://example.com/api1", "http://example.com/api2"));

    // Initialize the JobScheduler with the mocks
    jobScheduler = new JobScheduler(apiUrlProperties, sensorDataService, aggregationService);
  }

  @Test
  public void testFetchAndAggregate() {
    // Directly invoke the scheduled method (without waiting for the cron trigger)
    jobScheduler.fetchAndAggregate();

    // Verify that for each API URL, sensorDataService.fetchAndSaveSensorData() is called exactly
    // once
    verify(sensorDataService, times(1)).fetchAndSaveSensorData("http://example.com/api1");
    verify(sensorDataService, times(1)).fetchAndSaveSensorData("http://example.com/api2");

    // Verify that the aggregation service is called once after fetching data
    verify(aggregationService, times(1)).processTempDataForAggregations();
  }
}
