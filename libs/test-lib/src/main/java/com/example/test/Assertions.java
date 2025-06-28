package com.example.test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.client.HttpClientErrorException.*;

/** Custom assertion methods. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

  /**
   * Asserts that the given {@code callable} throws a {@link BadRequest} exception.
   *
   * @param callable the code expected to throw a {@code BadRequest} exception
   */
  public static void assertBadRequest(ThrowingCallable callable) {
    assertThatExceptionOfType(BadRequest.class).isThrownBy(callable);
  }

  /**
   * Asserts that the given {@code callable} throws a {@link Conflict} exception.
   *
   * @param callable the code expected to throw a {@code Conflict} exception
   */
  public static void assertConflict(ThrowingCallable callable) {
    assertThatExceptionOfType(Conflict.class).isThrownBy(callable);
  }

  /**
   * Asserts that the given {@code callable} throws a {@link Forbidden} exception.
   *
   * @param callable the code expected to throw a {@code Forbidden} exception
   */
  public static void assertForbidden(ThrowingCallable callable) {
    assertThatExceptionOfType(Forbidden.class).isThrownBy(callable);
  }

  /**
   * Asserts that the given {@code callable} throws a {@link NotFound} exception.
   *
   * @param callable the code expected to throw a {@code NotFound} exception
   */
  public static void assertNotFound(ThrowingCallable callable) {
    assertThatExceptionOfType(NotFound.class).isThrownBy(callable);
  }

  /**
   * Asserts that the given {@code callable} throws a {@link Unauthorized} exception.
   *
   * @param callable the code expected to throw a {@code Unauthorized} exception
   */
  public static void assertUnauthorized(ThrowingCallable callable) {
    assertThatExceptionOfType(Unauthorized.class).isThrownBy(callable);
  }
}
