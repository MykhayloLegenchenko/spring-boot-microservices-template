package com.example.test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.client.HttpClientErrorException;

/** Custom assertion methods. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

  /**
   * Assert that an exception of type BadRequest is thrown by the {@code callable}
   *
   * @param callable code throwing the BadRequest exception
   */
  public static void assertBadRequest(ThrowingCallable callable) {
    assertThatExceptionOfType(HttpClientErrorException.BadRequest.class).isThrownBy(callable);
  }

  /**
   * Assert that an exception of type Conflict is thrown by the {@code callable}
   *
   * @param callable code throwing the Conflict exception
   */
  public static void assertConflict(ThrowingCallable callable) {
    assertThatExceptionOfType(HttpClientErrorException.Conflict.class).isThrownBy(callable);
  }

  /**
   * Assert that an exception of type Forbidden is thrown by the {@code callable}
   *
   * @param callable code throwing the Forbidden exception
   */
  public static void assertForbidden(ThrowingCallable callable) {
    assertThatExceptionOfType(HttpClientErrorException.Forbidden.class).isThrownBy(callable);
  }

  /**
   * Assert that an exception of type NotFound is thrown by the {@code callable}
   *
   * @param callable code throwing the NotFound exception
   */
  public static void assertNotFound(ThrowingCallable callable) {
    assertThatExceptionOfType(HttpClientErrorException.NotFound.class).isThrownBy(callable);
  }

  /**
   * Assert that an exception of type Unauthorized is thrown by the {@code callable}
   *
   * @param callable code throwing the Unauthorized exception
   */
  public static void assertUnauthorized(ThrowingCallable callable) {
    assertThatExceptionOfType(HttpClientErrorException.Unauthorized.class).isThrownBy(callable);
  }
}
