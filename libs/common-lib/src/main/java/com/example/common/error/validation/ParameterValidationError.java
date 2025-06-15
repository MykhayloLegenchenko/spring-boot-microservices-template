package com.example.common.error.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/** Parameter validation error object. */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ParameterValidationError extends ValidationError {
  @Nullable private final String parameter;
  @Nullable private final Object value;

  public ParameterValidationError(
      @Nullable String parameter, @Nullable Object value, String reason) {
    super(reason);

    this.parameter = parameter;
    this.value = value;
  }

  @Override
  public String toString() {
    return "FieldValidationError(parameter="
        + parameter
        + ", value="
        + value
        + ", error="
        + getError()
        + ")";
  }
}
