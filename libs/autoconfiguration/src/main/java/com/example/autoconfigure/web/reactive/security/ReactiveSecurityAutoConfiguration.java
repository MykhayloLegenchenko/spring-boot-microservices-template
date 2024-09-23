package com.example.autoconfigure.web.reactive.security;

import com.example.common.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Auto-configuration for reactive web security.
 *
 * <p>Uses a bean of type {@link ReactiveSecurityConfigurator} for configuration.
 */
@AutoConfiguration
@ConditionalOnClass(ServerHttpSecurity.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableReactiveMethodSecurity
public class ReactiveSecurityAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http,
      ApplicationContext context,
      ReactiveSecurityConfigurator configurator) {

    configurator.configure(http, context);
    return http.build();
  }

  @Bean
  @ConditionalOnMissingBean
  public ReactiveSecurityConfigurator reactiveSecurityConfigurator() {
    return new DefaultReactiveSecurityConfigurator();
  }

  @Bean
  @ConditionalOnMissingBean
  public ReactiveJwtDecoder jwtDecoder(@Value("${auth.jwt.access-token.secret}") String secret) {
    return NimbusReactiveJwtDecoder.withSecretKey(SecurityUtils.createSecretKey(secret)).build();
  }

  @Bean
  @ConditionalOnMissingBean
  public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
    return new ReactiveJwtAuthenticationConverter();
  }
}
