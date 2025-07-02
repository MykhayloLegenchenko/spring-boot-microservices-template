package com.example.client.user.user.dto;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public interface UserData {
  UUID uuid();

  String email();

  String firstName();

  @Nullable String lastName();

  Instant createdAt();

  Instant updatedAt();
}
