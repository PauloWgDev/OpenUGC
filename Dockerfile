# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and project files
COPY . .

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port that Render will assign (Render injects it via $PORT)
EXPOSE 8080

# Run the jar file
CMD ["java", "-jar", "target/unity-api-0.0.1-SNAPSHOT.jar"]

