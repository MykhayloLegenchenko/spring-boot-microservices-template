package com.example.common.error.validation;

import com.example.common.aot.RuntimeHintsUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Registers runtime hints for native image compatibility of validation error classes. */
public class ValidationErrorsRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    RuntimeHintsUtils.registerDto(
        hints,
        FieldValidationError.class,
        ObjectValidationError.class,
        ParameterValidationError.class,
        PropertyValidationError.class,
        ValidationErrorsRuntimeHints.class);
  }
}
