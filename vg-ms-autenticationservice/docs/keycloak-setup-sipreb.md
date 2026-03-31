# 🔑 Guía de Configuración Keycloak — SIPREB
## Sistema Multi-Tenant de Control Patrimonial para Municipalidades

> **Versión Keycloak:** 26.5.4  
> **Patrón:** Realm único `sipreb` — separación por tenant via `municipal_code`  
> **Stack backend:** Java 17+ · Spring Boot 3.x · WebFlux reactivo · Arquitectura Hexagonal  
> **BD Keycloak:** Neon PostgreSQL (nube) · Sin contenedor de BD local  
> **Última actualización:** 2025

---

## 📋 Índice

1. [Instalación y arranque](#1-instalación-y-arranque)
2. [Configurar el Realm `sipreb`](#2-configurar-el-realm-sipreb)
3. [Roles — plataforma y municipalidades](#3-roles)
4. [Configurar el Client `sipreb-backend`](#4-configurar-el-client)
5. [Configurar el Token JWT](#5-configurar-el-token-jwt)
6. [Configurar Flujos de Autenticación](#6-configurar-flujos-de-autenticación)
7. [Crear usuarios de prueba](#7-crear-usuarios-de-prueba)
8. [Estructura del proyecto](#8-estructura-del-proyecto)
9. [Integración Spring WebFlux Reactivo](#9-integración-spring-webflux)
10. [Onboarding de nuevos tenants](#10-onboarding-de-nuevos-tenants)
11. [Seguridad adicional](#11-seguridad-adicional)
12. [Variables de entorno](#12-variables-de-entorno)

---

## 1. Instalación y arranque

### Archivos necesarios

```
vg-ms-autenticationservice/
├── docker-compose.yml
├── .env                  ← variables reales (NO commitear)
├── .env.example          ← plantilla sin valores (SÍ commitear)
└── ...
```

### .env.example

```env
# ── Keycloak Admin ───────────────────────────────────────────
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=

# ── Neon PostgreSQL para Keycloak ────────────────────────────
# ✅ NO necesitas script SQL — Keycloak crea sus tablas al primer arranque
# Solo crea la database "keycloak" vacía en Neon y apunta aquí
KC_DB_HOST=ep-xxxx.us-east-2.aws.neon.tech
KC_DB_NAME=keycloak
KC_DB_USER=
KC_DB_PASSWORD=

# ── Neon PostgreSQL para Auth Service (R2DBC) ────────────────
DB_HOST=ep-xxxx.us-east-2.aws.neon.tech
DB_NAME=auth_service
DB_USER=
DB_PASSWORD=

# ── Keycloak (usado por el Auth Service) ─────────────────────
KEYCLOAK_URL=http://localhost:8080
KEYCLOAK_REALM=sipreb
KEYCLOAK_CLIENT_ID=sipreb-backend
KEYCLOAK_CLIENT_SECRET=
KEYCLOAK_ADMIN_SECRET=

# ── Redis ────────────────────────────────────────────────────
REDIS_HOST=localhost
REDIS_PORT=6379
```

### docker-compose.yml

```yaml
# Keycloak 26.5.4 + Redis — BD en Neon (sin contenedor local de postgres)
services:

  keycloak:
    image: quay.io/keycloak/keycloak:26.5.4
    container_name: sipreb-keycloak
    command: start-dev   # para producción: start --optimized (ver sección 11)
    environment:
      KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD}

      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://${KC_DB_HOST}/${KC_DB_NAME}?sslmode=require
      KC_DB_USERNAME: ${KC_DB_USER}
      KC_DB_PASSWORD: ${KC_DB_PASSWORD}
      KC_DB_SCHEMA: public

      # Neon cierra conexiones idle — pool pequeño evita errores
      KC_DB_POOL_INITIAL_SIZE: "1"
      KC_DB_POOL_MIN_SIZE: "1"
      KC_DB_POOL_MAX_SIZE: "5"

      KC_HOSTNAME: localhost
      KC_HOSTNAME_STRICT: "false"
      KC_HOSTNAME_STRICT_HTTPS: "false"
      KC_HTTP_ENABLED: "true"
      KC_HEALTH_ENABLED: "true"
      KC_METRICS_ENABLED: "true"
      KC_LOG_LEVEL: INFO

    ports:
      - "8080:8080"   # Admin Console y endpoints
      - "9000:9000"   # Health y métricas

    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9000/health/ready || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: sipreb-redis
    ports:
      - "6379:6379"
    command: redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3
    restart: unless-stopped
```

```bash
docker compose up -d

# Esperar mensaje: "Keycloak 26.5.4 on JVM started"
docker compose logs -f keycloak

# Verificar salud
curl http://localhost:9000/health/ready
```

> ⚠️ **Primera vez**: Keycloak crea sus ~90 tablas internas automáticamente. Solo crea la database `keycloak` vacía en Neon (Dashboard → New Database). Tarda ~60-90 segundos la primera vez.

> 💡 **DB_HOST en Neon**: solo el hostname. Ejemplo: `ep-cool-forest-123456.us-east-2.aws.neon.tech`

Admin console: `http://localhost:8080/admin`

---

## 2. Configurar el Realm `sipreb`

Con realm único, toda la plataforma vive en un solo realm. La separación entre municipalidades la garantizan el claim `municipal_code` + `TenantIsolationFilter`.

```
Realm: sipreb
├── Usuarios de plataforma  (sin municipal_code)
│   └── roles: SUPER_ADMIN, PLATFORM_SUPPORT, BILLING_MANAGER, ONBOARDING_MANAGER
└── Usuarios de municipalidades  (con municipal_code)
    └── roles: TENANT_ADMIN, PATRIMONIO_GESTOR, MOVIMIENTOS_APROBADOR, ...
```

### 2.1 Crear el realm

`Realm selector (top left) → Create Realm`

```
Realm name: sipreb
Display name: SIPREB — Control Patrimonial
Enabled: ON
```

### 2.2 Configurar tokens

`Realm Settings → Tokens`

| Parámetro | Valor |
|-----------|-------|
| Default Signature Algorithm | RS256 |
| Access Token Lifespan | 5 minutes |
| Client login timeout | 1 minute |
| Refresh Token Max Reuse | 0 |
| SSO Session Idle | 30 minutes |
| SSO Session Max | 8 hours |

### 2.3 Política de contraseñas

`Realm Settings → Authentication → Password Policy`

| Política | Valor |
|----------|-------|
| Minimum Length | 12 |
| Uppercase Characters | 1 |
| Lowercase Characters | 1 |
| Special Characters | 1 |
| Digits | 1 |
| Not Username | ✅ |
| Password History | 5 |

### 2.4 Brute Force Protection

`Realm Settings → Security Defenses → Brute Force Detection`

```
Enabled: ON
Permanent Lockout: OFF
Max Login Failures: 5
Wait Increment: 30 seconds
Max Wait: 15 minutes
Failure Reset Time: 12 hours
```

### 2.5 Deshabilitar registro público y campos no requeridos

`Realm Settings → Login`
```
User registration: OFF
Login with email: OFF    ← login solo por username
Forgot password: ON
Remember me: OFF
```

`Realm Settings → User profile`

| Campo | Acción |
|---|---|
| `email` | Required: OFF · Permissions: Admin only |
| `firstName` | Required: OFF · Permissions: Admin only |
| `lastName` | Required: OFF · Permissions: Admin only |

> Los datos personales viven en tu tabla `persons`. Keycloak solo necesita `username` + `password` + atributos personalizados.

---

## 3. Roles

Todos los roles viven en el realm `sipreb`. Los roles de plataforma y los de municipalidad coexisten — los usuarios de plataforma no tienen `municipal_code` y el `TenantIsolationFilter` los deja pasar.

`Realm sipreb → Realm Roles → Create role`

### 3.1 Roles de plataforma

Estos roles son para el equipo que opera la plataforma SIPREB, **no para usuarios de municipalidades**.

| Rol | Descripción | Nivel |
|-----|-------------|-------|
| `SUPER_ADMIN` | Acceso total al sistema y a todos los tenants. Solo equipo core de la plataforma. | 🔴 Crítico |
| `PLATFORM_SUPPORT` | Soporte técnico: lectura de logs globales y estado de tenants. Sin acceso a datos de negocio. | 🟡 Medio |
| `BILLING_MANAGER` | Gestión de suscripciones, planes y facturación de municipalidades. Sin acceso operativo. | 🟡 Medio |
| `ONBOARDING_MANAGER` | Creación y configuración de nuevos tenants. Sin acceso a datos operativos existentes. | 🟡 Medio |

Rol compuesto `SUPER_ADMIN`:

`Realm Roles → SUPER_ADMIN → Associated Roles`  
Agregar: `PLATFORM_SUPPORT`, `BILLING_MANAGER`, `ONBOARDING_MANAGER`

### 3.2 Roles de municipalidades (`is_system = true`)

Estos roles se replican automáticamente al crear un nuevo tenant. No pueden eliminarse.

**Administración:**

| Rol | Descripción |
|-----|-------------|
| `TENANT_ADMIN` | Administrador de la municipalidad. Gestiona usuarios, asigna roles, configura el tenant. Único que puede crear roles custom. |
| `TENANT_CONFIG_MANAGER` | Gestión de configuración operativa: áreas, categorías, cargos, tipos de documento, ubicaciones y proveedores. Sin acceso a usuarios. |

**Patrimonio:**

| Rol | Descripción |
|-----|-------------|
| `PATRIMONIO_GESTOR` | CRUD completo sobre bienes patrimoniales. Registra, actualiza, calcula depreciaciones y tramita bajas. |
| `PATRIMONIO_OPERARIO` | Registra nuevos bienes y actualiza estados. Sin bajas ni depreciaciones. Ideal para personal de campo. |
| `PATRIMONIO_VIEWER` | Solo lectura del catálogo completo de bienes patrimoniales. |

**Movimientos:**

| Rol | Descripción |
|-----|-------------|
| `MOVIMIENTOS_SOLICITANTE` | Crea solicitudes de movimiento, transferencia o asignación de bienes. |
| `MOVIMIENTOS_APROBADOR` | Aprueba o rechaza movimientos y genera actas de entrega-recepción. Asignado típicamente a jefes de área. |
| `MOVIMIENTOS_VIEWER` | Solo lectura del historial de movimientos y estado de solicitudes. |

> El campo `direct_manager_id` en `users` determina a quién escala un movimiento. `MOVIMIENTOS_APROBADOR` solo habilita la acción; el enrutamiento lo decide ese campo.

**Inventario:**

| Rol | Descripción |
|-----|-------------|
| `INVENTARIO_COORDINADOR` | Programa y coordina inventarios físicos, asigna verificadores, concilia diferencias y cierra el proceso. |
| `INVENTARIO_VERIFICADOR` | Solo puede verificar ítems durante un inventario activo. Diseñado para asignación temporal con `expiration_date`. |

**Mantenimiento:**

| Rol | Descripción |
|-----|-------------|
| `MANTENIMIENTO_GESTOR` | Programa, ejecuta y cierra órdenes de mantenimiento preventivo y correctivo. Gestiona costos, garantías y alertas. |
| `MANTENIMIENTO_VIEWER` | Solo lectura del historial de mantenimientos y calendario de vencimientos. |

**Reportes y Auditoría:**

| Rol | Descripción |
|-----|-------------|
| `REPORTES_VIEWER` | Acceso a dashboards, generación de reportes regulatorios y exportación de datos. |
| `REPORTES_SCHEDULER` | Configura reportes programados automáticos. Extensión de `REPORTES_VIEWER`. |
| `AUDITORIA_VIEWER` | Solo lectura de `auditoria_cambios` y `auditoria_accesos`. Fundamental para fiscalizaciones. |

### 3.3 Roles compuestos

`Realm Roles → {rol} → Associated Roles` → agregar los roles indicados

| Rol | Roles asociados |
|---|---|
| `SUPER_ADMIN` | `PLATFORM_SUPPORT`, `BILLING_MANAGER`, `ONBOARDING_MANAGER` |
| `TENANT_ADMIN` | Todos los roles de municipalidad |
| `PATRIMONIO_GESTOR` | `PATRIMONIO_VIEWER` |
| `MOVIMIENTOS_APROBADOR` | `MOVIMIENTOS_VIEWER`, `MOVIMIENTOS_SOLICITANTE` |
| `INVENTARIO_COORDINADOR` | `INVENTARIO_VERIFICADOR` |
| `MANTENIMIENTO_GESTOR` | `MANTENIMIENTO_VIEWER` |
| `REPORTES_SCHEDULER` | `REPORTES_VIEWER` |

### 3.4 Mapeo de permisos por rol

Permisos base en la tabla `permissions` con formato `module:action` o `module:action:resource`.

| Rol | Permisos base |
|-----|---------------|
| `PATRIMONIO_GESTOR` | `patrimonio:create`, `patrimonio:read`, `patrimonio:update`, `patrimonio:delete`, `patrimonio:depreciation`, `patrimonio:baja` |
| `PATRIMONIO_OPERARIO` | `patrimonio:create`, `patrimonio:read`, `patrimonio:update:status` |
| `PATRIMONIO_VIEWER` | `patrimonio:read` |
| `MOVIMIENTOS_SOLICITANTE` | `movimientos:create`, `movimientos:read:own` |
| `MOVIMIENTOS_APROBADOR` | `movimientos:read`, `movimientos:approve`, `movimientos:reject`, `movimientos:acta:generate` |
| `MOVIMIENTOS_VIEWER` | `movimientos:read` |
| `INVENTARIO_COORDINADOR` | `inventario:create`, `inventario:read`, `inventario:update`, `inventario:conciliate`, `inventario:close` |
| `INVENTARIO_VERIFICADOR` | `inventario:read:active`, `inventario:verify:item` |
| `MANTENIMIENTO_GESTOR` | `mantenimiento:create`, `mantenimiento:read`, `mantenimiento:update`, `mantenimiento:close`, `mantenimiento:alert:configure` |
| `MANTENIMIENTO_VIEWER` | `mantenimiento:read` |
| `REPORTES_VIEWER` | `reportes:read`, `reportes:generate`, `reportes:export` |
| `REPORTES_SCHEDULER` | `reportes:read`, `reportes:generate`, `reportes:export`, `reportes:schedule` |
| `AUDITORIA_VIEWER` | `auditoria:read` |
| `TENANT_CONFIG_MANAGER` | `config:read`, `config:update`, `config:areas:manage`, `config:categories:manage`, `config:locations:manage` |
| `TENANT_ADMIN` | Todos los permisos anteriores + `users:manage`, `roles:manage`, `roles:assign` |

### 3.5 Asignación de roles por cargo y área

Los roles se filtran por `position_allowed_roles`. Los marcados `is_default = true` se asignan automáticamente al crear el usuario.

| Cargo | Área | Roles permitidos | Default |
|---|---|---|---|
| Técnico Patrimonio | Patrimonio | `PATRIMONIO_GESTOR`, `PATRIMONIO_OPERARIO`, `PATRIMONIO_VIEWER`, `REPORTES_VIEWER` | `PATRIMONIO_OPERARIO` |
| Jefe de Almacén | Patrimonio | `PATRIMONIO_GESTOR`, `MOVIMIENTOS_APROBADOR`, `INVENTARIO_COORDINADOR`, `REPORTES_VIEWER` | `PATRIMONIO_GESTOR`, `MOVIMIENTOS_APROBADOR` |
| Técnico Inventario | Cualquier | `INVENTARIO_VERIFICADOR`, `INVENTARIO_COORDINADOR`, `PATRIMONIO_VIEWER` | `INVENTARIO_VERIFICADOR` |
| Técnico Mantenimiento | Cualquier | `MANTENIMIENTO_GESTOR`, `MANTENIMIENTO_VIEWER` | `MANTENIMIENTO_GESTOR` |
| Analista | Cualquier | `REPORTES_VIEWER`, `REPORTES_SCHEDULER`, `AUDITORIA_VIEWER` | `REPORTES_VIEWER` |
| Administrador | Cualquier | Todos los roles | `TENANT_ADMIN` |

---

## 4. Configurar el Client `sipreb-backend`

Un **único client** para todos los microservicios del backend.

### 4.1 Crear el client

`Clients → Create client`

**General Settings:**
```
Client type: OpenID Connect
Client ID: sipreb-backend
Name: SIPREB Backend
Description: Client único para todos los microservicios SIPREB
Always display in UI: OFF
```

**Capability Config:**
```
Client authentication: ON       ← genera un client secret
Authorization: OFF
Standard flow: OFF              ← solo ROPC, sin redirect
Direct access grants: ON        ← login username + password
Service accounts roles: ON      ← comunicación M2M
Implicit flow: OFF
OAuth 2.0 Device Authorization Grant: OFF
OIDC CIBA Grant: OFF
```

**Login Settings:**
```
Root URL: https://api.sipreb.pe
Valid redirect URIs: https://api.sipreb.pe/*
Valid post logout redirect URIs: https://api.sipreb.pe/*
Web origins: https://api.sipreb.pe
```

### 4.2 Obtener el Client Secret

`Clients → sipreb-backend → Credentials → Client secret` → copiar a `KEYCLOAK_CLIENT_SECRET` en `.env`

### 4.3 Eliminar scopes innecesarios

`Clients → sipreb-backend → Client Scopes → Assigned client scopes`

Quitar de **Default**:

| Scope | Claims que elimina |
|---|---|
| `email` | `email`, `email_verified` |
| `profile` | `given_name`, `family_name`, `name`, `preferred_username`, `updated_at` |
| `address` | `address` |
| `phone` | `phone_number` |

Mantener únicamente: `openid`, `roles`, `sipreb-claims`

### 4.4 Client para el Frontend (si aplica)

```
Client ID: sipreb-frontend
Client authentication: OFF
Standard flow: ON
Direct access grants: OFF
Valid redirect URIs: https://app.sipreb.pe/*
Web origins: https://app.sipreb.pe
```

---

## 5. Configurar el Token JWT

El token no lleva `email`, `given_name`, `family_name` ni datos personales — solo identificadores operativos.

### 5.1 Crear el Client Scope

`Client Scopes → Create client scope`

```
Name: sipreb-claims
Type: Default
Protocol: OpenID Connect
Include in token scope: ON
```

### 5.2 Agregar mappers

`Client Scopes → sipreb-claims → Mappers → Configure a new mapper`

**user_id**
```
Mapper type: User Attribute · Name: user_id · User Attribute: user_id
Token Claim Name: user_id · Claim JSON Type: String
Add to ID token: ON · Add to access token: ON · Add to userinfo: OFF
```

**municipal_code**
```
Mapper type: User Attribute · Name: municipal_code · User Attribute: municipal_code
Token Claim Name: municipal_code · Claim JSON Type: String
Add to ID token: ON · Add to access token: ON · Add to userinfo: OFF
```

**area_id**
```
Mapper type: User Attribute · Name: area_id · User Attribute: area_id
Token Claim Name: area_id · Claim JSON Type: String
Add to ID token: OFF · Add to access token: ON · Add to userinfo: OFF
```

**position_id**
```
Mapper type: User Attribute · Name: position_id · User Attribute: position_id
Token Claim Name: position_id · Claim JSON Type: String
Add to ID token: OFF · Add to access token: ON · Add to userinfo: OFF
```

**direct_manager_id**
```
Mapper type: User Attribute · Name: direct_manager_id · User Attribute: direct_manager_id
Token Claim Name: direct_manager_id · Claim JSON Type: String
Add to ID token: OFF · Add to access token: ON · Add to userinfo: OFF
```

**roles (claim de primer nivel)**
```
Mapper type: User Realm Role · Name: realm-roles-claim
Token Claim Name: roles
Add to ID token: OFF · Add to access token: ON · Add to userinfo: OFF
Multivalued: ON · Add to token introspection: ON
```

> Por defecto Keycloak pone los roles en `realm_access.roles`. Este mapper los pone en `roles` de primer nivel.

### 5.3 Asignar el scope al client

`Clients → sipreb-backend → Client Scopes → Add client scope`  
Buscar `sipreb-claims` → Add → **Default**

### 5.4 Token resultante esperado

```json
{
  "sub": "uuid-interno-keycloak",
  "iss": "http://localhost:8080/realms/sipreb",
  "aud": "sipreb-backend",
  "exp": 1700003600,
  "iat": 1700000000,
  "jti": "uuid-del-token",
  "user_id": "uuid-de-users-table",
  "municipal_code": "uuid-municipalidad-san-luis",
  "area_id": "uuid-area-patrimonio",
  "position_id": "uuid-cargo-tecnico",
  "direct_manager_id": "uuid-jefe-directo",
  "roles": ["PATRIMONIO_GESTOR", "PATRIMONIO_VIEWER"],
  "preferred_username": "jperez"
}
```

> Usuarios de plataforma (`SUPER_ADMIN`) no tienen `municipal_code`, `area_id` ni `position_id`.

Para verificar: `Clients → sipreb-backend → Client Scopes → Evaluate → Generated access token`

---

## 6. Configurar Flujos de Autenticación

### 6.1 Login ROPC

Usa **Resource Owner Password Credentials**. Ya habilitado con `Direct access grants: ON` del paso 4.1. No requiere configuración adicional en Keycloak.

### 6.2 OTP (recomendado para administradores)

`Authentication → Required Actions → Configure OTP → Default Action: OFF`

Activar manualmente para usuarios `SUPER_ADMIN` y `TENANT_ADMIN`:

`Users → {admin_user} → Required Actions → Configure OTP ✅`

---

## 7. Crear usuarios de prueba

### 7.1 Usuario de municipalidad

`Realm sipreb → Users → Create user`

**General:**
```
Username: jperez
Email / First Name / Last Name: (vacíos — configurado en sección 2.5)
Enabled: ON
```

**Attributes:**
```
user_id           →  uuid-de-users-table   ← id de tu tabla users
municipal_code    →  uuid-municipalidad-san-luis
area_id           →  uuid-area-patrimonio
position_id       →  uuid-cargo-tecnico-patrimonio
direct_manager_id →  uuid-jefe-area-patrimonio
```

> ⚠️ Crear primero en tu BD → obtener UUID → usarlo como `user_id` en Keycloak. Ver `POST /auth/users/sync` en sección 9.10.

**Credentials:** `Password: Temp1234! · Temporary: ON`

**Role Mapping:** `Assign role → PATRIMONIO_GESTOR`

### 7.2 Usuario de plataforma

`Realm sipreb → Users → Create user`

```
Username: sipreb_admin
Enabled: ON
Attributes: (ninguno — sin municipal_code)
```

`Role Mapping → Assign Role → SUPER_ADMIN`

---

## 8. Estructura del proyecto

Estructura real del microservicio `vg-ms-autenticationservice`.

```
vg-ms-autenticationservice/
├── docker-compose.yml / .env / .env.example / pom.xml
│
└── src/main/java/edu/pe/vallegrande/AuthenticationService/
    │
    ├── AuthenticationServiceApplication.java
    │
    ├── application/service/
    │   ├── AssignmentServiceImpl.java    ← asigna roles (BD + Keycloak sync)
    │   ├── AuthServiceImpl.java          ← login, refresh, logout
    │   ├── JwtServiceImpl.java           ← extrae claims del token
    │   ├── PermissionServiceImpl.java    ← resuelve permisos con cache Redis
    │   ├── PersonServiceImpl.java
    │   ├── RoleServiceImpl.java
    │   └── UserServiceImpl.java          ← crea usuario BD + Keycloak + sync
    │
    ├── domain/
    │   ├── model/
    │   │   ├── auth/        AuthTokens · LoginCommand · LoginResult · RefreshTokenCommand
    │   │   ├── user/        UserAccount · CreateUserCommand · BlockUserCommand · SuspendUserCommand
    │   │   ├── person/      Person · CreatePersonCommand
    │   │   ├── role/        RoleModel · UpsertRoleCommand
    │   │   ├── permission/  PermissionModel
    │   │   └── assignment/  AssignRoleCommand · UserRoleAssignment · RolePermissionAssignment
    │   ├── ports/
    │   │   ├── in/    AuthService · UserService · RoleService · PermissionService
    │   │   │          PersonService · AssignmentService · JwtService
    │   │   └── out/   AuthUserPort · UserPort · UserRolePort · RolePort · RolePermissionPort
    │   │              PermissionPort · PersonPort · AssignmentPermissionQueryPort
    │   │              KeycloakPort ← NUEVO · TokenBlacklistPort · CurrentUserPort
    │   │              CachePort ← NUEVO
    │   └── exception/
    │       ├── ResourceNotFoundException.java
    │       └── DuplicateResourceException.java
    │
    └── infrastructure/
        ├── adapter/
        │   ├── in/web/
        │   │   ├── controller/  AuthController · UserController · RoleController
        │   │   │                PermissionController · PersonController · AssignmentController
        │   │   ├── dto/         LoginRequestDto · LoginResponseDto · TokenResponseDto
        │   │   │                UserCreateRequestDto · BlockUserRequestDto · SuspendUserRequestDto
        │   │   │                RoleRequestDto · PermissionRequestDto · AssignRoleRequestDto · ...
        │   │   └── mapper/      AuthWebMapper · UserWebMapper · RoleWebMapper · PersonWebMapper
        │   │
        │   └── out/
        │       ├── persistence/  UserPersistenceAdapter · RolePersistenceAdapter
        │       │                 PermissionPersistenceAdapter · PersonPersistenceAdapter
        │       │                 AuthPersistenceAdapter · AssignmentPersistenceAdapter
        │       │   ├── entity/   User · Role · Permission · RolePermission · UserRole · Person
        │       │   ├── mapper/   UserAccountMapper · RoleMapper · PermissionMapper · ...
        │       │   └── repository/ UserRepository · RoleRepository · PermissionRepository
        │       │                   RolePermissionRepository · UserRoleRepository · PersonRepository
        │       ├── keycloak/
        │       │   └── KeycloakAdapter.java        ← NUEVO — implementa KeycloakPort
        │       └── security/
        │           ├── InMemoryTokenBlacklistAdapter.java
        │           ├── SecurityContextCurrentUserAdapter.java
        │           └── RedisCacheAdapter.java       ← NUEVO — implementa CachePort
        │
        └── config/
            ├── SecurityConfig.java     ← WebFlux Security + JWT
            ├── R2dbcConfig.java
            ├── JacksonConfig.java
            ├── CorsConfig.java
            └── SwaggerConfig.java
```

**Dirección de dependencias (regla hexagonal):**

```
controller (in)  →  ports/in  ←  application/service
                                        ↓
                                  ports/out  ←  adapter/out
                                               (persistence · keycloak · security)
```

El dominio nunca importa clases de Spring, R2DBC ni Keycloak.

---

## 9. Integración Spring WebFlux Reactivo

> ⚠️ `keycloak-spring-boot-adapter` es **bloqueante**. No usarlo. Integración correcta: `spring-boot-starter-oauth2-resource-server`.

### 9.1 Dependencias — pom.xml

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>r2dbc-postgresql</artifactId>
</dependency>
<dependency>
  <groupId>io.r2dbc</groupId>
  <artifactId>r2dbc-pool</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

### 9.2 application.yml

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${KEYCLOAK_URL}/realms/sipreb/protocol/openid-connect/certs
          issuer-uri: ${KEYCLOAK_URL}/realms/sipreb

  r2dbc:
    url: r2dbc:postgresql://${DB_HOST}/${DB_NAME}?sslMode=require
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    pool:
      initial-size: 1
      min-idle: 1
      max-size: 10
      max-idle-time: 10m
      validation-query: SELECT 1

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

keycloak:
  url: ${KEYCLOAK_URL}
  realm: sipreb
  client-id: ${KEYCLOAK_CLIENT_ID}
  client-secret: ${KEYCLOAK_CLIENT_SECRET}
  admin-realm: master
  admin-client-id: admin-cli
  admin-client-secret: ${KEYCLOAK_ADMIN_SECRET}
```

### 9.3 SecurityConfig.java

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthConverter;

    public SecurityConfig(JwtAuthenticationConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http, TenantIsolationFilter tenantFilter) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex
                .pathMatchers("/auth/login", "/auth/refresh").permitAll()
                .pathMatchers("/actuator/health").permitAll()
                .anyExchange().authenticated()
            )
            .addFilterAfter(tenantFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
            );
        return http.build();
    }
}
```

### 9.4 JwtAuthenticationConverter.java

```java
@Component
public class JwtAuthenticationConverter implements
        Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        Collection<GrantedAuthority> authorities = roles == null
            ? Collections.emptyList()
            : roles.stream()
                .map(SimpleGrantedAuthority::new)  // sin prefijo ROLE_
                .collect(Collectors.toList());
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }
}
```

### 9.5 TenantIsolationFilter.java

Usuarios con `SUPER_ADMIN` o `PLATFORM_SUPPORT` pasan sin validación de tenant.

```java
@Component
public class TenantIsolationFilter implements WebFilter {

    private static final String TENANT_HEADER = "X-Municipal-Code";
    private static final Set<String> PLATFORM_ROLES =
        Set.of("SUPER_ADMIN", "PLATFORM_SUPPORT");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestTenant = exchange.getRequest()
            .getHeaders().getFirst(TENANT_HEADER);

        if (requestTenant == null) return chain.filter(exchange);

        return exchange.getPrincipal()
            .cast(JwtAuthenticationToken.class)
            .flatMap(auth -> {
                List<String> roles = auth.getToken().getClaimAsStringList("roles");

                if (roles != null && roles.stream().anyMatch(PLATFORM_ROLES::contains))
                    return chain.filter(exchange);

                String tokenTenant = auth.getToken().getClaimAsString("municipal_code");
                if (tokenTenant == null || !requestTenant.equals(tokenTenant)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            })
            .switchIfEmpty(chain.filter(exchange));
    }
}
```

### 9.6 Login con username + password (ROPC)

```
POST /auth/login  { username, password, municipal_code }
         ↓
[AuthServiceImpl]
1. findByUsernameAndMunicipalCode → status = ACTIVE · blocked_until < NOW()
2. keycloakPort.login(username, password, "sipreb")
         ↓ 200 OK
3. resetLoginAttempts · updateLastLogin
4. return AuthTokens
         ↓ 401 Keycloak
3. login_attempts += 1 · si >= 5 → blocked_until = NOW() + 15min
4. return 401
```

```java
// application/service/AuthServiceImpl.java
@Override
public Mono<LoginResult> login(LoginCommand command) {
    return authUserPort.findByUsernameAndMunicipalCode(
                command.username(), command.municipalCode())
        .switchIfEmpty(Mono.error(
            new ResourceNotFoundException("Usuario no encontrado")))
        .flatMap(this::validateUserStatus)
        .flatMap(user ->
            keycloakPort.login(command.username(), command.password(), "sipreb")
                .flatMap(tokens ->
                    userPort.resetLoginAttempts(user.getId())
                        .then(userPort.updateLastLogin(user.getId()))
                        .thenReturn(LoginResult.success(tokens))
                )
                .onErrorResume(KeycloakAuthException.class, e ->
                    handleFailedAttempt(user).then(Mono.error(e)))
        );
}
```

> ⚠️ Doble capa de bloqueo: `blocked_until` en tu BD + Brute Force en Keycloak (sección 2.4).

### 9.7 KeycloakPort.java

```java
// domain/ports/out/KeycloakPort.java  ← NUEVO
public interface KeycloakPort {
    Mono<AuthTokens> login(String username, String password, String realm);
    Mono<AuthTokens> refreshToken(String refreshToken, String realm);
    Mono<Void> logout(String keycloakUserId, String realm);
    Mono<String> createUser(CreateKeycloakUserCommand command, String realm);
    Mono<Void> assignRole(String keycloakUserId, String roleName, String realm);
    Mono<Void> syncRoles(String keycloakUserId, List<String> roleNames, String realm);
}
```

### 9.8 KeycloakAdapter.java

```java
// infrastructure/adapter/out/keycloak/KeycloakAdapter.java  ← NUEVO
@Component
public class KeycloakAdapter implements KeycloakPort {

    private final WebClient webClient;
    private final KeycloakProperties props;

    public KeycloakAdapter(WebClient.Builder builder, KeycloakProperties props) {
        this.webClient = builder.baseUrl(props.getUrl()).build();
        this.props = props;
    }

    @Override
    public Mono<AuthTokens> login(String username, String password, String realm) {
        return webClient.post()
            .uri("/realms/{realm}/protocol/openid-connect/token", realm)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(new LinkedMultiValueMap<>() {{
                add("grant_type",    "password");
                add("client_id",     props.getClientId());
                add("client_secret", props.getClientSecret());
                add("username",      username);
                add("password",      password);
            }})
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError,
                r -> Mono.error(new KeycloakAuthException("Credenciales inválidas")))
            .bodyToMono(AuthTokens.class);
    }

    @Override
    public Mono<AuthTokens> refreshToken(String refreshToken, String realm) {
        return webClient.post()
            .uri("/realms/{realm}/protocol/openid-connect/token", realm)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(new LinkedMultiValueMap<>() {{
                add("grant_type",    "refresh_token");
                add("client_id",     props.getClientId());
                add("client_secret", props.getClientSecret());
                add("refresh_token", refreshToken);
            }})
            .retrieve()
            .bodyToMono(AuthTokens.class);
    }

    @Override
    public Mono<Void> logout(String keycloakUserId, String realm) {
        return getAdminToken()
            .flatMap(token -> webClient.post()
                .uri("/admin/realms/{realm}/users/{id}/logout", realm, keycloakUserId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Void.class));
    }

    @Override
    public Mono<String> createUser(CreateKeycloakUserCommand cmd, String realm) {
        return getAdminToken()
            .flatMap(token -> webClient.post()
                .uri("/admin/realms/{realm}/users", realm)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                    "username", cmd.username(),
                    "enabled",  true,
                    "attributes", Map.of(
                        "user_id",           List.of(cmd.userId()),
                        "municipal_code",     List.of(cmd.municipalCode()),
                        "area_id",           List.of(cmd.areaId()),
                        "position_id",       List.of(cmd.positionId()),
                        "direct_manager_id", List.of(cmd.directManagerId())
                    )
                ))
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    String location = response.getHeaders().getFirst("Location");
                    return location.substring(location.lastIndexOf("/") + 1);
                })
            );
    }

    private Mono<String> getAdminToken() {
        return webClient.post()
            .uri("/realms/master/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(new LinkedMultiValueMap<>() {{
                add("grant_type",    "client_credentials");
                add("client_id",     "admin-cli");
                add("client_secret", props.getAdminClientSecret());
            }})
            .retrieve()
            .bodyToMono(AuthTokens.class)
            .map(AuthTokens::accessToken);
    }
}
```

### 9.9 Sincronización de roles — BD como fuente de verdad

```
BD (roles / users_roles)  →  fuente de verdad · auditoría · lógica de negocio
Keycloak (realm roles)    →  reflejo para el JWT · control de acceso en endpoints
```

Nunca asignar roles directamente en la UI de Keycloak. Siempre desde `AssignmentServiceImpl`.

```java
// application/service/AssignmentServiceImpl.java
@Override
public Mono<Void> assignRoleToUser(AssignRoleCommand command) {
    return userRolePort.assign(command)
        .then(authUserPort.findById(command.userId()))
        .flatMap(user -> keycloakPort.assignRole(
            user.getKeycloakId(), command.roleName(), "sipreb"
        ));
}
```

> Agregar `ALTER TABLE users ADD COLUMN keycloak_id VARCHAR(36);` para guardar el `sub` de Keycloak.

### 9.10 Endpoint de sincronización — `POST /auth/users/sync`

Sincroniza usuarios existentes en BD hacia Keycloak. Requiere rol `SUPER_ADMIN`.

```java
// UserController.java
@PostMapping("/sync")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public Mono<ResponseEntity<SyncResultDto>> syncUsers(
        @RequestBody SyncUsersRequestDto dto) {
    if (dto.userId() != null)
        return userService.syncSingleUserToKeycloak(dto.userId())
            .map(r -> ResponseEntity.ok(UserWebMapper.toSyncDto(r)));
    return userService.syncUsersToKeycloak(dto.municipalCode())
        .map(r -> ResponseEntity.ok(UserWebMapper.toSyncDto(r)));
}
```

```bash
# Sync masivo por municipalidad
POST /auth/users/sync
Authorization: Bearer <token-SUPER_ADMIN>
{ "municipalCode": "uuid-municipalidad-san-luis" }

# Sync individual
POST /auth/users/sync
Authorization: Bearer <token-SUPER_ADMIN>
{ "userId": "uuid-del-usuario" }

# Respuesta
{ "total": 47, "synced": 45, "failed": ["usuario_x"] }
```

### 9.11 AuthController y RedisCacheAdapter

```java
// AuthController.java — extraer user_id y keycloakId para logout
@PostMapping("/logout")
@PreAuthorize("isAuthenticated()")
public Mono<ResponseEntity<Void>> logout(@AuthenticationPrincipal Jwt jwt) {
    String userId        = jwt.getClaimAsString("user_id");
    String keycloakId    = jwt.getSubject();
    String municipalCode = jwt.getClaimAsString("municipal_code");
    return authService.logout(userId, keycloakId, municipalCode)
        .thenReturn(ResponseEntity.noContent().<Void>build());
}
```

```java
// RedisCacheAdapter.java — implementa CachePort, TTL 60s
@Component
public class RedisCacheAdapter implements CachePort {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofSeconds(60);

    @Override
    public Mono<List<String>> getPermissions(String userId, String municipalCode) {
        return redisTemplate.opsForValue()
            .get("perms:" + userId + ":" + municipalCode)
            .map(this::parseJson)
            .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<Void> setPermissions(String userId, String municipalCode,
                                      List<String> permissions) {
        return redisTemplate.opsForValue()
            .set("perms:" + userId + ":" + municipalCode, toJson(permissions), TTL)
            .then();
    }

    @Override
    public Mono<Void> invalidate(String userId, String municipalCode) {
        return redisTemplate.delete("perms:" + userId + ":" + municipalCode).then();
    }
}
```

---

## 10. Onboarding de nuevos tenants

Con realm único, el onboarding no crea realms ni clients — solo registra usuarios con el `municipal_code` del nuevo tenant.

```
Tenant Management Service
    ↓ crea registro en master_tenant · genera municipal_code
    ↓ llama al Auth Service
            ↓
    UserServiceImpl.createTenantAdmin()
            ↓ userPort.save() → BD
            ↓ keycloakPort.createUser() → realm sipreb
            ↓ keycloakPort.assignRole("TENANT_ADMIN", "sipreb")
    ✅ Municipalidad lista
```

```java
// application/service/UserServiceImpl.java
public Mono<Void> createTenantAdmin(CreateUserCommand command) {
    return userPort.save(UserAccount.from(command))
        .flatMap(savedUser ->
            keycloakPort.createUser(
                CreateKeycloakUserCommand.builder()
                    .username(savedUser.getUsername())
                    .userId(savedUser.getId().toString())
                    .municipalCode(savedUser.getMunicipalCode())
                    .areaId(savedUser.getAreaId())
                    .positionId(savedUser.getPositionId())
                    .directManagerId(savedUser.getDirectManagerId())
                    .build(),
                "sipreb"
            )
            .flatMap(keycloakId ->
                userPort.updateKeycloakId(savedUser.getId(), keycloakId)
                    .then(keycloakPort.assignRole(keycloakId, "TENANT_ADMIN", "sipreb"))
            )
        );
}
```

---

## 11. Seguridad adicional

### 11.1 HTTPS en producción

```bash
docker run quay.io/keycloak/keycloak:26.5.4 start \
  --hostname=auth.sipreb.pe \
  --https-certificate-file=/etc/ssl/certs/cert.pem \
  --https-certificate-key-file=/etc/ssl/certs/key.pem
```

### 11.2 Configurar CORS

`Realm Settings → Security Defenses → Headers`
```
X-Frame-Options: SAMEORIGIN
Content-Security-Policy: frame-src 'self'; frame-ancestors 'self'; object-src 'none';
```

`Clients → sipreb-backend → Web Origins`
```
https://app.sipreb.pe
https://api.sipreb.pe
```

### 11.3 Deshabilitar endpoints innecesarios

`Realm Settings → General` → `User-managed access: OFF`  
`Realm Settings → Login` → `User registration: OFF`

### 11.4 Rotación de secrets

`Clients → sipreb-backend → Credentials → Regenerate`  
Actualizar `KEYCLOAK_CLIENT_SECRET` en el Auth Service y reiniciar.

### 11.5 Configurar eventos y auditoría

`Realm Settings → Events → Config`
```
Save Events: ON
Saved Types: LOGIN, LOGIN_ERROR, LOGOUT, TOKEN_EXCHANGE
Events Expiration: 30 days
```

`Realm Settings → Events → Admin Events`
```
Save Admin Events: ON
Include Representation: ON
```

---

## 12. Variables de entorno

```env
# ── Keycloak ────────────────────────────────────────────────
KEYCLOAK_URL=http://localhost:8080
KEYCLOAK_REALM=sipreb
KEYCLOAK_CLIENT_ID=sipreb-backend
KEYCLOAK_CLIENT_SECRET=<secret-generado-en-paso-4.2>
KEYCLOAK_ADMIN_SECRET=<secret-del-admin-cli>

# ── Neon PostgreSQL (Auth Service) ──────────────────────────
DB_HOST=ep-cool-forest-123456.us-east-2.aws.neon.tech
DB_NAME=auth_service
DB_USER=<neon-user>
DB_PASSWORD=<neon-password>

# ── Neon PostgreSQL (Keycloak) — van en docker-compose.yml ──
# KC_DB_URL=jdbc:postgresql://<host>.neon.tech/keycloak?sslmode=require

# ── Redis ────────────────────────────────────────────────────
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PERMISSIONS_TTL=60
```

### Separación de databases en Neon

```
Proyecto Neon: sipreb-prod
├── keycloak        → Keycloak (auto-creada al primer arranque, sin script)
├── auth_service    → Auth Service (R2DBC)
├── master_tenant   → Tenant Management Service
├── muni_san_luis   → tenant específico
└── muni_los_olivos → tenant específico
```

### TenantRealmResolver

```java
@Component
public class TenantRealmResolver {
    private static final String REALM = "sipreb";
    public String resolve(String municipalCode) { return REALM; }
}
```

---

## 📎 Referencias útiles

| Recurso | URL |
|---------|-----|
| Documentación oficial Keycloak | https://www.keycloak.org/documentation |
| Admin REST API | `{KEYCLOAK_URL}/admin/realms` |
| OpenID Configuration | `{KEYCLOAK_URL}/realms/sipreb/.well-known/openid-configuration` |
| JWKS (verificar tokens) | `{KEYCLOAK_URL}/realms/sipreb/protocol/openid-connect/certs` |
| Endpoint de token | `{KEYCLOAK_URL}/realms/sipreb/protocol/openid-connect/token` |

---

*Versión 4.1 — SIPREB 2025 — Keycloak 26.5.4 · Realm único `sipreb` · Spring Boot WebFlux · Neon PostgreSQL*
