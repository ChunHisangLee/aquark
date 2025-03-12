package com.jack.aquark.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "scheduling")
@Data
public class SchedulingProperties {
    private String cron;
    private int intervalMinutes;
}
