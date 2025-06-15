package com.example.autoconfigure.web.reactive.security;

import org.springframework.context.ApplicationContext;
import org.springframework.security.config.web.server.ServerHttpSecurity;

/** Configurator interface for reactive web security. */
public interface ReactiveSecurityConfigurator {

  /** Configures the given {@link ServerHttpSecurity} object. */
  void configure(ServerHttpSecurity http, ApplicationContext context);
}
