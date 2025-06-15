package com.example.client.users.user.dto;

import java.time.Instant;
import java.util.UUID;

public interface UserData {
  UUID uuid();

  String email();

  String firstName();

  String lastName();

  Instant createdAt();
}
