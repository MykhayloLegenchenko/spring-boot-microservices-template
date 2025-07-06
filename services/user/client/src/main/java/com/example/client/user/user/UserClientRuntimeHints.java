package com.example.client.user.user;

import com.example.common.aot.RuntimeHintsUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Registers runtime hints for native image compatibility of the user API client. */
public class UserClientRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    RuntimeHintsUtils.registerClientApi(
        hints,
        UserBlockingClient.class,
        UserReactiveClient.class,
        "com.example.client.user.user.dto");
  }
}
