package com.example.common.error.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Response status exception for {@link HttpStatus#INTERNAL_SERVER_ERROR} responses. */
public class InternalServerErrorException extends ResponseStatusException {
  public InternalServerErrorException() {
    this(null);
  }

  public InternalServerErrorException(@Nullable String reason) {
    this(reason, null);
  }

  public InternalServerErrorException(@Nullable String reason, @Nullable Throwable cause) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, reason, cause);
  }
}
