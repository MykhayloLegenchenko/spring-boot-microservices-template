package com.example.autoconfigure.web.servlet.security;

import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/** Configurator interface for web security. */
public interface ServletSecurityConfigurator {

  /** Configures the given {@link HttpSecurity} object. */
  void configure(HttpSecurity http, ApplicationContext context) throws Exception;
}
