FROM eclipse-temurin:23-jre-alpine

RUN apk add grep \
    && apk add curl \
    && apk cache clean \
    && mkdir /app

ARG GROUP
ARG SERVICE

COPY ${GROUP}/${SERVICE}/build/libs/*.jar /app/app.jar

COPY tools/opentelemetry /app/opentelemetry/
COPY libs/opentelemetry-extension/build/libs/*.jar /app/opentelemetry/opentelemetry-extension.jar

COPY docker/services/*.sh /app/

ENV PORT=8080
EXPOSE $PORT

HEALTHCHECK --interval=5s --timeout=5s --start-period=5s --retries=3 CMD ["./app/check.sh", "$PORT"]

ENV PROFILE="container"
ENV OTEL_PROFILE=$PROFILE
ENV CONFIG_SERVICE_URL="http://config-service:8888"

ENTRYPOINT ["./app/run.sh", "app"]
