package com.jack.aquark.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "api")
@Data
public class ApiUrlProperties {
    private List<String> urls;
}
