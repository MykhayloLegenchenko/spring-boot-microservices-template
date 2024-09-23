package com.example.common.error.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/** Validation error object. */
@Data
@RequiredArgsConstructor
public class ValidationError {
  private final String error;

  public static List<ValidationError> getErrors(
      Throwable ex, MessageSource messageSource, Locale locale) {
    return switch (ex) {
      case BindException e -> getErrors(e.getAllErrors(), messageSource, locale);
      case BindValidationException e ->
          getErrors(e.getValidationErrors().getAllErrors(), messageSource, locale);
      case HandlerMethodValidationException e -> getMethodErrors(e, messageSource, locale);
      case WebExchangeBindException e -> getErrors(e.getAllErrors(), messageSource, locale);
      default -> Collections.emptyList();
    };
  }

  private static List<ValidationError> getErrors(
      List<? extends MessageSourceResolvable> errors, MessageSource messageSource, Locale locale) {
    return errors.stream().map(e -> create(e, messageSource, locale)).toList();
  }

  private static List<ValidationError> getMethodErrors(
      HandlerMethodValidationException ex, MessageSource messageSource, Locale locale) {
    var result = new ArrayList<ValidationError>();
    for (var res : ex.getAllValidationResults()) {
      var name = res.getMethodParameter().getParameterName();
      for (var err : res.getResolvableErrors()) {
        result.add(
            new ParameterValidationError(
                name, res.getArgument(), messageSource.getMessage(err, locale)));
      }
    }

    return result;
  }

  private static ValidationError create(
      MessageSourceResolvable error, MessageSource messageSource, Locale locale) {

    var reason = messageSource.getMessage(error, locale);
    return switch (error) {
      case FieldError e ->
          new FieldValidationError(e.getObjectName(), e.getField(), e.getRejectedValue(), reason);
      case ObjectError e -> new ObjectValidationError(e.getObjectName(), reason);
      default -> new ValidationError(reason);
    };
  }
}
