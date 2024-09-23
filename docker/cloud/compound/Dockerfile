FROM eclipse-temurin:23-jre-alpine

RUN apk add grep \
    && apk add curl \
    && apk cache clean \
    && mkdir -p /app/logs

ARG GROUP="cloud"

COPY ${GROUP}/discovery-service/build/libs/*.jar /app/discovery.jar
COPY ${GROUP}/config-service/build/libs/*.jar /app/config.jar
COPY ${GROUP}/gateway-service/build/libs/*.jar /app/gateway.jar

COPY tools/opentelemetry /app/opentelemetry/
COPY libs/opentelemetry-extension/build/libs/*.jar /app/opentelemetry/opentelemetry-extension.jar

COPY docker/services/*.sh /app/
COPY docker/cloud/compound/*.sh /app/

EXPOSE 8761
EXPOSE 8888
EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=5s --start-period=5s --retries=3 CMD ["./app/check-all.sh"]

ENV PROFILE="container"
ENV OTEL_PROFILE=$PROFILE
ENV DISCOVERY_SERVICE_URL="http://localhost:8761"
ENV CONFIG_SERVICE_URL="http://localhost:8888"

ENTRYPOINT ["./app/start-all.sh"]
