FROM eclipse-temurin:24-jre-alpine

WORKDIR app
RUN apk add wget --no-cache

ARG APP_DIR
COPY ${APP_DIR}/build/libs/*.jar app.jar

ENV SERVER_PORT=8080
HEALTHCHECK --interval=5s --timeout=5s --start-period=5s --retries=3 \
    CMD wget --quiet --tries=1 -O - "http://localhost:$SERVER_PORT/actuator/health" | grep -q '"status":"UP"' || exit 1


ENV JAVA_OPTS="-XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=80.0"
ENV SPRING_PROFILES_ACTIVE="container"
ENV CONFIG_SERVICE_URL="http://config-service:8888"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
