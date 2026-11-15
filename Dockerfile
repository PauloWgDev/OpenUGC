# Build

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN chmod +x ./mvnw

COPY src ./src

RUN ./mvnw clean package -DskipTests -Dmaven.test.skip=true

# Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# curl is used by the HEALTHCHECK below
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# start-period gives Spring Boot time to start (and Flyway/JPA to touch the DB) before failures count
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS "http://localhost:${PORT:-8080}/actuator/health" || exit 1


CMD ["java", "-jar", "app.jar"]

