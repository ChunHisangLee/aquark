package com.jack.aquark.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RedisConfig {
  // Spring Boot auto-configures Redis if you specify
  // spring.redis.host, spring.redis.port, etc.
  // You may define a CacheManager bean if you want customization.
}
