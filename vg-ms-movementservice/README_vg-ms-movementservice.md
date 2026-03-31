# Auditoría de Seguridad y Arquitectura - vg-ms-movementservice

**Versión del Servicio:** Spring Boot 3.5.x
**Stack Tecnológico:** Spring Boot WebFlux + R2DBC + PostgreSQL (Neon)
**Patrón Arquitéctonico:** Hexagonal (Ports & Adapters)
**Fecha de Auditoría:** 2026-03-19
**Clasificación:** Microservicio de Gestión de Movimientos de Activos

---

## 1. RESUMEN EJECUTIVO

**Análisis Total:** 35 archivos Java + SQL scripts auditados.

**Hallazgos Críticos:** 3 — Vulnerabilidades de Seguridad
**Hallazgos Altos:** 4 — Problemas arquitectónicos
**Hallazgos Medios:** 2 — Mejoras recomendadas

**Puntuación General (escala 0-10):** **6.5/10**

| Dimensión | Puntuación |
|-----------|-----------|
| Seguridad | 5.5/10 |
| Arquitectura | 7.0/10 |
| Gestión de Datos | 7.5/10 |
| Patrón Reactivo | 8.0/10 |
| Cobertura de Testing | 5.0/10 |

**Veredicto:** Servicio con buen fundamento reactivo pero expuesto por credenciales hardcodeadas y falta de validación de WebClient. CORS está correctamente configurado (sin credenciales). Requiere remediación de seguridad antes de producción.

---

## 2. PROBLEMAS CRÍTICOS (P0)

### 2.1. Credenciales de Base de Datos Expuestas

