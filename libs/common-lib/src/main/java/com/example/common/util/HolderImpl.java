package com.example.common.util;

import org.springframework.lang.Nullable;

/** Implementation class for {@link Holder} interface. */
class HolderImpl<T> implements Holder<T> {
  @Nullable private T value;

  public HolderImpl(@Nullable T value) {
    this.value = value;
  }

  @Nullable
  @Override
  public T get() {
    return value;
  }

  @Override
  public void accept(@Nullable T t) {
    value = t;
  }
}
