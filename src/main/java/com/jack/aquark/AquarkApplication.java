package com.jack.aquark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableKafka
public class AquarkApplication {

  public static void main(String[] args) {
    SpringApplication.run(AquarkApplication.class, args);
  }
}
