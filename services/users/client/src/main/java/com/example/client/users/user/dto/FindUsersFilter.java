package com.example.client.users.user.dto;

import org.jspecify.annotations.Nullable;

public interface FindUsersFilter {
  @Nullable String search();

  @Nullable Boolean enabled();

  @Nullable Boolean deleted();
}
