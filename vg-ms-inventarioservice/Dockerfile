# Multi-stage build para optimizar el tamaño de la imagen
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar archivos de configuración de Maven primero (para cache)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Stage 2: Runtime - Usar distroless para máxima optimización
FROM gcr.io/distroless/java17-debian12:nonroot
WORKDIR /app

# Copiar el JAR desde el stage de build
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 5006

# Usuario no-root ya configurado en distroless
# Ejecutar la aplicación con optimizaciones de memoria
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

