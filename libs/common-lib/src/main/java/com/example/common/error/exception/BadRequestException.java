package com.example.common.error.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Response status exception for {@link HttpStatus#BAD_REQUEST} responses. */
public class BadRequestException extends ResponseStatusException {
  public BadRequestException() {
    this(null);
  }

  public BadRequestException(@Nullable String reason) {
    this(reason, null);
  }

  public BadRequestException(@Nullable String reason, @Nullable Throwable cause) {
    super(HttpStatus.BAD_REQUEST, reason, cause);
  }
}
