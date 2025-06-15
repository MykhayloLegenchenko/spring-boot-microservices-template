package com.example.common.error.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/** Field validation error object. */
@Getter
@EqualsAndHashCode(callSuper = true)
public class FieldValidationError extends ObjectValidationError {
  private final String field;
  @Nullable private final Object value;

  public FieldValidationError(String object, String field, @Nullable Object value, String reason) {
    super(object, reason);

    this.field = field;
    this.value = value;
  }

  @Override
  public String toString() {
    return "FieldValidationError(object="
        + getObject()
        + ", field="
        + field
        + ", value="
        + value
        + ", error="
        + getError()
        + ")";
  }
}
