package com.example.autoconfigure.web.reactive.error;

import com.example.common.error.ErrorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Auto-configuration of exception handling in reactive REST controllers. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ReactiveResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
  private final MessageSource messageSource;

  /** Handles unhandled exceptions in reactive REST controllers. */
  @ExceptionHandler
  public Mono<ResponseEntity<Object>> handleUnhandledException(
      Exception ex, ServerWebExchange exchange) {
    return handleExceptionInternal(
        ex, null, ErrorUtils.getHeaders(ex), ErrorUtils.getStatusCode(ex), exchange);
  }

  /**
   * @implSpec Logs the exception and updates the response entity with additional details.
   */
  @Override
  protected Mono<ResponseEntity<Object>> handleExceptionInternal(
      Exception ex,
      @Nullable Object body,
      @Nullable HttpHeaders headers,
      HttpStatusCode status,
      ServerWebExchange exchange) {

    if (log.isDebugEnabled()) {
      log.debug("Response error", ex);
    }

    //noinspection DataFlowIssue
    return super.handleExceptionInternal(ex, body, headers, status, exchange)
        .mapNotNull(
            entity ->
                ErrorUtils.updateResponseEntity(
                    entity, ex, messageSource, exchange.getLocaleContext().getLocale()));
  }
}
