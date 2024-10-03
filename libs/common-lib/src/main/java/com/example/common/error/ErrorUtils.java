package com.example.common.error;

import com.example.common.error.validation.ValidationError;
import io.opentelemetry.api.trace.Span;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.client.WebClientException;

/** Error utilities. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorUtils {
  private static final URI TYPE_BLANK = URI.create("about:blank");
  private static final URI TYPE_VALIDATION = URI.create("validation");

  private static final boolean OPEN_TELEMETRY_PRESENT;
  private static final boolean WEB_CLIENT_PRESENT;

  static {
    var loader = ErrorUtils.class.getClassLoader();

    OPEN_TELEMETRY_PRESENT = ClassUtils.isPresent("io.opentelemetry.api.trace.Span", loader);
    WEB_CLIENT_PRESENT = ClassUtils.isPresent("reactor.netty.http.client.HttpClient", loader);
  }

  /**
   * Updates the given response entity with additional details.
   *
   * <p>Intended for use by exception handlers.
   *
   * @param entity response entity to be updated, can be {@code null}
   * @param ex response exception
   * @param messageSource message source
   * @param locale locale for resolving messages, can be {@code null}
   * @return the updated {@link ResponseEntity} or null if the input entity was null
   */
  @Nullable
  public static ResponseEntity<Object> updateResponseEntity(
      @Nullable ResponseEntity<Object> entity,
      Exception ex,
      MessageSource messageSource,
      @Nullable Locale locale) {

    if (entity == null) {
      return null;
    }

    var body = entity.getBody();
    if (body == null) {
      body = ProblemDetail.forStatusAndDetail(entity.getStatusCode(), ex.getMessage());
    }

    if (body instanceof ProblemDetail detail) {
      addValidationDetails(detail, ex, messageSource, locale);

      if (OPEN_TELEMETRY_PRESENT) {
        addOpenTelemetryDetails(detail, ex);
      }
    }

    return body != entity.getBody()
        ? new ResponseEntity<>(body, entity.getHeaders(), entity.getStatusCode())
        : entity;
  }

  /**
   * Returns the appropriate HTTP status code based on the provided exception.
   *
   * <p>Intended for use by exception handlers.
   *
   * @param ex response exception
   */
  public static HttpStatusCode getStatusCode(Exception ex) {
    if (ex instanceof ErrorResponse e) {
      return e.getStatusCode();
    }

    return switch (ex) {
      case AccessDeniedException ignored -> HttpStatus.FORBIDDEN;
      case AuthenticationException ignored -> HttpStatus.UNAUTHORIZED;
      case BindException ignored -> HttpStatus.BAD_REQUEST;
      case RestClientResponseException ignored -> HttpStatus.BAD_GATEWAY;
      default -> {
        if (WEB_CLIENT_PRESENT && ex instanceof WebClientException) {
          yield HttpStatus.BAD_REQUEST;
        }

        yield HttpStatus.INTERNAL_SERVER_ERROR;
      }
    };
  }

  /**
   * Returns the appropriate HTTP headers based on the provided exception.
   *
   * <p>Intended for use by exception handlers.
   *
   * @param ex response exception
   */
  public static HttpHeaders getHeaders(Exception ex) {
    return ex instanceof ErrorResponse e ? e.getHeaders() : HttpHeaders.EMPTY;
  }

  private static List<ValidationError> findValidationErrors(
      Throwable ex, MessageSource messageSource, Locale locale) {

    while (ex != null) {
      var errors = ValidationError.getErrors(ex, messageSource, locale);
      if (!errors.isEmpty()) {
        return errors;
      }

      ex = ex.getCause();
    }

    return Collections.emptyList();
  }

  private static void setDetailProperty(ProblemDetail detail, String name, Object value) {
    var properties = detail.getProperties();
    if (properties == null) {
      properties = new LinkedHashMap<>();
      detail.setProperties(properties);
    }

    properties.put(name, value);
  }

  private static void addValidationDetails(
      ProblemDetail detail, Exception ex, MessageSource messageSource, @Nullable Locale locale) {

    if (!TYPE_BLANK.equals(detail.getType())) {
      return;
    }

    if (locale == null) {
      locale = Locale.getDefault();
    }

    var validationErrors = findValidationErrors(ex, messageSource, locale);
    if (!validationErrors.isEmpty()) {
      detail.setType(TYPE_VALIDATION);
      setDetailProperty(detail, "errors", validationErrors);
    }
  }

  private static void addOpenTelemetryDetails(ProblemDetail detail, Exception ex) {
    var span = Span.current();
    var context = span.getSpanContext();
    if (context.isValid()) {
      span.recordException(ex);
      setDetailProperty(detail, "traceId", context.getTraceId());
    }
  }
}
