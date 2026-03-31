# API Gateway - Sistema SIPREB 🏛️

El **API Gateway** es el componente central y punto de entrada único para el ecosistema de microservicios de SIPREB (Seguimiento de Bienes Patrimoniales). Su propósito es actuar como un guardián y orquestador de todas las peticiones externas.

## 🏗️ Arquitectura: Hexagonal (Clean Architecture)

Este microservicio se ha implementado siguiendo los principios de la **Arquitectura Hexagonal** para asegurar que la lógica de negocio sea independiente de las tecnologías externas (como Spring Cloud, Redis o Keycloak).

### Estructura de Paquetes:
- **`domain`**: El corazón del sistema. Define los modelos (`Route`, `AuthToken`), excepciones y los **Puertos** (Interfaces) de entrada y salida.
- **`application`**: Contiene los **Casos de Uso** (Servicios) que implementan la lógica de orquestación, como la validación de límites de peticiones o la redirección de rutas.
- **`infrastructure`**: Detalles de implementación y **Adaptadores**.
    - **`adapter/in/web`**: Filtros globales de entrada (`AuthFilter`, `RateLimitFilter`, `CorrelationIdFilter`).
    - **`adapter/out`**: Implementaciones técnicas de los puertos (Keycloak, Redis, Proxy Reactivo).
    - **`config`**: Configuraciones de seguridad, beans de Spring y resiliencia.

## 🛠️ Responsabilidades y Funcionalidades

### 1. Enrutamiento Inteligente 🌐
Gestiona el tráfico hacia los microservicios core basándose en el path de la petición:
- **Autenticación**: `/api/v1/auth/**`, `/api/v1/users/**`
- **Patrimonio**: `/api/v1/patrimonio/**`, `/api/v1/bienes/**`
- **Mantenimiento**: `/api/v1/mantenimiento/**`
- **Configuración**: `/api/v1/areas/**`, `/api/v1/positions/**`

### 2. Seguridad Centralizada (OAuth2 + Keycloak) 🔐
Implementa un filtro global de seguridad que:
- Valida los tokens JWT contra el servidor de identidades (Keycloak).
- Bloquea peticiones no autorizadas antes de que lleguen a los microservicios internos.
- Inyecta información de seguridad en el contexto reactivo.

### 3. Rate Limiting con Redis ⏳
Previene el abuso de la API limitando el número de peticiones por identificador de cliente (IP) en una ventana de tiempo, utilizando **Redis Reactivo** para un alto rendimiento.

### 4. Trazabilidad Distribuida (Correlation ID) 🆔
Añade automáticamente un encabezado `X-Correlation-Id` a cada petición entrante. Este ID viaja a través de todos los microservicios, permitiendo reconstruir el flujo completo de una transacción en los logs.

### 5. Resiliencia (Circuit Breaker) ⚡
Utiliza **Resilience4j** para detectar fallos en servicios descendentes. Si un servicio falla repetidamente, el Gateway "abre el circuito" para evitar sobrecargar el sistema y fallar rápidamente (Fail-Fast).

### 6. CORS Global 🌍
Maneja las políticas de Intercambio de Recursos de Origen Cruzado de manera centralizada para permitir la comunicación fluida con el frontend.

## 🚀 Requisitos Técnicos
- **Java 17**
- **Spring Cloud Gateway** (WebFlux)
- **Redis Server** (para Rate Limiting)
- **Keycloak Server** (para Autenticación)

## ⚙️ Configuración Principal (`application.yaml`)

```yaml
spring:
  cloud:
    gateway:
      routes: # Definición de rutas a MS
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: # URL de Keycloak
  data:
    redis:
      host: # URL de Redis
```

---
**Desarrollado para la Municipalidad Distrital de San Luis - Cañete**
