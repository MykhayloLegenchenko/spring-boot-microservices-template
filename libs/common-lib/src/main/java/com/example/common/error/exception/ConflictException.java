package com.example.common.error.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

/** Response status exception for {@link HttpStatus#CONFLICT} responses. */
public class ConflictException extends ResponseStatusException {
  public ConflictException() {
    this(null);
  }

  public ConflictException(@Nullable String reason) {
    this(reason, null);
  }

  public ConflictException(@Nullable String reason, @Nullable Throwable cause) {
    super(HttpStatus.CONFLICT, reason, cause);
  }
}