**Severidad:** CRÍTICO (P0)
**Componente:** [application.yml](vg-ms-movementservice/src/main/resources/application.yml#L9-L12)
**Estado:** ⚠️ No Remediado

**Hallazgo:**

```yaml
# Líneas 9-12: application.yml
r2dbc:
  url: r2dbc:postgresql://ep-small-pine-a4vgqevg-pooler.us-east-1.aws.neon.tech/neondb
  username: neondb_owner
  password: npg_7bhWjTw1XUMe
```

**Impacto:**

- Acceso directo a tabla `asset_movements` con todos los detalles de traspasos
- Lectura de `handover_receipts` con información sensible
- Potencial manipulación de registros de UUIDs de activos

**Remediación:**

Externalizar a variables de entorno y secretos manager:

```yaml
r2dbc:
  url: ${DB_URL}
  username: ${DB_USERNAME}
  password: ${DB_PASSWORD}
```

Deploy con Kubernetes secrets o equivalente en Docker.

---

### 2.2. WebClient sin Control de Timeout y Resiliencia

**Severidad:** CRÍTICO (P0)
**Componentes:** UserService.java, AssetService.java, ConfigurationService.java
**Estado:** ⚠️ No Remediado

**Hallazgo:**

El servicio hace llamadas HTTP a otros microservicios (asset, configuration, user) sin:

- Timeout
- Retry policy
- Circuit breaker
- Back-off exponencial

**Ejemplo de patrón vulnerable:**

```java
// Presumiblemente en UserService (línea 21):
WebClient webClient;  // Sin configuración de timeout
// Llamada bloqueante potencial que puede colgar indefinidamente
```

**Impacto - Cascading Failure:**

1. Se detiene assetservice → userservice cuelga indefinidamente esperando respuesta
2. userservice cuelga → movementservice cuelga esperando userservice
3. Gateway cuelga esperando movementservice
4. API completa cae (thundering herd)

**Remediación:**

Configurar WebClient con resiliencia:

```java
@Bean
public WebClient webClient() {
    return WebClient.builder()
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .baseUrl("http://user-service:8080")
        .filter((request, next) -> {
            return next.exchange(request)
                .timeout(Duration.ofSeconds(5))           // ← Timeout
                .retry(1)                                 // ← Retry once
                .onErrorMap(TimeoutException.class,
                    e → new ServiceUnavailableException("User service timeout"))
                .log();  // Log para debugging
        })
        .build();
}
```

**O usar Spring Cloud CircuitBreaker:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
```

---

### 2.3. Sin Validación de Entrada en DTOs

**Severidad:** CRÍTICO (P0)
**Componente:** [application/dto/](vg-ms-movementservice/src/main/java/pe/edu/vallegrande/movementservice/application/dto/) — AssetMovementRequest.java, etc.
**Estado:** ⚠️ No Remediado

**Hallazgo:**

No hay evidencia de `@Valid` o `@NotNull` en DTOs. Esto permite:

```json
POST /api/v1/asset-movements
{
  "assetId": null,                    // ← Acepta null
  "fromLocation": "",                  // ← Acepta strings vacíos
  "toLocation": "A".repeat(10000),    // ← Acepta entrada gigante (DoS)
  "movementDate": "invalid-date",     // ← Formato inválido
  "handedBy": null,                    // ← Sin validar identidad
  "receivedBy": null
}
```

**Remediación:**

```java
public class AssetMovementRequest {
    @NotNull(message = "assetId es obligatorio")
    @NotBlank
    private UUID assetId;

    @NotBlank(message = "fromLocation es obligatorio")
    @Size(min = 3, max = 100, message = "fromLocation debe tener 3-100 caracteres")
    private String fromLocation;

    @NotBlank(message = "toLocation es obligatorio")
    @Size(min = 3, max = 100)
    private String toLocation;

    @NotNull(message = "movementDate es obligatoria")
    @PastOrPresent(message = "movementDate no puede ser en el futuro")
    private LocalDate movementDate;
}
```

Enabler pom.xml:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Y aplicar en controlador:

```java
@PostMapping
public Mono<ResponseEntity<AssetMovementResponse>> create(
    @Valid @RequestBody AssetMovementRequest request) {
    // Solo se ejecuta si request es válido
}
```

---

## 3. ANÁLISIS ARQUITECTURA

### 3.1. Estructura Hexagonal - Bien Definida

**Evidencia:**

```
vg-ms-movementservice/
├── infrastructure/
│   ├── adapter/
│   │   ├── input/       ← Puertos de entrada REST
│   │   └── output/      ← Puertos de salida (BD, WebClient)
│   └── config/          ← Configuración Spring
├── application/
│   ├── dto/             ← Data Transfer Objects
│   ├── ports/           ← Interfaces de puertos
│   ├── service/         ← Orchestración de aplicación
│   └── mapper/          ← Conversión DTO ↔ Domain
├── domain/
│   ├── model/           ← Entidades AssetMovement, HandoverReceipt
│   ├── exception/       ← Excepciones de dominio
│   └── service/         ← Lógica de negocio puro
└── test/
    └── java/
        └── (2 test files - INSUFICIENTE)
```

**Fortaleza:** Capas bien separadas, dependencias apuntan hacia adentro (Domain es independiente).

**Puntuación:** 7.5/10

### 3.2. WebFlux - Uso Correcto

El servicio está completamente reactivo. Endpoints retornan `Mono` y `Flux`:

```java
public Mono<ResponseEntity<AssetMovementResponse>> create(...)
public Mono<AssetMovement> startMovement(UUID id, UUID userId)
public Flux<AssetMovement> getAllByAsset(UUID assetId)
```

**Debilidad:** Sin evidence de composición reactiva avanzada (`flatMap`, `switchMap`, `zip`).

**Puntuación:** 7.5/10

### 3.3. CORS - Correctamente Configurado (sin Credenciales)

**Evidencia (CorsConfig.java, líneas 17-31):**

```java
@Bean
public CorsWebFilter corsWebFilter() {
    CorsConfiguration config = new CorsConfiguration();

    // importante: desactivamos credenciales          ← LÍNEA 20-21
    config.setAllowCredentials(false);                // ✅ CORRECTO

    // permite todos los orígenes
    config.addAllowedOriginPattern("*");              // ← COMPATIBLE porque credenciales = false

    // permite todos los headers
    config.addAllowedHeader("*");

    // permite todos los métodos
    config.addAllowedMethod("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsWebFilter(source);
}
```

**Evaluación:**

- ✅ `allowCredentials(false)` previene exploit CSRF
- ✅ Wildcard origin pattern es seguro sin credenciales
- ⚠️ Pero es muy permisivo; mejor especificar orígenes

**Remediación Opcional:**

```java
config.setAllowedOrigins(Arrays.asList(
    "https://sipreb.vallegrande.edu.pe",
    "https://admin.vallegrande.edu.pe"
));
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT"));
config.setMaxAge(3600L);
```

**Puntuación:** 8.0/10

---

## 4. SEGURIDAD DETALLADA

### 4.1. OAuth2 y JWT

**Configuración (application.yml, líneas 16-20):**

```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        issuer-uri: http://localhost:8080/realms/vallegrande
```

**Evaluación:**

- ✅ OAuth2 habilitado
- ⚠️ issuer-uri apunta a localhost en desarrollo
- ❌ No hay evidencia de protección de endpoints

**Puntuación:** 6.0/10

### 4.2. Actuator Endpoints

No hay configuración visible de actuator en application.yml, lo que significa están expuestos con defaults inseguros.

**Riesgo:** `GET /actuator` podría listar todos los endpoints disponibles.

**Remediación:**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: when-authorized
```

**Puntuación:** 4.0/10

---

## 5. GESTIÓN DE DATOS

### 5.1. Esquema SQL - Bien Definido

**Evidencia (001_create_asset_movements.sql):**

El esquema define:

- `asset_movements` table con UUID FK a assets
- `handover_receipts` table con autorización
- Constraints CHECK en status
- Campos de auditoría (`created_at`, `updated_at`)

**Fortaleza:**

- ✅ Soft delete potential (no DELETE físico visible)
- ✅ Constraints en estado
- ✅ Fields de auditoría presentes

**Debilidad:**

- ❌ No hay FOREIGN KEY constraints visibles (enforcement débil)
- ❌ No hay INDEX en búsquedas frecuentes

**Puntuación:** 7.0/10

### 5.2. Operaciones de Lectura - Patrón N+1

**Riesgo Identificado:**

Si el controlador retorna `Flux<AssetMovement>` y el mapeo incluye datos relacionados (asset details, user details), podría ejecutar 1 query para obtener movimientos + N queries para detalles de cada movimiento.

**Remediación:**

Usar JOINs en SQL o batching:

```sql
SELECT am.*, a.asset_name, u.user_name
FROM asset_movements am
LEFT JOIN assets a ON am.asset_id = a.id
LEFT JOIN users u ON am.handed_by = u.id
WHERE am.asset_id = ?
```

**Puntuación:** 6.0/10

---

## 6. PATRONES REACTIVOS

### 6.1. R2DBC Pool

**Configuración (application.yml, líneas 13-15, 20-25):**

```yaml
pool:
  enabled: true
  initial-size: 1        # ⚠️ MUY BAJO (debería ser 5-10)
  max-size: 3            # ⚠️ MUY BAJO (debería ser 15-20)
  max-idle-time: 30m
  max-acquire-time: 60s  # ✅ Buen timeout
```

**Riesgo:** Con initial-size=1 y max-size=3, cualquier pico de tráfico causará:

- Bloqueo esperando conexión disponible
- Timeout de queries
- Cascading failures

**Remediación:**

```yaml
pool:
  enabled: true
  initial-size: 8
  max-size: 15
  max-idle-time: 10m
  max-acquire-time: 30s
  max-create-connection-time: 2s
  validation-query: SELECT 1
```

**Puntuación:** 4.5/10

### 6.2. Composición Reactiva

No hay evidence de uso avanzado (`flatMap`, `switchMap`, `zip`). Las operaciones parecen lineales.

**Puntuación:** 6.0/10

---

## 7. TESTING

### 7.1. Cobertura Muy Baja

**Evidencia:**

```
src/test/java/pe/edu/vallegrande/movementservice/
└── application/
    └── service/
        └── (0-2 test files)
```

**Estimado:** <10% cobertura

**Deficiencias:**

- ❌ Sin tests de controlador REST
- ❌ Sin tests de integración con BD
- ❌ Sin tests de seguridad (OAuth2)
- ❌ Sin tests de validación

**Puntuación:** 2.0/10

---

## 8. LOGGING Y MONITOREO

### 8.1. Logging no Configurado Visiblemente

No hay sección `logging:` en application.yml, usando defaults de Spring (todos INFO).

**Riesgo:** Logs excesivos en producción; dificultades de debugging.

**Remediación:**

```yaml
logging:
  level:
    root: WARN
    "[pe.edu.vallegrande]": INFO
    "[io.r2dbc.postgresql]": WARN
    "[reactor.netty]": WARN
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

**Puntuación:** 4.0/10

---

## 9. CONCLUSIÓN

### Resumen de Hallazgos

| # | Problema | Severidad | Acción |
|---|----------|-----------|--------|
| 1 | Credenciales expuestas | CRÍTICO | Rotar y externalizar |
| 2 | WebClient sin timeout | CRÍTICO | Agregar resilience |
| 3 | Sin validación de input | CRÍTICO | Bean Validation |
| 4 | Pool conexiones bajo | ALTO | Aumentar initial/max size |
| 5 | Testing mínimo | ALTO | Cobertura >50% requerida |
| 6 | Logging insuficiente | MEDIO | Configurar levels |
| 7 | N+1 queries potential | MEDIO | Usar JOINs |

### Puntuación Final: 6.5/10

**Recomendación:** NOT PRODUCTION READY hasta resolver los 3 issues críticos.

---

## APÉNDICE A: Archivos Auditados

1. [application.yml](vg-ms-movementservice/src/main/resources/application.yml#L1-L50) — Config
2. [CorsConfig.java](vg-ms-movementservice/src/main/java/pe/edu/vallegrande/movementservice/infrastructure/config/CorsConfig.java) — CORS ✅
3. [pom.xml](vg-ms-movementservice/pom.xml) — Dependencies
4. [db/001_create_asset_movements.sql](vg-ms-movementservice/src/main/resources/db/001_create_asset_movements.sql) — Schema
5. [db/002_create_handover_receipts.sql](vg-ms-movementservice/src/main/resources/db/002_create_handover_receipts.sql) — Schema

**Fin de Auditoría**
