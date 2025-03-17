package com.jack.aquark.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    log.info(
        "Initializing RedisConnectionFactory with host: {} port: {} password: {}",
        redisHost,
        redisPort,
        redisPassword);

    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    config.setHostName(redisHost);
    config.setPort(redisPort);

    if (!redisPassword.isEmpty()) {
      config.setPassword(redisPassword);
    }

    return new LettuceConnectionFactory(config);
  }

  /** Configures CacheManager to use a Jackson-based JSON serializer for caching. */
  @Bean
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // Create an ObjectMapper with any custom modules/config as needed
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    // Use the constructor (mapper, type) to avoid the deprecated setObjectMapper(...)
    Jackson2JsonRedisSerializer<Object> jsonSerializer =
        new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

    RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(60)) // 60-minute TTL
            .disableCachingNullValues()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

    return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Use String for keys
    StringRedisSerializer keySerializer = new StringRedisSerializer();
    template.setKeySerializer(keySerializer);
    template.setHashKeySerializer(keySerializer);

    // Use Jackson for values
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    Jackson2JsonRedisSerializer<Object> jsonSerializer =
        new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

    template.setValueSerializer(jsonSerializer);
    template.setHashValueSerializer(jsonSerializer);

    template.afterPropertiesSet();
    return template;
  }
}
