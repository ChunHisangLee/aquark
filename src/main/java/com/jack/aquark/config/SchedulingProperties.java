package com.jack.aquark.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "scheduling")
@Data
public class SchedulingProperties {
  private String cron;
  private int intervalMinutes;
}
