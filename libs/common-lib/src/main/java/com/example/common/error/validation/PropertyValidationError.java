package com.example.common.error.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/** Property validation error object. */
@Getter
@EqualsAndHashCode(callSuper = true)
public class PropertyValidationError extends ValidationError {
  private final String property;
  @Nullable private final Object value;

  public PropertyValidationError(String property, @Nullable Object value, String reason) {
    super(reason);

    this.property = property;
    this.value = value;
  }

  @Override
  public String toString() {
    return "PropertyValidationError(property="
        + property
        + ", value="
        + value
        + ", error="
        + getError()
        + ")";
  }
}
