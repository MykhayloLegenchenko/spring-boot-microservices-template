package com.example.client.user.user.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

public record UserEvent(@NotNull Type type, @NotNull UserDtoEx user, @Nullable Set<String> roles) {
  public static final String TOPIC = "user";

  public UserEvent {
    if (type == Type.SET_ROLES) {
      Assert.notNull(roles, "roles must not be null");
    } else {
      Assert.isNull(roles, "roles must not be null");
    }
  }

  public UserEvent(Type type, UserDtoEx user) {
    this(type, user, null);
  }

  public enum Type {
    REGISTER,
    UPDATE,
    ENABLE,
    DISABLE,
    DELETE,
    SET_ROLES,
  }
}
