package com.example.common.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * The value holder interface.
 *
 * <p>Implements {@link Supplier} and {@link Consumer} for its value.
 *
 * @param <T> value type
 */
public interface Holder<T extends @Nullable Object> extends Supplier<T>, Consumer<T> {
  /**
   * Creates a new {@code Holder} with a {@code null} value.
   *
   * @param <T> value type
   */
  static <T> Holder<T> create() {
    return new HolderImpl<>(null);
  }

  /**
   * Creates a new {@code Holder} with the given value.
   *
   * @param <T> value type
   */
  static <T> Holder<T> create(@Nullable T value) {
    return new HolderImpl<>(value);
  }

  @Nullable
  @Override
  T get();

  @Override
  void accept(@Nullable T t);

  default Optional<T> asOptional() {
    return Optional.ofNullable(get());
  }
}
