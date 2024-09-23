package com.example.common.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

/** Response status exception for wrong client responses. */
@Getter
public class ClientResponseException extends ResponseStatusException {
  private final HttpStatusCode originalStatus;
  private final Object error;

  public ClientResponseException(HttpStatusCode status, Object error) {
    this(status, error, null);
  }

  public ClientResponseException(HttpStatusCode status, Object error, @Nullable Throwable cause) {
    super(HttpStatus.BAD_GATEWAY, null, cause);

    this.originalStatus = status;
    this.error = error;
  }

  @Override
  public String getMessage() {
    super.getMessage();
    var reason = getReason();
    return originalStatus + (reason != null ? " \"" + reason + "\"" : "") + " " + error;
  }
}
