package com.example.common.web.bind.annotation;

import com.example.common.web.service.invoker.RequestParamObjectArgumentResolver;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates that a method parameter object fields should be bound to a web request
 * parameters.
 *
 * <p>Sorurce:
 *
 * <p>Handled by the {@link RequestParamObjectArgumentResolver}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParamObject {
  /**
   * Whether the parameter is required.
   *
   * <p>Defaults to {@code true}, leading to an exception being thrown if the parameter is missing
   * in the request.
   */
  boolean required() default true;
}
