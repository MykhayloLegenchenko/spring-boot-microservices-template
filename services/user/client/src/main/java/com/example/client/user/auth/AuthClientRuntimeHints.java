package com.example.client.user.auth;

import com.example.common.aot.RuntimeHintsUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Registers runtime hints for native image compatibility of the auth API client. */
public class AuthClientRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    RuntimeHintsUtils.registerClientApi(
        hints,
        AuthBlockingClient.class,
        AuthReactiveClient.class,
        "com.example.client.user.auth.dto");
  }
}
