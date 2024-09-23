package com.example.client.users.user.dto;

import org.springframework.lang.Nullable;

public interface FindUsersFilter {
  @Nullable
  String search();

  @Nullable
  Boolean enabled();

  @Nullable
  Boolean deleted();
}
