package com.example.autoconfigure.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;

/** Auto-configuration for Swagger. */
@SecurityScheme(
    name = "default",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT")
@ConditionalOnWebApplication
@ConditionalOnClass(SpringDocConfiguration.class)
public class SwaggerAutoConfiguration {}
