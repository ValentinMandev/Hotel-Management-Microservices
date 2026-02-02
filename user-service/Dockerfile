# ============================================
# STAGE 1: BUILD
# ============================================
FROM gradle:9.3.1-jdk21-alpine AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew clean bootJar -x test --no-daemon
RUN ls -la build/libs/


# ============================================
# STAGE 2: RUNTIME
# ============================================
FROM eclipse-temurin:21-jre-alpine
LABEL service="user-service"
LABEL version="1.0.0"
WORKDIR /app

RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser
COPY --from=build --chown=appuser:appuser /app/build/libs/user-service.jar app.jar
USER appuser

HEALTHCHECK --interval=30s --timeout=3s --start-period=120s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

EXPOSE 8081

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]

