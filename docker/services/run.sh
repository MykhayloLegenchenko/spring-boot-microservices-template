#!/bin/sh

java -Dspring.profiles.active="${PROFILE}" \
  -Djava.security.egd=file:/dev/./urandom \
  -javaagent:/app/opentelemetry/opentelemetry-javaagent.jar \
  -Dotel.javaagent.extensions=/app/opentelemetry/opentelemetry-extension.jar \
  -Dotel.javaagent.configuration-file="/app/opentelemetry/${OTEL_PROFILE}.properties" \
  -jar "/app/$1.jar"
