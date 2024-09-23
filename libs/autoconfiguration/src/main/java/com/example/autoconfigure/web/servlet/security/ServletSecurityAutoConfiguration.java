package com.example.autoconfigure.web.servlet.security;

import com.example.common.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Auto-configuration for web security.
 *
 * <p>Uses a bean of type {@link ServletSecurityConfigurator} for configuration.
 */
@AutoConfiguration
@ConditionalOnClass(HttpSecurity.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableMethodSecurity
public class ServletSecurityAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public SecurityFilterChain filterChain(
      HttpSecurity http, ApplicationContext context, ServletSecurityConfigurator configuration)
      throws Exception {

    configuration.configure(http, context);
    return http.build();
  }

  @Bean
  @ConditionalOnMissingBean
  public ServletSecurityConfigurator servletSecurityConfigurator() {
    return new DefaultServletSecurityConfigurator();
  }

  @Bean
  @ConditionalOnMissingBean
  public JwtDecoder jwtDecoder(@Value("${auth.jwt.access-token.secret}") String secret) {
    return NimbusJwtDecoder.withSecretKey(SecurityUtils.createSecretKey(secret)).build();
  }

  @Bean
  @ConditionalOnMissingBean
  public ServletJwtAuthenticationConverter jwtAuthenticationServletConverter() {
    return new ServletJwtAuthenticationConverter();
  }
}
