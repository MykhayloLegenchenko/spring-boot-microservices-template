package com.example.autoconfigure.web.servlet.error;

import com.example.common.error.ErrorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Auto-configuration of exception handling in REST controllers. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ServletResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
  private final MessageSource messageSource;

  /** Handles unhandled exceptions in REST controllers. */
  @ExceptionHandler
  @Nullable
  public ResponseEntity<Object> handleUnhandledException(Exception ex, WebRequest request) {
    return handleExceptionInternal(
        ex, null, ErrorUtils.getHeaders(ex), ErrorUtils.getStatusCode(ex), request);
  }

  /**
   * @implSpec Logs the exception and updates the response entity with additional details.
   */
  @Override
  @Nullable
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      @Nullable Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {

    if (log.isDebugEnabled()) {
      log.debug("Response error", ex);
    }

    return ErrorUtils.updateResponseEntity(
        super.handleExceptionInternal(ex, body, headers, statusCode, request),
        ex,
        messageSource,
        LocaleContextHolder.getLocale());
  }
}
