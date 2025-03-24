package com.jack.aquark;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AquarkApplicationTests {
  @Autowired private ApplicationContext context;

  @Test
  void contextLoads() {
    // This ensures the application context is loaded and is not null.
    assertNotNull(context, "ApplicationContext should be loaded");
  }
}
