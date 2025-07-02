package com.example.service.user.configuration;

import static org.awaitility.Awaitility.await;

import com.example.client.user.role.dto.RoleEvent;
import com.example.client.user.user.dto.UserEvent;
import com.example.common.security.jwt.JwtTokenService;
import com.example.service.user.UserServiceApplication;
import com.example.test.web.client.blocking.MockRestClientService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = UserServiceApplication.class)
@Import({MockRestClientService.class, JwtTokenService.class})
@EmbeddedKafka(topics = {RoleEvent.TOPIC, UserEvent.TOPIC})
public class AbstractIntegrationTest {
  @Autowired protected TestKafkaListener testKafkaListener;

  @BeforeEach
  void setUp(@Autowired KafkaListenerEndpointRegistry registry) {
    testKafkaListener.clear();
    await().atMost(30, TimeUnit.SECONDS).until(() -> testKafkaListener.isRunning(registry));
  }
}
