# ============================================
# Stage 1: Builder - Construcción y extracción de capas
# ============================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copiar archivos esenciales para la construcción
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias en modo offline (aprovecha caché de capas de Docker)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN mvn clean package -DskipTests -B

# Extraer las capas del JAR (optimizacion para Spring Boot)
# Esto permite que las dependencias (que cambian poco) se caheen mejor
RUN java -Djarmode=layertools -jar target/*.jar extract

# ============================================
# Stage 2: Runtime - Imagen final ligera y segura
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Metadatos
LABEL maintainer="Valle Grande"
LABEL version="1.0.0"

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=prod \
    TZ=America/Lima \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC"

# Crear usuario sin privilegios y carpetas necesarias
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser && \
    apk add --no-cache tzdata dumb-init && \
    mkdir -p /app && \
    chown -R appuser:appuser /app

WORKDIR /app

# Copiar capas extraídas desde el builder (orden importa para caché)
COPY --from=builder --chown=appuser:appuser /app/dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appuser /app/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/application/ ./

# Usuario no root
USER appuser

# Exponer puerto
EXPOSE 5002

# Healthcheck usando la clase integrada de Java (opcional) o wget
# Nota: Alpine incluye wget en busybox, pero a veces es limitado. 
# Usamos el wget nativo de busybox si es suficiente, o instalamos uno mejor si falla.
# Aquí usamos wget de busybox que suele venir con Alpine por defecto.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:5002/actuator/health || exit 1

# Entrypoint usando dumb-init para manejo correcto de señales y Launcher de JarLauncher
ENTRYPOINT ["dumb-init", "java", "org.springframework.boot.loader.launch.JarLauncher"]
