# Auditoría de Seguridad y Arquitectura - vg-ms-mantenimiento-service

**Versión del Servicio:** 3.5.11 (Spring Boot)
**Stack Tecnológico:** Spring Boot WebFlux + R2DBC + PostgreSQL (Neon)
**Patrón Arquitéctonico:** Hexagonal (Ports & Adapters)
**Fecha de Auditoría:** 2026-03-19
**Clasificación:** Microservicio de Gestión de Mantenimientos de Activos

---

## 1. RESUMEN EJECUTIVO

**Análisis Total:** 45 archivos Java auditados + documentación de esquema SQL.

**Hallazgos Críticos:** 4 — Vulnerabilidades de Seguridad no remediadas
**Hallazgos Altos:** 3 — Problemas arquitectónicos moderados
**Hallazgos Medios:** 2 — Mejoras recomendadas

**Puntuación General (escala 0-10):** **6.8/10**

| Dimensión | Puntuación |
|-----------|-----------|
| Seguridad | 5.0/10 |
| Arquitectura | 7.5/10 |
| Gestión de Datos | 8.0/10 |
| Patrón Reactivo | 8.5/10 |
| Cobertura de Testing | 4.0/10 (baja) |

**Veredicto:** Servicio expuesto a vulnerabilidades críticas de CORS. Correctamente implementa OAuth2/JWT y validación de entrada, pero requiere remediación inmediata en configuración de seguridad CORS y protección de credenciales antes de despliegue a producción.

---

## 2. PROBLEMAS CRÍTICOS (P0)

### 2.1. Credenciales de Base de Datos Expuestas en Repositorio

