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

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# HEALTHCHECK CMD curl -f http://localhost:8080/api/healthcheck || exit 1

CMD ["java", "-jar", "app.jar"]

