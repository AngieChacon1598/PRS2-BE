# Build Stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Gran executable permissions to the wrapper
RUN chmod +x mvnw

# Resolve dependencies (go offline to cache them in a layer)
RUN ./mvnw dependency:go-offline

# Copy the rest of the project source code
COPY src ./src

# Package the application
RUN ./mvnw clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Define the entrypoint to run the gateway
ENTRYPOINT ["java", "-jar", "app.jar"]
