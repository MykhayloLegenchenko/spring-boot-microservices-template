package com.example.common.error.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Response status exception for {@link HttpStatus#NOT_FOUND} responses. */
public class NotFoundException extends ResponseStatusException {
  public NotFoundException() {
    this(null);
  }

  public NotFoundException(@Nullable String reason) {
    this(reason, null);
  }

  public NotFoundException(@Nullable String reason, @Nullable Throwable cause) {
    super(HttpStatus.NOT_FOUND, reason, cause);
  }
}
