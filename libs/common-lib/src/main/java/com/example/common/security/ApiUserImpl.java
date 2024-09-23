package com.example.common.security;

import java.util.UUID;

/** Implementation class for {@link ApiUser}. */
record ApiUserImpl(UUID uuid) implements ApiUser {

  @Override
  public String getName() {
    return uuid.toString();
  }
}
