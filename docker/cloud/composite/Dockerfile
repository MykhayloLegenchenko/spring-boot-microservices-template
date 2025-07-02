FROM eclipse-temurin:24-jre-alpine

WORKDIR app
RUN apk add wget --no-cache

COPY docker/cloud/composite/*.sh .
COPY cloud/discovery-service/build/libs/*.jar discovery.jar
COPY cloud/config-service/build/libs/*.jar /app/config.jar
COPY cloud/gateway-service/build/libs/*.jar /app/gateway.jar
RUN mkdir logs

HEALTHCHECK --interval=5s --timeout=5s --start-period=5s --retries=3 CMD \
    ./health-check.sh 8888 && \
    ./health-check.sh 8761 && \
    ./health-check.sh 8080 || exit 1

ENV JAVA_OPTS="-XX:InitialRAMPercentage=17.0 -XX:MaxRAMPercentage=27.0"
ENV SPRING_PROFILES_ACTIVE="container"
ENV CONFIG_SERVICE_URL="http://config-service:8888"

ENTRYPOINT ["./entry-point.sh"]
