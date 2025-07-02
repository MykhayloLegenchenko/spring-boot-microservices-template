package com.example.client.user.role.dto;

import jakarta.validation.constraints.NotNull;

public record RoleEvent(@NotNull Type type, @NotNull RoleDto role) {
  public static final String TOPIC = "role";

  public enum Type {
    CREATE,
    UPDATE,
    DELETE,
  }
}
