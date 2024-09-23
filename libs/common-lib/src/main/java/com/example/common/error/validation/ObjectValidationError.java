package com.example.common.error.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Object validation error object. */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ObjectValidationError extends ValidationError {
  private final String object;

  public ObjectValidationError(String object, String reason) {
    super(reason);
    this.object = object;
  }

  @Override
  public String toString() {
    return "ObjectValidationError(object=" + object + ", error=" + getError() + ")";
  }
}
