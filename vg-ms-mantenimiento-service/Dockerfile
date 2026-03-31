# ============================================
# Stage 1: Build - Construcción de la aplicación
# ============================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven para aprovechar el caché de dependencias
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar la aplicación y crear el JAR ejecutable
RUN mvn clean package -DskipTests -B

# ============================================
# Stage 2: Runtime - Imagen final ultra-optimizada
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Metadatos de la imagen
LABEL maintainer="Valle Grande"
LABEL description="Maintenance Service - Microservicio de Gestión de Mantenimiento Municipal"
LABEL version="1.0.0"

# Variables de entorno por defecto optimizadas
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom" \
    TZ=America/Lima

# Instalar solo wget para healthcheck, crear usuario no-root
RUN apk add --no-cache wget tzdata && \
    addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser && \
    mkdir -p /app && \
    chown -R appuser:appuser /app

# Establecer directorio de trabajo
WORKDIR /app

# Cambiar a usuario no-root
USER appuser

# Copiar solo el JAR ejecutable desde el stage builder
COPY --from=builder --chown=appuser:appuser /app/target/*.jar app.jar

# Exponer puerto de la aplicación
EXPOSE 5007

# Health check optimizado
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:5007/actuator/health || exit 1

# Punto de entrada simplificado con el JAR
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
