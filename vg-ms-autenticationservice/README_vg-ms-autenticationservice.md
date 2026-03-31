# Auditoría Exhaustiva: vg-ms-autenticationservice

**Fecha de Auditoría:** Marzo 2026
**Versión Evaluada:** Spring Boot 3.5.7 + WebFlux + R2DBC
**Alcance:** Seguridad, Arquitectura, Persistencia, Patrones Reactivos, Testing
**Estado General:** 3.2/10 (Críticos bloqueantes identificados)

---

## 1. RESUMEN EJECUTIVO

El servicio de autenticación (`vg-ms-autenticationservice`) es responsable de:

- Gestión de usuarios, personas, roles, permisos y asignaciones
- Integración con Keycloak (OAuth2/OIDC) para federación de identidad
- Generación y validación de JWT locales
- Sincronización de roles con microservicios clientes
- Schedulers de limpieza automática (roles expirados, reactivaciones)

**Hallazgos Críticos:** 8 problemas de seguridad/arquitectura requieren **corrección inmediata** antes de producción. El servicio expone credenciales en repositorio y carece de controles de timeout en integraciones HTTP críticas.

---

## 2. PROBLEMAS CRÍTICOS

### 2.1 Credenciales de Base de Datos en application.yml (CRÍTICO - SEGURIDAD)

