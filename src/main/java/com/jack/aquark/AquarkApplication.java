package com.jack.aquark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AquarkApplication {

  public static void main(String[] args) {
    SpringApplication.run(AquarkApplication.class, args);
  }
}
