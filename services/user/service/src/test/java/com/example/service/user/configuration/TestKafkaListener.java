package com.example.service.user.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.client.user.role.dto.RoleEvent;
import com.example.client.user.user.dto.UserEvent;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
public class TestKafkaListener {
  private final LinkedBlockingQueue<ConsumerRecord<String, RoleEvent>> roleEvents =
      new LinkedBlockingQueue<>();
  private final LinkedBlockingQueue<ConsumerRecord<String, UserEvent>> userEvents =
      new LinkedBlockingQueue<>();

  public boolean isRunning(KafkaListenerEndpointRegistry registry) {
    return isContainerRunning(registry, "test-role") && isContainerRunning(registry, "test-user");
  }

  @SneakyThrows
  public ConsumerRecord<String, RoleEvent> getRoleEvent() {
    var event = roleEvents.poll(10, TimeUnit.SECONDS);
    assertThat(event).isNotNull();

    return event;
  }

  @SneakyThrows
  public ConsumerRecord<String, UserEvent> getUserEvent() {
    var event = userEvents.poll(10, TimeUnit.SECONDS);
    assertThat(event).isNotNull();

    return event;
  }

  public void clear() {
    roleEvents.clear();
    userEvents.clear();
  }

  @KafkaListener(topics = RoleEvent.TOPIC, groupId = "test", id = "test-role")
  public void roleEventListener(ConsumerRecord<String, RoleEvent> data) {
    roleEvents.add(data);
  }

  @KafkaListener(topics = UserEvent.TOPIC, groupId = "test", id = "test-user")
  public void userEventListener(ConsumerRecord<String, UserEvent> data) {
    System.out.println(getClass().getSimpleName() + "\nEvent: " + data);
    userEvents.add(data);
  }

  private boolean isContainerRunning(KafkaListenerEndpointRegistry registry, String name) {
    var container = registry.getListenerContainer(name);
    return container != null
        && container.isRunning()
        && !((ConcurrentMessageListenerContainer<?, ?>) container)
            .getAssignedPartitions()
            .isEmpty();
  }
}