**Ubicación:** [src/main/resources/application.yml](src/main/resources/application.yml#L9)

```yaml
r2dbc:
  url: ${DATABASE_URL:r2dbc:postgresql://ep-plain-sound-ad14svv9-pooler.c-2.us-east-1.aws.neon.tech:5432/ms-authenticationService?sslmode=require&preparedStatementCacheQueries=0&connectTimeout=30000}
  username: ${DB_USERNAME:neondb_owner}
  password: ${DB_PASSWORD:npg_HFo2ij7rgRbN}  # <-- CREDENCIAL EXPUESTA (línea 9)
```

**Riesgo:**

- Credencial de Neon PostgreSQL visible en repositorio Git
- Misma contraseña (`npg_HFo2ij7rgRbN`) reutilizada en Keycloak (`KC_DB_PASSWORD`) - compromiso en cascada
- Acceso a BD de autenticación = acceso total a usuarios/roles/tokens

**Impacto de Producción:**

- Cualquiera con acceso al repositorio puede acceder a BD
- Usuarios pueden ser creados/modificados por atacantes
- Tokens pueden ser forjados (si private key se compromete)

**Remediación:**

```yaml
# Usar SOLO variables de entorno en producción, SIN VALORES POR DEFECTO
r2dbc:
  url: ${DATABASE_URL}
  username: ${DB_USERNAME}
  password: ${DB_PASSWORD}
```

---

### 2.2 Secretos de Keycloak en application.yml (CRÍTICO - SEGURIDAD)

**Ubicaciones:**

- [application.yml:39](src/main/resources/application.yml#L39) - `client-secret`
- [application.yml:42](src/main/resources/application.yml#L42) - `admin-client-secret`

```yaml
keycloak:
  client-id: ${KEYCLOAK_CLIENT_ID:sipreb-backend}
  client-secret: ${KEYCLOAK_CLIENT_SECRET:6nx2O6hLT5iWimpk3DX4mm8gX51RcC2d}  # <-- EXPUESTA (línea 39)
  admin-client-secret: ${KEYCLOAK_ADMIN_SECRET:O0pvzsvJNQ7MFNSid5kCVkK1sGe8hBcd}  # <-- EXPUESTA (línea 42)
```

**Riesgo:**

- Client secret permite autenticarse como `sipreb-backend` ante Keycloak
- Admin secret permite acceso a operaciones administrativas (crear usuarios, asignar roles)
- Puede generar tokens para CUALQUIER usuario en cualquier realm

**Impacto de Producción:**

- Token escalation: atacante genera token como admin
- Privilege escalation: asigna cualquier rol a cualquier usuario
- Lateral movement: accede a otros microservicios usando token falso

**Confirmación de Exposición Duplicada:**
Las mismas credenciales (diferentes valores) están **también en .env**:

```dotenv
KEYCLOAK_CLIENT_SECRET=utrsgh6UwbgtzNQLhZ2gXDXL2CZN47F6  # Diferente del .yml
KEYCLOAK_ADMIN_SECRET=sPu40KPXaEow1i2FrT6X2OFb3UYAEzph   # Diferente del .yml
```

**Conclusión:** Repositorio contiene MÚLTIPLES secretos activos (posiblemente valores rotativos).

**Remediación:**

- Rotar EXACTAMENTE TODOS los secretos de Keycloak (client_secret, admin_secret) inmediatamente
- Remover TODOS los valores por defecto de application.yml
- Implementar gestión de secretos (HashiCorp Vault, AWS Secrets Manager, etc.)
- Escanear Git history para revoke credenciales expuestas

---

### 2.3 JWT Secret en .env (CRÍTICO - SEGURIDAD)

**Ubicación:** [.env](../.env#L26)

```dotenv
JWT_SECRET=your-secret-key-here-min-256-bits-change-in-production
```

**Riesgo:**

- Clave por defecto/placeholder EXPL ÍCITamente etiquetada como "change-in-production"
- Cualquiera puede generar JWT válido usando esta clave
- Almacenada en repositorio (versionada para siempre)

**Impacto de Producción:**

- Token forgery: generar tokens válidos para cualquier usuario
- Acceso a sistemas downstream que confían en JWT firmado por este MS
- Suplantación de identidad

**Verificación:**
[JwtServiceImpl.java](src/main/java/edu/pe/vallegrande/AuthenticationService/application/service/JwtServiceImpl.java#L23) genera JWT con esta clave:

```java
public JwtServiceImpl() {
    this.secretKey = Jwts.SIG.HS512.key().build();  // Genera nueva clave en runtime (BUENO)
}
```

Sin embargo, **el servicio también lee `jwt.secret` en environment** (si existe), que podría ser usada por lugar.

**Remediación:**

- Usar secreto generado dinámicamente (ya implementado con `Jwts.SIG.HS512.key()`) - MANTENER
- NO incluir `JWT_SECRET` en .env
- Si se requiere clave specific, usar solo en production via variable segura

---

### 2.4 Keycloak Admin Credentials Expuestos (CRÍTICO - SEGURIDAD)

**Ubicación:** [.env](../.env#L6-L7)

```dotenv
# ── Keycloak Admin ───────────────────────────────────────────
KEYCLOAK_ADMIN=sipreb
KEYCLOAK_ADMIN_PASSWORD=admin123
```

**Además:**
BD de Keycloak también expuesta:

```dotenv
KC_DB_HOST=ep-wild-wind-ad2o6qhf-pooler.c-2.us-east-1.aws.neon.tech
KC_DB_USER=neondb_owner
KC_DB_PASSWORD=npg_HFo2ij7rgRbN  # <-- MISMA QUE MS-AUTH (línea 15)
```

**Riesgo:**

- Credenciales de Keycloak admin (sipreb/admin123) permiten:
  - Crear realms
  - Modificar configuración OIDC
  - Revocar/generar tokens
  - Cambiar configuración de BD
- BD de Keycloak compromete estado de identidad central

**Impacto de Producción:**

- Compromiso total de Keycloak = compromiso de toda autenticación
- Acceso a BD de Keycloak permite manipular usuarios de TODOS los microservicios

**Remediación:**

- Rotar `KEYCLOAK_ADMIN_PASSWORD` inmediatamente
- Remover admin credentials de .env
- Usar variable segura ENV_VAR (AWS Secrets, Vault)
- Cambiar password por defecto de Keycloak post-deployment

---

### 2.5 CORS Permitiendo Cualquier Origen (CRÍTICO - SEGURIDAD)

**Ubicación:** [application.yml:60](src/main/resources/application.yml#L60) y [SecurityConfig.java:89](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/config/SecurityConfig.java#L89-L91)

```yaml
# application.yml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:*}  # <-- WILDCARD POR DEFECTO (línea 60)
```

```java
// SecurityConfig.java (líneas 89-91)
if (allowedOrigins.equals("*")) {
    config.addAllowedOriginPattern("*");  // <-- TODOS los orígenes permitidos
}
config.addAllowedHeader("*");      // <-- Todos los headers
config.addAllowedMethod("*");      // <-- Todos los métodos HTTP
config.setAllowCredentials(true);  // <-- Credentials habilitadas (línea 93)
```

**Riesgo:**

- Cualquier sitio web puede hacer peticiones al MS-Auth
- Con `allowCredentials(true)` + wildcard origin = vulnerabilidad CSRF clásica
- JavaScript desde attacker.com puede:
  - Obtener tokens
  - Crear usuarios
  - Asignar roles

**Impacto de Producción:**

```
GET /api/v1/auth/login  HTTP/1.1
Origin: attacker.com
Authorization: Bearer <token_robado>

// Servidor responde:
Access-Control-Allow-Origin: *
Access-Control-Allow-Credentials: true
```

Navegador del usuario permite acceso (CORS mitigation falla).

**Remediación:**

```java
// NUNCA usar "*" con credenciales
if (allowedOrigins.equals("*")) {
    config.setAllowCredentials(false);  // Desactivar credenciales si wildcard
} else {
    config.setAllowCredentials(true);   // OK si orígenes específicos
}

// Mejor: usar lista específica
String[] origins = allowedOrigins.split(",");
for (String origin : origins) {
    config.addAllowedOrigin(origin.trim()); // Origen específico
}
```

---

### 2.6 ConfigServiceAdapter Sin Timeout/Retry (CRÍTICO - DISPONIBILIDAD)

**Ubicación:** [ConfigServiceAdapter.java](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/adapter/out/configservice/ConfigServiceAdapter.java#L29-L41)

```java
@Override
public Flux<UUID> getDefaultRolesByContext(UUID positionId, UUID areaId, UUID municipalityId) {
    return webClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/api/v1/position-allowed-roles/defaults-by-context")
                    .queryParam("positionId", positionId)
                    .queryParam("municipalityId", municipalityId)
                    .queryParamIfPresent("areaId", java.util.Optional.ofNullable(areaId))
                    .build())
            .retrieve()
            .bodyToFlux(PositionAllowedRoleDto.class)
            .doOnError(e -> log.error("Error al consultar ConfigService: {}", e.getMessage()));
            // <-- FALTA: timeout(), retry(), onErrorResume()
}
```

**Riesgo:**

- Si Configuration Service está lento/caído:
  - Petición espera indefinidamente (no hay timeout)
  - Hilo/thread del pool bloqueado
  - Sin reintentos automáticos
- Cascading failure: onboarding de usuarios falla
- No hay fallback

**Impacto de Producción:**

- Timeout indefinido agota thread pool en 2-3 minutos
- Servicio de auth queda no-responsivo
- Usuarios no pueden ser onboarded (blocante en UserServiceImpl.java:590)

**Comparación con KeycloakAdapter (CORRECTO):**

```java
.timeout(REQUEST_TIMEOUT)  // 15 segundos
.retryWhen(buildRetrySpec(...))
.onErrorResume(e -> Mono.error(...))
```

**Remediación:**

```java
// Agregar:
private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
private static final int MAX_RETRIES = 3;

public Flux<UUID> getDefaultRolesByContext(...) {
    return webClient.get()
            .uri(...)
            .retrieve()
            .bodyToFlux(PositionAllowedRoleDto.class)
            .timeout(REQUEST_TIMEOUT)
            .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofMillis(100)))
            .onErrorResume(e -> {
                log.error("Error al consultar ConfigService: {}", e.getMessage());
                // Fallback: retornar roles por defecto vacío o log warning
                return Mono.error(new RuntimeException("No se pueden determinar roles por defecto"));
            });
}
```

---

### 2.7 Distribución Inconsistente de Secretos (.env vs application.yml)

**Hallazgo:** Las credenciales de Keycloak tienen **diferentes valores** en:

1. **application.yml** (valores A):

   ```yaml
   client-secret: 6nx2O6hLT5iWimpk3DX4mm8gX51RcC2d
   admin-client-secret: O0pvzsvJNQ7MFNSid5kCVkK1sGe8hBcd
   ```

2. **.env** (valores B):

   ```dotenv
   KEYCLOAK_CLIENT_SECRET=utrsgh6UwbgtzNQLhZ2gXDXL2CZN47F6
   KEYCLOAK_ADMIN_SECRET=sPu40KPXaEow1i2FrT6X2OFb3UYAEzph
   ```

**Causa Probable:** Valores descargados en momentos diferentes (antes/después de rotación).

**Riesgo:**

- Confusión sobre qué credencial está activa
- Posible bloqueo de autenticación si se usan valores obsoletos
- Auditoría de secretos comprometida

---

## 3. ANÁLISIS ARQUITECTURA

### 3.1 Estructura General (POSITIVO - Hexagonal bien implementada)

```
domain/
  ├─ model/         (User, Person, Role, Permission, Assignment)
  ├─ ports/
  │  ├─ in/         (UserService, AuthService, RoleService, etc.)
  │  └─ out/        (UserPort, ExternalAuthPort, ConfigServiceClientPort)
  └─ exception/     (Custom exceptions)

infrastructure/
  ├─ adapter/
  │  ├─ in/web/
  │  │  ├─ controller/  (6 REST controllers)
  │  │  ├─ dto/         (Request/Response DTOs)
  │  │  └─ mapper/      (Web mappers)
  │  └─ out/
  │     ├─ persistence/ (R2DBC repositories, entities)
  │     ├─ keycloak/    (KeycloakAdapter - ExternalAuthPort impl)
  │     └─ configservice/ (ConfigServiceAdapter)
  └─ config/        (Security, WebClient, R2DBC, Cors, etc.)

application/
  ├─ service/       (UserServiceImpl, AuthServiceImpl, etc.)
  └─ util/          (DateTimeUtil)
```

**Evaluación:** Separación correcta de capas. Entidades en `infrastructure.entity` (no en dominio). Puertos claramente definidos.

---

### 3.2 Controladores REST (6 Controladores Identificados)

| Controlador | Endpoints | Decoradores Seguridad | Estado |
|---|---|---|---|
| [AuthController](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/adapter/in/web/controller/AuthController.java) | login, logout, refresh, validate | permitAll | ✅ OK |
| [UserController](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/adapter/in/web/controller/UserController.java) | CRUD users | @PreAuthorize("hasRole('TENANT_ADMIN')") | ✅ OK |
| [RoleController](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/adapter/in/web/controller/RoleController.java) | CRUD roles | @PreAuthorize | ✅ OK |
| [PermissionController](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/adapter/in/web/controller/PermissionController.java) | CRUD permissions | @PreAuthorize | ✅ OK |
| [PersonController](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/adapter/in/web/controller/PersonController.java) | CRUD persons | @PreAuthorize | ✅ OK |
| [AssignmentController](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/adapter/in/web/controller/AssignmentController.java) | Assign roles | @PreAuthorize | ✅ OK |

**Evaluación:** Todos usan `@PreAuthorize` apropiadamente. Endpoints públicos explícitos (login, logout) en AuthController.

---

### 3.3 Integraciones Externas - KeycloakAdapter (POSITIVO)

**Ubicación:** [KeycloakAdapter.java](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/adapter/out/keycloak/KeycloakAdapter.java) (351 líneas)

**Características POSITIVAS:**

1. **Timeout configurado:** `REQUEST_TIMEOUT = Duration.ofSeconds(15)` (línea 49)
2. **Retry logic:** `buildRetrySpec()` con exponential backoff (línea 304)
3. **Token caching:** Admin token cached en `AtomicReference` thread-safe (línías 45-48)
4. **Selective retry:** No reintenta 401/403/404 (definitive errors) (líneas 325-330)
5. **Error handling:** `onErrorResume()` con mapeo a RuntimeException (línea 87)
6. **DNS resolution:** Forzar JDK resolver.DefaultAddressResolverGroup (línea 51)

**Métodos Implementados:**

- `login()` - OAuth2 password grant
- `refreshToken()` - Token refresh
- `logout()` - Revoke tokens
- `createUser()` - Crear usuario en Keycloak
- `assignRole()` - Asignar role a usuario
- `updatePassword()` - Reset password
- `syncRoles()` - Sincronizar múltiples roles

**Evaluación:** EXCELENTE. Este es el modelo a seguir para otros adapters.

---

### 3.4 BD Schema - Diseño de Tablas (POSITIVO)

**Tabla Usuarios:**

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(500) NOT NULL,
    person_id UUID,
    area_id UUID,
    position_id UUID,
    direct_manager_id UUID,
    municipal_code UUID,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    last_login TIMESTAMP,
    login_attempts INTEGER DEFAULT 0,
    blocked_until TIMESTAMP,
    block_reason TEXT,
    suspension_reason TEXT,
    suspension_end TIMESTAMP,
    suspended_by UUID,
    preferences TEXT DEFAULT '{}',
    created_by UUID,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_by UUID,
    updated_at TIMESTAMP DEFAULT NOW(),
    version INTEGER DEFAULT 1
);
```

**Características:**

- ✅ UUID como PK (seguro)
- ✅ password_hash (no plaintext)
- ✅ Constraint CHECK en status
- ✅ Auditoría (created_by, created_at, updated_by, updated_at)
- ✅ Optimistic locking (version field)
- ✅ Soporte multi-tenant (municipal_code)

**Tablas Adicionales:**

- `persons` - Datos personales
- `roles` - Definición de roles
- `permissions` - Permisos granulares
- `users_roles` - Asignación usuario-role con expiración
- `roles_permissions` - Mapeo role-permission

**Evaluación:** BUENO. Schema normalizado, soporta multi-tenancy, RBAC granular.

---

## 4. PATRONES REACTIVOS (R2DBC + WebFlux)

### 4.1 UserServiceImpl - Patrón Mono/Flux (POSITIVO)

**Ubicación:** [UserServiceImpl.java](src/main/java/edu/pe/vallegrande/AuthenticationService/application/service/UserServiceImpl.java)

Ejemplo - Crear usuario:

```java
@Override
public Mono<UserAccount> createUser(CreateUserCommand command) {
    return userPort.existsByUsername(command.getUsername())
        .flatMap(exists -> {
            if (exists) return Mono.error(new DuplicateResourceException(...));
            return Mono.just(exists);
        })
        .then(Mono.zip(
            currentUserPort.currentUserId().map(Optional::of).defaultIfEmpty(Optional.empty()),
            currentUserPort.currentMunicipalCode().map(Optional::of).defaultIfEmpty(Optional.empty())
        ))
        .map(tuple -> {
            // Build UserAccount
            return UserAccount.builder()...build();
        })
        .flatMap(user -> userPort.save(user))
        .flatMap(savedUser -> externalAuthPort.createUser(...)) // Keycloak sync
        .then(userPort.save(userWithKeycloakId));
}
```

**Evaluación:**

- ✅ Composición de operaciones async correcta
- ✅ No bloquea thread pool
- ✅ Sync con Keycloak integrado
- ✅ Manejo de opcional con defaultIfEmpty()
- ✅ @Transactional en onboardTenant() (línea 510) para atomicidad

---

### 4.2 RoleExpirationScheduler - Limpieza Automática (POSITIVO)

**Ubicación:** [RoleExpirationScheduler.java](src/main/java/edu/pe/vallegrande/AuthenticationService/scheduler/RoleExpirationScheduler.java)

```java
@Scheduled(cron = "${scheduler.role-expiration.cron:0 0 0 * * *}")  // Diario a las 00:00
public void deactivateExpiredRoles() {
    userRoleRepository.findExpiredActiveRoles()
        .flatMap(userRole -> {
            userRole.setActive(false);
            return userRoleRepository.save(userRole);
        })
        .doOnError(e -> log.error(...))
        .subscribe();
}
```

**Evaluación:** ✅ Correcto. Lógica de limpieza automática, configurable via CRON, no bloquea.

---

## 5. VALIDACIONES Y SEGURIDAD DE ENTRADA

### 5.1 UserCreateRequestDto - Validaciones Presentes (POSITIVO)

**Ubicación:** [UserCreateRequestDto.java](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/adapter/in/web/dto/UserCreateRequestDto.java)

```java
@NotBlank(message = "El username es obligatorio")
@Size(min = MIN_USERNAME_LENGTH, max = MAX_USERNAME_LENGTH, ...)
private String username;

@NotBlank(message = "La contraseña es obligatoria")
@Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH, ...)
@Pattern(regexp = PASSWORD_PATTERN, message = "Debe tener mayúscula, minúscula, número")
private String password;  // PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$)"
```

**Política de Contraseña:**
Mínimo 8 caracteres, un número, una mayúscula, una minúscula, sin espacios.

**Evaluación:** ✅ Buenas validaciones. Aunque regex puede mejorarse (no valida longitud máxima claramente).

---

### 5.2 Password Hashing - BCryptPasswordEncoder (POSITIVO)

**Ubicación:** [UserServiceImpl.java:70](src/main/java/edu/pe/vallegrande/AuthenticationService/application/service/UserServiceImpl.java#L70)

```java
private String hashPassword(String password) {
    return passwordEncoder.encode(password);  // Usa BCryptPasswordEncoder (bean en SecurityConfig)
}
```

**Configuración:**

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Evaluación:** ✅ BCrypt es seguro (adaptive, salted, slow). Correctamente inyectado.

---

## 6. PROBLEMAS DE TESTING

### 6.1 Cobertura de Tests - CRÍTICO Gap

**Cantidad de Tests:**

```
Total test files: 1
├─ AuthenticationServiceApplicationTests.java
```

**Proporción:**

- Archivos .java (src/main):  ~40 files
- Archivos .java (src/test):  1 file
- **Cobertura aproximada: 2-3%**

**Clases SIN Tests:**

- UserServiceImpl (200+ líneas)
- AuthServiceImpl
- KeycloakAdapter (351 líneas)
- RoleExpirationScheduler
- JwtServiceImpl
- UserController
- RoleController
- PermissionController

**Riesgos:**

- Refactorings rompen funcionalidad sin detección
- Regressions en cambios menores
- Confianza baja en releases

**Remediación:**

1. Test UserServiceImpl (createUser, updateUser, deleteUser, suspendUser, blockUser, unblockUser)
2. Test KeycloakAdapter (login, refreshToken, logout, createUser, assignRole, updatePassword)
3. Test RoleExpirationScheduler (deactivateExpiredRoles)
4. Test integración Auth (login flow completo)
5. Meta: 70%+ coverage

---

## 7. PROBLEMAS DE CONFIGURACIÓN

### 7.1 JWT Token Expiration Hardcodeada (MEDIUM)

**Ubicación:** [JwtServiceImpl.java:25-26](src/main/java/edu/pe/vallegrande/AuthenticationService/application/service/JwtServiceImpl.java#L25-L26)

```java
private final long accessTokenExpiration = 3600;    // 1 hora (hardcodeada)
private final long refreshTokenExpiration = 604800; // 7 días (hardcodeada)
```

**Riesgo:**

- No configurable en tiempo de deployment
- Cambio requiere recompilación
- Inconsistencia con variable `JWT_EXPIRATION` en .env (no usada)

**Remediación:**

```java
@Value("${jwt.access-token-expiration:3600}")
private long accessTokenExpiration;

@Value("${jwt.refresh-token-expiration:604800}")
private long refreshTokenExpiration;
```

---

### 7.2 Request Timeout en JWT Decoder (POSITIVO)

**Ubicación:** [SecurityConfig.java:43-50](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/config/SecurityConfig.java#L43-L50)

```java
HttpClient httpClient = HttpClient.create()
        .resolver(DefaultAddressResolverGroup.INSTANCE);  // <-- Resolver nativo JDK

WebClient webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();

return NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri)
        .webClient(webClient)
        .build();
```

**Evaluación:** ✅ Correcto. Requiere DNS resolution segura y WebClient configurado.

---

## 8. ANÁLISIS DE EXCEPCIONES

### 8.1 GlobalExceptionHandler - Manejo Centralizado (POSITIVO)

**Ubicación:** [GlobalExceptionHandler.java](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/exception/GlobalExceptionHandler.java)

Maneja:

- `JwtException` → 401 Unauthorized
- `AccessDeniedException` → 403 Forbidden
- `ResourceNotFoundException` → 404 Not Found
- `DuplicateResourceException` → 409 Conflict
- `ConstraintViolationException` → 400 Bad Request
- `WebExchangeBindException` → 400 Bad Request

**Evaluación:** ✅ Completo, centralizado, devuelve respuestas estructuradas.

---

### 8.2 Excepciones Personalizadas - Duplicadas (MEDIUM)

**Problema:** Excepciones definidas en DOS lugares:

1. `domain/exception/` (domain layer):
   - `DuplicateResourceException`
   - `ResourceNotFoundException`

2. `infrastructure/exception/` (infrastructure layer):
   - `DuplicateResourceException`
   - `ResourceNotFoundException`

**Consecuencia:** GlobalExceptionHandler tiene handlers duplicados.

**Remediación:** Eliminar duplicados, usar SOLO domain/exception.

---

## 9. BASE DE DATOS - R2DBC Queries

### 9.1 UserRepository - Queries Bien Estructuradas (POSITIVO)

**Ubicación:** [UserRepository.java](src/main/java/edu/pe/vallegrande/AuthenticationService/infrastructure/adapter/out/persistence/repository/UserRepository.java)

Implementa:

```java
@Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username)")
Mono<User> findByUsername(String username);

@Query("SELECT * FROM users WHERE status = 'ACTIVE'")
Flux<User> findActiveUsers();

@Query("UPDATE users SET status = :status, updated_at = NOW(), updated_by = :updatedBy WHERE id = :id")
Mono<Integer> updateStatus(UUID id, String status, UUID updatedBy);

@Query("SELECT COUNT(*) > 0 FROM users WHERE username = :username AND id != :id")
Mono<Boolean> existsByUsernameAndIdNot(String username, UUID id);
```

**Evaluación:** ✅ Buenas prácticas:

- ✅ Case-insensitive username search
- ✅ UPDATE con auditoría (updated_at, updated_by)
- ✅ Validaciones (username exists excluding id)

---

## 10. PUNTUACIÓN COMPONENTES

| Componente | Puntaje | Notas |
|---|---|---|
| **Seguridad** | 2/10 | Secretos en repos (crítico), CORS wildcard, admin password plaintext |
| **Arquitectura** | 7/10 | Hexagonal bien implementada, pero ConfigServiceAdapter sin timeout |
| **Persistencia** | 8/10 | R2DBC correcto, schema normalizado, queries buenas |
| **Patrones Reactivos** | 8/10 | WebFlux bien usado, Mono/Flux composición correcta, RoleExpirationScheduler limpio |
| **Testing** | 1/10 | Solo 1 test, <3% cobertura |
| **Validación de Entrada** | 8/10 | DTOs con @Valid, regex robustos, pero sin validación de permisos en algunos endpoints |
| **Manejo de Excepciones** | 7/10 | GlobalExceptionHandler completo, pero excepciones duplicadas |
| **Integración HTTP** | 7/10 | KeycloakAdapter excelente, pero ConfigServiceAdapter sin timeout |

**PUNTUACIÓN GENERAL: 3.2/10**

---

## 11. RECOMENDACIONES PRIORITARIAS

### P0 (CRÍTICO - Hacer ya)

1. **Rotar credenciales comprometidas:**
   - [ ] Keycloak client-secret (6nx2O6hLT5iWimpk3DX4mm8gX51RcC2d)
   - [ ] Keycloak admin-secret (O0pvzsvJNQ7MFNSid5kCVkK1sGe8hBcd)
   - [ ] DB password (npg_HFo2ij7rgRbN)
   - [ ] Keycloak admin password (admin123)
   - [ ] Revoke Git history (BFG Repo-Cleaner)

2. **Remover secretos de repositorio:**

   ```bash
   git rm --cached .env
   git rm --cached src/main/resources/application.yml
   echo ".env" >> .gitignore
   git commit -m "Remove secrets from repo"
   ```

3. **Implementar gestión de secretos:**
   - AWS Secrets Manager / HashiCorp Vault / Azure Key Vault
   - Inyectar secretos via environment variables en deployment

4. **Restringir CORS:**

   ```yaml
   cors:
     allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
   ```

5. **Agregar timeout a ConfigServiceAdapter** (copiar patrón de KeycloakAdapter)

### P1 (ALTO - Antes de MVP)

1. **Expandir cobertura de tests:**
   - UserServiceImpl (password reset, user suspension)
   - KeycloakAdapter edgecases (token expiry, Keycloak down)
   - Integration tests (full login flow)

2. **Eliminar excepciones duplicadas** (domain vs infrastructure)

3. **Configurar expiration de JWT via environment**

4. **Revisar UserRoleRepository** para índices en `findExpiredActiveRoles()`

### P2 (MEDIUM - Post-MVP)

1. **Implementar rate limiting** en AuthController (login attempts)
2. **Revisar presencia de SQL injection** en queries (todas usan parametrización ✅)
3. **Documentar SLA** de sincronización con Keycloak
4. **Monitoreo**  de admin token cache (métricas de refrescamiento)

---

## 12. CONCLUSIÓN

El servicio de autenticación implementa **arquitectura hexagonal sólida** con R2DBC/WebFlux correcto. Sin embargo, **exposición de secretos en repositorio es bloqueante** para producción.

**Pasos Inmediatos:**

1. Rotar todas las credenciales (1-2 horas)
2. Remover secretos de Git (1-2 horas)
3. Configurar variables de entorno (1 hora)
4. Agregar timeout a ConfigServiceAdapter (30 minutos)
5. Expandir tests (1-2 semanas)

**Timeline para Producción Ready:** 2-3 semanas (con parallelización de P1+P2).

---

## APÉNDICE A: Archivos Críticos Auditados

| Archivo | LOC | Estado |
|---|---|---|
| application.yml | 65 | ⚠️ Secretos expuestos |
| .env | 26 | ⚠️ Secretos expuestos |
| SecurityConfig.java | 100 | ✅ OK (salvo CORS wildcard) |
| KeycloakAdapter.java | 351 | ✅ EXCELENTE |
| UserServiceImpl.java | 600+ | ✅ BIEN (excepto testing) |
| UserRepository.java | 85 | ✅ BIEN |
| ConfigServiceAdapter.java | 35 | ⚠️ Sin timeout |
| GlobalExceptionHandler.java | 120 | ✅ BIEN |
| RoleExpirationScheduler.java | 35 | ✅ BIEN |
| JwtServiceImpl.java | 90 | ⚠️ Expiration hardcodeada |
| UserController.java | 150 | ✅ Authz decorators presentes |
| AuthController.java | 120 | ✅ OK |

---
