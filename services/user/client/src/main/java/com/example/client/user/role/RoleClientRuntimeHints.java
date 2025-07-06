package com.example.client.user.role;

import com.example.common.aot.RuntimeHintsUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Registers runtime hints for native image compatibility of the role API client. */
public class RoleClientRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    RuntimeHintsUtils.registerClientApi(
        hints,
        RoleBlockingClient.class,
        RoleReactiveClient.class,
        "com.example.client.user.role.dto");
  }
}