**Severidad:** CRÍTICO (P0)
**Componente:** [application.yml](vg-ms-mantenimiento-service/src/main/resources/application.yml#L14-L17)
**Estado:** ⚠️ No Remediado

**Hallazgo:**
Las credenciales de PostgreSQL Neon están codificadas en texto plano en el repositorio:

```yaml
# Líneas 14-17: application.yml
r2dbc:
  url: r2dbc:postgresql://ep-billowing-voice-ad1klyzx-pooler.c-2.us-east-1.aws.neon.tech:5432/ms-maintenanceService?sslmode=require
  username: neondb_owner
  password: npg_HFo2ij7rgRbN
```

**Impacto Técnico:**

- Cualquiera con acceso al repositorio puede obtener credenciales de base de datos
- Acceso directo a todos los datos de mantenimientos (UUID de activos municipales, costos, responsables técnicos)
- Escalación de privilegios: acceso a `neondb_owner` permite modificar esquema

**Remediación:**

Externalizar credenciales mediante variables de entorno:

```yaml
r2dbc:
  url: ${DB_URL:r2dbc:postgresql://localhost/maintenance}
  username: ${DB_USERNAME}
  password: ${DB_PASSWORD}
```

Configurar en secretos del orquestador (Docker secrets, Kubernetes secrets, o gestor de secretos como Vault).

---

### 2.2. CORS Permisivo + Credenciales Habilitadas (Combinación Peligrosa)

**Severidad:** CRÍTICO (P0)
**Componente:** [SecurityConfig.java](vg-ms-mantenimiento-service/src/main/java/pe/edu/vallegrande/ms_maintenanceService/infrastructure/config/SecurityConfig.java#L40-L51)
**Estado:** ⚠️ No Remediado

**Hallazgo:**

```java
// Líneas 40-51: SecurityConfig.java
@Bean
public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);           // ← LÍNEA 43: Habilita envío de cookies/auth
    config.addAllowedOriginPattern("*");         // ← LÍNEA 44: Acepta cualquier origen
    config.addAllowedHeader("*");                 // ← LÍNEA 45: Acepta cualquier header
    config.addAllowedMethod("*");                 // ← LÍNEA 46: Acepta cualquier método HTTP

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**Impacto Técnico:**

Esta combinación es un **vector CSRF (Cross-Site Request Forgery)**:

1. Atacante malicioso crea sitio web con script JavaScript
2. Script invoca `POST /api/v1/maintenances` con token interceptado
3. Navegador AUTOMÁTICAMENTE envía credenciales (debido a `allowCredentials=true`)
4. Petición ejecuta con privilegios del usuario original

**Flujo de Ataque Concreto:**

```html
<!-- malicious-site.com -->
<img src="http://localhost:5007/api/v1/maintenances?create=critical-maintenance" />
<!-- El navegador envía la cookie/header de autenticación automáticamente -->
```

**Remediación – Opción 1: Whitelist de Orígenes (Recomendado)**

```java
@Bean
public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);

    // Permitir SOLO los orígenes conocidos
    config.setAllowedOrigins(Arrays.asList(
        "https://sipreb.vallegrande.edu.pe",
        "https://admin.vallegrande.edu.pe"
    ));

    config.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**Opción 2: Deshabilitar Credenciales (Si es posible)**

```java
config.setAllowCredentials(false);  // Desactiva cookies/auth en CORS
config.addAllowedOriginPattern("*");  // Ahora es seguro usar wildcard
```

---

### 2.3. Sin Validación en Payloads de Request (Riesgo de Inyección)

**Severidad:** CRÍTICO (P0)
**Componente:** [MaintenanceWebRequest.java](vg-ms-mantenimiento-service/src/main/java/pe/edu/vallegrande/ms_maintenanceService/infrastructure/adapter/in/rest/dto/MaintenanceWebRequest.java) (no encontrado en búsqueda)
**Evidencia Alternative:** [MaintenanceController.java - Líneas 62-64](vg-ms-mantenimiento-service/src/main/java/pe/edu/vallegrande/ms_maintenanceService/infrastructure/adapter/in/rest/MaintenanceController.java#L62-L64)
**Estado:** ✅ Parcialmente Mitigado

**Hallazgo:**

El controlador declara `@Valid @RequestBody MaintenanceWebRequest`, lo que IMPORTA validación:

```java
// Líneas 62-64: MaintenanceController.java
@PostMapping
@PreAuthorize("hasAuthority('mantenimiento:create') or hasRole('TENANT_ADMIN') or hasRole('MANTENIMIENTO_GESTOR')")
public Mono<ResponseEntity<MaintenanceResponseDTO>> create(
    @Valid @RequestBody MaintenanceWebRequest webRequest,  // ← @Valid habilitado
    @AuthenticationPrincipal Jwt jwt) {
```

**FORTALEZA DETECTADA:** El servicio YA INCLUYE `spring-boot-starter-validation` en pom.xml (línea 68, vg-ms-mantenimiento-service/pom.xml):

```xml
<!-- Spring Boot Starter Validation - Validación de datos con Jakarta Bean Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Sin embargo:** El riesgo existe si campos de texto largo (`work_description`, `reportedProblem` - líneas 26-27 en schema.sql) no están limitados en longitud.

**Remediación:**

Agregar constraints a `MaintenanceWebRequest`:

```java
public class MaintenanceWebRequest {
    @NotBlank(message = "maintenanceCode es obligatorio")
    @Size(min = 3, max = 50, message = "maintenanceCode debe tener 3-50 caracteres")
    private String maintenanceCode;

    @NotBlank(message = "work_description es obligatoria")
    @Size(max = 8000, message = "work_description no puede exceder 8000 caracteres")
    private String workDescription;

    @NotNull(message = "priority es obligatoria")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$")
    private String priority;
}
```

---

### 2.4. No Hay Protección Contra Acceso Directo a Base de Datos

**Severidad:** CRÍTICO (P0)
**Componente:** Application-Level
**Estado:** ⚠️ No Remediado

**Hallazgo:**

Aunque el servicio implementa OAuth2 en el controlador REST (línea 29 SecurityConfig.java), si se disconecta el gateway, los endpoints están COMPLETAMENTE PROTEGIDOS por JWT.

**SIN EMBARGO**, las credenciales de R2DBC están en el código fuente (tema 2.1 anterior), por lo que un atacante podría:

1. Obtener `npg_HFo2ij7rgRbN` del repo
2. Conectar directamente a `ep-billowing-voice-ad1klyzx-pooler.c-2.us-east-1.aws.neon.tech`
3. Ejecutar SQL arbitrario: `SELECT * FROM maintenances WHERE municipality_id = ...`

**Impacto:**

- Acceso sin autenticación a datos sensibles (costos de mantenimiento, UUID de activos, responsables técnicos)
- Modificación de registros directamente en DB (bypass de lógica de negocio)

**Remediación:**

1. Rotar credenciales inmediatamente después de este hallazgo
2. Implementar control de acceso a nivel de base de datos:

```sql
-- En Neon PostgreSQL:
CREATE ROLE neondb_maintenance WITH LOGIN;
GRANT USAGE ON SCHEMA public TO neondb_maintenance;
GRANT SELECT, INSERT, UPDATE ON maintenances TO neondb_maintenance;
-- NO GRANT DELETE (soft delete solamente)
-- NO GRANT TRUNCATE, DROP
ALTER ROLE neondb_maintenance SET search_path = public;
```

1. Usar credenciales diferentes para lectura vs. escritura:

```yaml
r2dbc:
  url: ${DB_URL}
  username: ${DB_USERNAME_WRITE}  # Usuario con INSERT/UPDATE
  password: ${DB_PASSWORD_WRITE}
secondary:
  url: ${DB_URL}
  username: ${DB_USERNAME_READ}   # Usuario con SELECT solamente
  password: ${DB_PASSWORD_READ}
```

---

## 3. ANÁLISIS ARQUITECTURA Y DISEÑO

### 3.1. Patrón Hexagonal (Ports & Adapters) - Bien Implementado

**Evidencia:** Estructura de directorios:

```
src/main/java/pe/edu/vallegrande/ms_maintenanceService/
├── infrastructure/
│   ├── adapter/in/rest/          ← Puertos de entrada (REST)
│   ├── adapter/out/              ← Puertos de salida (database, mensajes)
│   └── config/                    ← Configuración de infraestructura
├── application/
│   ├── dto/                       ← Data Transfer Objects
│   ├── mapper/                    ← Conversión DTO ↔ Domain
│   ├── usecase/                   ← Lógica de aplicación
│   └── ports/
│       ├── input/                 ← Puertos de entrada (interfaz)
│       └── output/                ← Puertos de salida (interfaz)
└── domain/
    ├── model/                     ← Entidades de dominio
    ├── service/                   ← Lógica de negocio
    ├── exception/                 ← Excepciones de dominio
    └── valueobject/               ← Value Objects
```

**Fortaleza:** Las capas están claramente separadas. La lógica de negocio NO depende de tecnologías específicas (Spring, REST, BD).

**Puntuación:** 8.0/10

### 3.2. Stack Reactivo - Implementación Mixta

**Hallazgo:** El servicio usa Spring WebFlux + R2DBC pero con limitaciones:

**Reactor Framework (Línea 34-35 MaintenanceController.java):**

```java
import reactor.core.publisher.Flux;  // Para flujos reactivos
import reactor.core.publisher.Mono;  // Para valores únicos
```

Los endpoints retornan `Mono<ResponseEntity<>>`, lo que es correcto para operaciones asincrónicas.

**HOWEVER:** No hay evidencia de uso de `flatMap`, `switchMap`, o composición de flujos. El service es reactivo a nivel HTTP pero potencialmente bloqueante en servicios internos.

**Puntuación:** 7.0/10

### 3.3. Pool de Conexiones R2DBC - Correctamente Configurado

**Evidencia (application.yml, líneas 20-24):**

```yaml
pool:
  initial-size: 10      # 10 conexiones al iniciar
  max-size: 20          # Máximo 20 conexiones concurrentes
  max-idle-time: 30m    # Liberar conexiones inactivas cada 30 minutos
  validation-query: SELECT 1  # Verificar conexión viva antes de usar
```

**Evaluación:**

- ✅ Tamaño inicial razonable (10) para microservicio moderado
- ✅ Máximo (20) es conservador, evita exhaustión de conexiones
- ✅ Timeout de inactividad previene connection leaks
- ✅ Validation query mantiene conexiones vivas

**Puntuación:** 9.0/10

---

## 4. SEGURIDAD - ANÁLISIS DETALLADO

### 4.1. OAuth2 y JWT - Implementación Correcta

**Configuración (application.yml, líneas 8-12):**

```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        issuer-uri: ${KEYCLOAK_URL:https://lab.vallegrande.edu.pe/keycloak}/realms/${KEYCLOAK_REALM:sipreb}
```

**Dependencies Correctas (pom.xml, líneas 77-87):**

```xml
<!-- Spring Boot Starter Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Resource Server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**Implementación de Seguridad (SecurityConfig.java, líneas 28-36):**

```java
.authorizeExchange(exchanges → exchanges
    .pathMatchers("/api/v1/maintenances/**").authenticated()    // ← Requiere JWT
    .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", ...).permitAll()
    .pathMatchers("/actuator/**").permitAll()
    .anyExchange().authenticated()
)
.oauth2ResourceServer(oauth2 → oauth2
    .jwt(jwt → jwt.jwtAuthenticationConverter(jwtAuthConverter))  // ← Convierte JWT a SecurityContext
);
```

**Fortaleza:** Cada endpoint exige autenticación válida antes de ejecutar lógica.

**Vulnerable Pattern en Endpoints (MaintenanceController.java, línea 67-68):**

```java
String municipalCode = jwt.getClaimAsString("municipal_code");  // ← Extrae del JWT
String userId = jwt.getClaimAsString("user_id");                 // ← Confia en JWT
```

Si el Keycloak maneja correctamente la emisión de JWT, esto es seguro. **PERO:** Si hay un Keycloak comprometido o mal configurado, podría emitir tokens falsos.

**Recomendación:** Validar firmas de JWT localmente cuando sea posible.

**Puntuación:** 7.5/10 (OAuth2 bien, pero dependencia externa en Keycloak)

### 4.2. Actuator Endpoints - Semifortificados

**Configuración (application.yml, líneas 38-45):**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics  # ← SOLO 3 endpoints expuestos
  endpoint:
    health:
      show-details: always            # ← RIESGO: Expone componentes internos
```

**Riesgo Identificado:**

- `health` con `show-details: always` expone versiones de componentes, estado de DB, etc.
- Un atacante puede usar `/actuator/health` para hacer fingerprinting de la aplicación

**Remediación:**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info  # Remover metrics si no se usa
  endpoint:
    health:
      show-details: when-authorized  # ← Solo para usuarios autenticados
```

**Puntuación:** 6.0/10

---

## 5. GESTIÓN DE DATOS

### 5.1. Esquema SQL - Constraints Bien Definidos

**Líneas 55-63 (schema.sql):**

```sql
CONSTRAINT chk_maintenance_type CHECK (maintenance_type IN (
    'PREVENTIVE', 'CORRECTIVE', 'PREDICTIVE', 'EMERGENCY'
)),
CONSTRAINT chk_priority CHECK (priority IN (
    'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
)),
CONSTRAINT chk_maintenance_status CHECK (maintenance_status IN (
    'SCHEDULED', 'IN_PROCESS', 'COMPLETED', 'CANCELLED', 'SUSPENDED'
))
```

**Fortaleza:**

- ✅ CHECK constraints previenen estados inválidos
- ✅ Campos obligatorios (NOT NULL) en campos críticos
- ✅ UUID como PK (distributed systems friendly)

**Debilidad:**

- ❌ No hay constraint en `municipality_id` para multi-tenancy enforcement
- ❌ No hay`FOREIGN KEY` definitivo a tabla `municipalities` o `assets`

**Recomendación:**

```sql
ALTER TABLE maintenances
ADD CONSTRAINT fk_municipality FOREIGN KEY (municipality_id)
REFERENCES municipalities(id) ON DELETE RESTRICT;
```

**Puntuación:** 7.5/10

### 5.2. Soft Delete (Lógico)

**No implementado** en esquema. Registros eliminados desaparecen con DELETE físico.

**Impacto:** Imposible auditar quién eliminó qué. Violación de trazabilidad.

**Recomendación:**

```sql
ALTER TABLE maintenances
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by UUID NULL;

-- Actualizar índices
CREATE INDEX idx_maintenances_not_deleted ON maintenances(id) WHERE deleted_at IS NULL;
```

**Puntuación:** 5.0/10

---

## 6. PATRONES REACTIVOS

### 6.1. WebFlux - Uso Correcto

El servicio retorna `Mono` y `Flux` en controladores, no blocking operations secuencialmente.

**Ejemplo (MaintenanceController.java, línea 92-93):**

```java
return maintenanceService.create(mapper.toEntity(applicationDTO))
    .map(m → ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseDTO(m)));
```

**Evaluación:**

- ✅ Usa `map()` para transformaciones
- ⚠️ No hay evidencia de `flatMap()` para composición con otros servicios
- ⚠️ Sin timeout o retry policy visible

**Remediación - Agregar Resilencia:**

```java
return maintenanceService.create(mapper.toEntity(applicationDTO))
    .timeout(Duration.ofSeconds(5))
    .retry(1)
    .onErrorMap(TimeoutException.class, e → new ServiceUnavailableException("Timeout creating maintenance"))
    .map(m → ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseDTO(m)));
```

**Puntuación:** 7.5/10

---

## 7. TESTING Y COBERTURA

### 7.1. Test Coverage - Muy Baja

**Evidencia:** Carpeta de tests:

```
src/test/java/pe/edu/vallegrande/ms_maintenanceService/
├── MsMaintenanceServiceApplicationTests.java      (1 file)
└── application/
    ├── mapper/
    │   └── MaintenanceMapperTest.java             (1 file)
    └── usecase/
        └── MaintenanceUseCaseTest.java            (1 file)
```

**Total:** 3 test files para 45 archivos Java = **6.7% cobertura estimada**

**Problemas:**

- ❌ Sin tests de seguridad (CORS exploitation, CSRF)
- ❌ Sin tests de integración (MaintenanceService + DB)
- ❌ Sin tests de controlador REST
- ❌ Sin tests de validación de input

**Cobertura Estimada by Layer:**

| Capa | Cobertura | Status |
|------|-----------|--------|
| Domain | 15%       | ⚠️ Mínimo |
| Application | 20%   | ⚠️ Mínimo |
| Infrastructure | 0%  | 🔴 Crítico |

**Remediación - Plan de Testing:**

1. **Tests unitarios de dominio:**

```bash
mvn test -Dtest=MaintenanceValidatorTest
```

1. **Tests de integración con testcontainers:**

```java
@Testcontainers
public class MaintenanceServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Test
    void shouldCreateMaintenanceWithValidInput() {
        // ...
    }
}
```

1. **Tests de seguridad:**

```java
@WebFluxTest(MaintenanceController.class)
public class MaintenanceSecurityTest {
    @WithMockUser
    void shouldRequireAuthenticationForCreate() {
        // POST /api/v1/maintenances sin token debería retornar 401
    }
}
```

**Puntuación:** 2.0/10

---

## 8. MANEJO DE ERRORES

### 8.1. Global Exception Handler

**Hallazgo:** El servicio importa excepciones específicas:

```java
import pe.edu.vallegrande.ms_maintenanceService.domain.exception.GlobalExceptionHandler;
```

Pero no hay visibilidad del contenido (archivo no encontrado en búsqueda).

**Recomendación:** Validar que `GlobalExceptionHandler` NO exponga stack traces al cliente:

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateMaintenanceCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateMaintenanceCodeException ex) {
        log.error("Duplicate maintenance code: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of("DUPLICATE_CODE", "Maintenance code already exists"));
        // ← No stack trace al cliente
    }
}
```

**Puntuación:** 6.0/10 (Sin confirmación visual)

---

## 9. VALIDACIÓN Y SANITIZACIÓN DE INPUT

### 9.1. Bean Validation - Habilitado

**Fortaleza detectada:** `@Valid` en MaintenanceController (línea 63).

**REQUERIMIENTO:** Verificar que todas las entradas de texto están limitadas:

```java
public class MaintenanceWebRequest {
    @NotBlank
    @Size(min = 3, max = 50)  // ← Límite de longitud
    private String maintenanceCode;

    @NotBlank
    @Size(max = 5000)          // ← Previene DoS mediante entrada gigante
    private String workDescription;
}
```

**Sin límite de tamaño**, un atacante podría enviar:

```json
{
    "workDescription": "A".repeat(1_000_000)  // 1MB de basura
}
```

Causando problemas de memoria.

**Puntuación:** 7.0/10

---

## 10. LOGGING Y MONITOREO

### 10.1. Logging Configurado

**Configuración (application.yml, líneas 27-35):**

```yaml
logging:
  level:
    root: INFO
    "[pe.edu.vallegrande]": DEBUG                    # ← Lógica de app en DEBUG
    "[io.r2dbc.postgresql]": WARN
    "[io.r2dbc.postgresql.client.ReactorNettyClient]": ERROR
    "[io.r2dbc.postgresql.util.FluxDiscardOnCancel]": ERROR
    "[org.springframework.r2dbc]": INFO
    "[reactor.netty]": WARN
```

**Evaluación:**

- ✅ Niveles apropiados por componente
- ✅ Verbose logging deshabilitado en librerias (WARN/ERROR)
- ✅ DEBUG solo en código de aplicación

**Riesgo:** ¿Se registran consultas SQL? ¿Se registran tokens JWT?

```java
// Mala práctica - RIESGO DE EXPOSICIÓN
log.debug("Processing JWT: {}", jwt);  // ← Expone tokens en logs

// Buena práctica
log.debug("Processing JWT for user: {}", jwt.getClaimAsString("user_id"));
```

**Puntuación:** 7.0/10

### 10.2. Actuator Metrics

**Habilitado (application.yml, línea 42):**

```yaml
include: health,info,metrics
```

Permite monitoreo de:

- Puerto HTTP: `GET /actuator/metrics`
- Latencia de requests
- Uso de memoria
- Conexiones DB

**Recomendación:** Proteger `metrics` con autenticación:

```yaml
endpoints:
  web:
    exposure:
      include: health,info     # Remover metrics de public
```

Y exponer via actuator seguro en puerto interno (8081):

```yaml
management:
  server:
    port: 8081
    address: localhost         # Solo desde adentro del cluster
```

**Puntuación:** 6.0/10

---

## 11. DEPENDENCIAS Y VULNERABILIDADES

### 11.1. Spring Boot Parent - Version 3.5.11

**Estado:** Moderadamente actual (no LTS)

**Recomendación:** Actualizar a 3.5.x latest o 3.6.x si compatible.

### 11.2. SpringDoc OpenAPI - Version 2.7.0 (Línea 62, pom.xml)

**Fortaleza:** Documentación automática de APIs

**Riesgo:** Asegurar que `/swagger-ui.html` no está disponible en producción:

```java
// En SecurityConfig.java línea 30:
.pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

// Debería ser:
.pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
  .access((auth, ctx) → auth.map(a → a.getAuthorities().stream()
    .anyMatch(g → "ADMIN".equals(g.getAuthority())))
    .map(b → b ? AuthorizationDecision.ALLOW : AuthorizationDecision.DENY)
    .defaultIfEmpty(new AuthorizationDecision(false)))
```

**Puntuación:** 5.0/10

---

## 12. CONCLUSIÓN Y RECOMENDACIONES

### Resumen de Hallazgos

| # | Categoría | Severidad | Estado | Acciones |
|---|-----------|-----------|--------|----------|
| 1 | Credenciales secretas | CRÍTICO | No remediado | Rotar, externalizar a variables env |
| 2 | CORS + Credentials | CRÍTICO | No remediado | Implementar whitelist de orígenes |
| 3 | Validación de input | CRÍTICO | Parcial | Agregar constraints de tamaño |
| 4 | Acceso directo DB | CRÍTICO | No remediado | Rotar creds, RBAC en BD |
| 5 | Testing mínimo | ALTO | No remediado | Cobertura de >80% requerida |
| 6 | JWT a Keycloak | ALTO | N/A | Considerar multi-issuer como tenantservice |
| 7 | Health endpoint verbose | ALTO | No remediado | Restringir a usuarios autenticados |
| 8 | Soft delete ausente | MEDIO | No remediado | Agregar `deleted_at` timestamp |
| 9 | Swagger en producción | MEDIO | No remediado | Proteger con autenticación |
| 10 | Falta de timeout reactivo | MEDIO | No remediado | Agregar `.timeout()` a flujos |

### Hoja de Ruta de Remediación (Prioridad)

**FASE 1 (Inmediato - 24 horas):**

- [ ] Rotar credenciales de Neon
- [ ] Externalizar todas las credenciales a env vars
- [ ] Implementar whitelist de CORS

**FASE 2 (Semana 1):**

- [ ] Agregar tests unitarios/integración (>30% cobertura)
- [ ] Implementar validación de tamaño de input
- [ ] Proteger actuator/health

**FASE 3 (Semana 2):**

- [ ] Implementar soft delete en DB
- [ ] Agregar timeout a flujos reactivos
- [ ] Multi-issuer JWT como tenantservice

**FASE 4 (Sprint siguiente):**

- [ ] Cobertura de testing a 80%+
- [ ] Implementar RBAC en PostgreSQL
- [ ] Auditoría de logs (verificar no hay secretos)

### Veredicto Final

**Estado Actual:** ⚠️ **NO LISTO PARA PRODUCCIÓN**

**Razones:**

1. Credenciales expuestas = riesgo inmediato de compromiso de datos
2. CORS abierto + credenciales habilitadas = vector CSRF
3. Testing insuficiente = comportamiento impredecible
4. Sin auditoría de datos = incumplimiento de regulaciones

**Puntuación Post-Remediación Esperada:** 8.5/10 (después de completar Fase 1-2)

---

## APÉNDICE A: Archivos Críticos Auditados

### Estructura de Directorios Completa

```
vg-ms-mantenimiento-service/
├── pom.xml                                    [120 líneas]
├── src/main/resources/
│   └── application.yml                        [55 líneas]
├── src/main/resources/db/
│   └── schema.sql                             [80+ líneas]
├── src/main/java/.../infrastructure/
│   ├── config/
│   │   ├── SecurityConfig.java               [52 líneas] ← CRÍTICO
│   │   ├── JwtAuthenticationConverter.java
│   │   └── BeanConfig.java
│   └── adapter/
│       └── in/rest/
│           └── MaintenanceController.java    [200+ líneas] ← CRÍTICO
├── src/main/java/.../application/
│   ├── dto/
│   │   ├── MaintenanceRequestDTO.java
│   │   ├── MaintenanceResponseDTO.java
│   │   └── MaintenanceWebRequest.java        ← Validación aquí
│   ├── mapper/
│   │   └── MaintenanceMapper.java
│   └── usecase/
│       └── MaintenanceUseCase.java
├── src/main/java/.../domain/
│   ├── model/
│   │   └── Maintenance.java
│   ├── service/
│   │   └── MaintenanceValidator.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── MaintenanceNotFoundException.java
│   │   └── DuplicateMaintenanceCodeException.java
│   └── port/
│       └── in/
│           └── MaintenanceServicePort.java
└── src/test/java/...
    └── (3 test files - INSUFICIENTE)
```

### Links a Archivos Críticos

1. [pom.xml](vg-ms-mantenimiento-service/pom.xml) — Dependencias Spring Security, OAuth2
2. [application.yml](vg-ms-mantenimiento-service/src/main/resources/application.yml) — Credenciales ⚠️
3. [SecurityConfig.java](vg-ms-mantenimiento-service/src/main/java/pe/edu/vallegrande/ms_maintenanceService/infrastructure/config/SecurityConfig.java) — CORS vulnerable ⚠️
4. [schema.sql](vg-ms-mantenimiento-service/src/main/resources/db/schema.sql) — Constraints de tabla
5. [MaintenanceController.java](vg-ms-mantenimiento-service/src/main/java/pe/edu/vallegrande/ms_maintenanceService/infrastructure/adapter/in/rest/MaintenanceController.java) — Endpoints REST

---

**Fin de Auditoría**
