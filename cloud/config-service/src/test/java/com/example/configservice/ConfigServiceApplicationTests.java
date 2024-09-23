package com.example.configservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = ConfigServiceApplication.class)
class ConfigServiceApplicationTests {

  @Test
  @SuppressWarnings("EmptyMethod")
  void contextLoads() {}
}
