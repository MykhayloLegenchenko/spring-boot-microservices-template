package com.example.common.security;

import java.security.Principal;
import java.util.UUID;

/** The {@code Principal} interface extension that contains the user's UUID. */
public interface ApiUser extends Principal {
  UUID uuid();

  /**
   * Creates a new instance of {@link ApiUser}.
   *
   * @param uuid UUID of the user
   */
  static ApiUser create(UUID uuid) {
    return new ApiUserImpl(uuid);
  }
}
