# 🔑 Guía de Configuración Keycloak — SIPREB
## Sistema Multi-Tenant de Control Patrimonial para Municipalidades

> **Versión Keycloak:** 26.5.4  
> **Patrón:** Un realm por municipalidad (tenant)  
> **Stack backend:** Java 17+ · Spring Boot 3.x · WebFlux reactivo · Arquitectura Hexagonal  
> **BD Keycloak:** Neon PostgreSQL (nube) · Sin contenedor de BD local  
> **Última actualización:** 2025

---

## 📋 Índice

1. [Instalación y arranque](#1-instalación-y-arranque)
2. [Configuración del Realm Master](#2-configuración-del-realm-master)
3. [Roles de plataforma (Super-Admin, Billing, Support)](#3-roles-de-plataforma)
4. [Crear un Realm por Tenant](#4-crear-un-realm-por-tenant)
5. [Configurar el Client para el Auth Service](#5-configurar-el-client)
6. [Configurar Roles dentro del Realm](#6-configurar-roles-dentro-del-realm)
7. [Configurar el Token JWT (claims personalizados)](#7-configurar-el-token-jwt)
8. [Configurar Flujos de Autenticación](#8-configurar-flujos-de-autenticación)
9. [Crear usuarios de prueba](#9-crear-usuarios-de-prueba)
10. [Estructura del proyecto Spring Boot (Arquitectura Hexagonal)](#10-estructura-del-proyecto)
11. [Integración Keycloak — Spring WebFlux Reactivo](#11-integración-keycloak-spring-webflux)
12. [Automatizar onboarding de nuevos tenants](#12-automatizar-onboarding)
13. [Seguridad adicional recomendada](#13-seguridad-adicional)
14. [Variables de entorno de referencia](#14-variables-de-entorno)

---

## 1. Instalación y arranque

### Estructura de archivos Docker

```
vg-ms-autenticationservice/
├── docker-compose.yml
├── .env                  ← variables de entorno (NO commitear)
├── .env.example          ← plantilla sin valores reales (SÍ commitear)
└── ...
```

### .env.example

```env
# ── Keycloak Admin ───────────────────────────────────────────
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=

# ── Neon PostgreSQL para Keycloak ────────────────────────────
# Obtener desde: Neon Dashboard → Connection Details → JDBC
# ✅ NO necesitas un script — Keycloak crea sus propias tablas al primer arranque
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
KEYCLOAK_REALM=municipalidad-san-luis
KEYCLOAK_CLIENT_ID=sipreb-backend
KEYCLOAK_CLIENT_SECRET=
KEYCLOAK_ADMIN_SECRET=

# ── Redis ────────────────────────────────────────────────────
REDIS_HOST=localhost
REDIS_PORT=6379
```

### docker-compose.yml

```yaml
# docker-compose.yml
# Keycloak 26.5.4 con Neon PostgreSQL (sin BD local)
services:

  keycloak:
    image: quay.io/keycloak/keycloak:26.5.4
    container_name: sipreb-keycloak
    # start-dev: deshabilita HTTPS, ideal para desarrollo local
    # Para producción usar: start --optimized (ver sección 13)
    command: start-dev
    environment:
      # ── Admin ──────────────────────────────────────────────
      KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD}

      # ── Base de datos — Neon PostgreSQL ────────────────────
      KC_DB: postgres
      # Neon requiere SSL obligatorio
      KC_DB_URL: jdbc:postgresql://${KC_DB_HOST}/${KC_DB_NAME}?sslmode=require
      KC_DB_USERNAME: ${KC_DB_USER}
      KC_DB_PASSWORD: ${KC_DB_PASSWORD}
      KC_DB_SCHEMA: public

      # ── Pool de conexiones ─────────────────────────────────
      # Neon cierra conexiones idle — pool pequeño evita errores
      KC_DB_POOL_INITIAL_SIZE: "1"
      KC_DB_POOL_MIN_SIZE: "1"
      KC_DB_POOL_MAX_SIZE: "5"

      # ── Hostname ───────────────────────────────────────────
      KC_HOSTNAME: localhost
      KC_HOSTNAME_STRICT: "false"           # permite acceso por IP en dev
      KC_HOSTNAME_STRICT_HTTPS: "false"     # desactiva forzar HTTPS en dev
      KC_HTTP_ENABLED: "true"

      # ── Health checks ──────────────────────────────────────
      KC_HEALTH_ENABLED: "true"
      KC_METRICS_ENABLED: "true"

      # ── Logging ────────────────────────────────────────────
      KC_LOG_LEVEL: INFO

    ports:
      - "8080:8080"     # HTTP — Admin Console y endpoints
      - "9000:9000"     # Management (health, metrics)

    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9000/health/ready || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s   # Keycloak tarda en arrancar con Neon

    restart: unless-stopped

    # ── Sin depends_on — la BD es Neon (externa) ───────────────

  # ── Redis para cache de permisos ─────────────────────────────
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
# Levantar servicios
docker compose up -d

# Ver logs de Keycloak (esperar mensaje "Keycloak 26.5.4 on JVM started")
docker compose logs -f keycloak

# Verificar que está listo
curl http://localhost:9000/health/ready
```

> ⚠️ **Primera vez con Neon**: Keycloak crea sus ~90 tablas internas automáticamente en la database `keycloak`. **No necesitas ningún script SQL**. Solo crea la database vacía en Neon (Dashboard → New Database → nombre: `keycloak`) y Keycloak hace el resto. Puede tardar hasta 60-90 segundos en el primer arranque.

> 💡 **Cómo obtener la connection string en Neon:**  
> Dashboard Neon → Tu proyecto → Connection Details → dropdown `JDBC` → copiar host.  
> Solo necesitas el **hostname** (ej: `ep-cool-forest-123456.us-east-2.aws.neon.tech`).

Una vez levantado, accede a: `http://localhost:8080`  
Admin console: `http://localhost:8080/admin`

---

## 2. Configuración del Realm Master

El realm `master` es el realm raíz de Keycloak. Aquí solo viven los usuarios de la plataforma (Super Admin, Billing, Support). **Nunca crear usuarios de municipalidades aquí.**

### 2.1 Cambiar configuración de sesión del realm master

`Realm Settings → Sessions`

| Parámetro | Valor recomendado |
|-----------|------------------|
| SSO Session Idle | 30 minutes |
| SSO Session Max | 8 hours |
| Access Token Lifespan | 5 minutes |
| Refresh Token Lifespan | 30 minutes |

### 2.2 Configurar política de contraseñas

`Realm Settings → Authentication → Password Policy`

Agregar las siguientes políticas:

| Política | Valor |
|----------|-------|
| Minimum Length | 12 |
| Uppercase Characters | 1 |
| Lowercase Characters | 1 |
| Special Characters | 1 |
| Digits | 1 |
| Not Username | ✅ |
| Password History | 5 |

### 2.3 Habilitar Brute Force Protection

`Realm Settings → Security Defenses → Brute Force Detection`

```
Enabled: ON
Permanent Lockout: OFF
Max Login Failures: 5
Wait Increment: 30 seconds
Max Wait: 15 minutes
Failure Reset Time: 12 hours
```

---

## 3. Roles de plataforma

Estos roles viven en el realm `master` y son para el equipo que opera la plataforma SIPREB.

### 3.1 Crear roles en el realm master

`Realm master → Realm Roles → Create role`

Crear los siguientes roles:

| Rol | Descripción |
|-----|-------------|
| `SUPER_ADMIN` | Acceso total al sistema y a todos los tenants |
| `PLATFORM_SUPPORT` | Soporte técnico, lectura de logs globales |
| `BILLING_MANAGER` | Gestión de suscripciones y facturación |
| `ONBOARDING_MANAGER` | Creación y configuración de nuevos tenants |

### 3.2 Crear rol compuesto SUPER_ADMIN

`Realm Roles → SUPER_ADMIN → Associated Roles`

Agregar como roles asociados: `PLATFORM_SUPPORT`, `BILLING_MANAGER`, `ONBOARDING_MANAGER`

Así un `SUPER_ADMIN` automáticamente tiene todos los permisos de los roles inferiores.

### 3.3 Crear usuario administrador de plataforma

`Users → Create user`

```
Username: sipreb_admin
Email: admin@sipreb.pe
First Name: Admin
Last Name: SIPREB
Email Verified: ON
```

`Users → sipreb_admin → Role Mapping → Assign Role → SUPER_ADMIN`

---

## 4. Crear un Realm por Tenant

Cada municipalidad tiene su propio realm. Esto garantiza aislamiento total de sesiones, tokens y usuarios.

### 4.1 Crear el realm

`Realm selector (top left) → Create Realm`

```
Realm name: municipalidad-san-luis
Enabled: ON
```

> Convención de nombres: `municipalidad-{nombre-slug}` en minúsculas sin espacios.  
> Ejemplos: `municipalidad-san-luis`, `municipalidad-los-olivos`, `municipalidad-surco`

### 4.2 Configuración general del realm

`Realm Settings → General`

```
Display name: Municipalidad de San Luis
HTML Display name: <b>Municipalidad de San Luis</b>
Frontend URL: https://api.sipreb.pe
```

### 4.3 Configurar tokens del realm

`Realm Settings → Tokens`

| Parámetro | Valor |
|-----------|-------|
| Default Signature Algorithm | RS256 |
| Access Token Lifespan | 5 minutes |
| Access Token Lifespan For Implicit Flow | 15 minutes |
| Client login timeout | 1 minute |
| Refresh Token Max Reuse | 0 |
| SSO Session Idle | 30 minutes |
| SSO Session Max | 8 hours |

> El Access Token corto (5 min) es intencional. El Refresh Token maneja la renovación transparente.

### 4.4 Replicar política de contraseñas

Repetir el paso [2.2](#22-configurar-política-de-contraseñas) y [2.3](#23-habilitar-brute-force-protection) en cada realm de tenant.

---

## 5. Configurar el Client

Un **único client `sipreb-backend`** sirve a todos los microservicios del backend. No es necesario un client por microservicio para tu escala actual — solo tendría sentido separar si necesitas permisos M2M diferenciados por servicio en el futuro.

### 5.1 Crear el client

`Clients → Create client`

**Paso 1 — General Settings:**
```
Client type: OpenID Connect
Client ID: sipreb-backend
Name: SIPREB Backend
Description: Client único para todos los microservicios SIPREB
Always display in UI: OFF
```

**Paso 2 — Capability Config:**
```
Client authentication: ON       ← genera un client secret
Authorization: OFF
Standard flow: OFF              ← no usamos redirect, solo ROPC
Direct access grants: ON        ← login username + password (ROPC)
Service accounts roles: ON      ← para comunicación M2M entre microservicios
Implicit flow: OFF
OAuth 2.0 Device Authorization Grant: OFF
OIDC CIBA Grant: OFF
```

**Paso 3 — Login Settings:**
```
Root URL: https://api.sipreb.pe
Home URL: https://api.sipreb.pe
Valid redirect URIs: https://api.sipreb.pe/*
Valid post logout redirect URIs: https://api.sipreb.pe/*
Web origins: https://api.sipreb.pe
```

### 5.2 Obtener el Client Secret

`Clients → sipreb-backend → Credentials`

Copiar el valor de `Client secret` → pegar en `KEYCLOAK_CLIENT_SECRET` del `.env`.

### 5.3 Eliminar scopes innecesarios (email, profile, address)

Keycloak incluye por defecto los scopes `email`, `profile`, `address` y `phone` que agregan `email`, `given_name`, `family_name` y otros claims al token. Como no los necesitamos, los eliminamos.

`Clients → sipreb-backend → Client Scopes`

En la pestaña **"Assigned client scopes"** verás los scopes asignados. Quitar los siguientes de **Default**:

| Scope a quitar | Qué elimina del token |
|---|---|
| `email` | `email`, `email_verified` |
| `profile` | `given_name`, `family_name`, `name`, `preferred_username`, `updated_at` |
| `address` | claim `address` |
| `phone` | `phone_number` |

**Pasos:**
1. `Clients → sipreb-backend → Client Scopes → Assigned client scopes`
2. Para cada scope: seleccionar → `Remove` (moverlo de Default a Optional o eliminarlo)
3. Mantener únicamente: `roles`, `sipreb-claims`, `openid`

> ✅ Con esto el token solo tendrá los claims que definiste en `sipreb-claims` más los estándar de OpenID (`sub`, `iss`, `aud`, `exp`, `iat`, `jti`).

### 5.4 Deshabilitar campos no requeridos en el realm

Para que Keycloak no exija email, first name ni last name al crear usuarios:

`Realm Settings → User profile (Keycloak 26.x)`

| Campo | Acción |
|---|---|
| `email` | Required: OFF · Permissions: Admin only |
| `firstName` | Required: OFF · Permissions: Admin only |
| `lastName` | Required: OFF · Permissions: Admin only |

> Así puedes crear usuarios en Keycloak solo con `username` + `password` + atributos personalizados. El email y nombre completo viven en tu tabla `persons`, no en Keycloak.

### 5.5 Crear client para el Frontend (si aplica)

```
Client ID: sipreb-frontend
Client authentication: OFF      ← público, sin secret
Standard flow: ON
Direct access grants: OFF       ← el frontend no hace ROPC directamente
Valid redirect URIs: https://app.sipreb.pe/*
Web origins: https://app.sipreb.pe
```

---

## 6. Configurar Roles dentro del Realm

### 6.1 Crear roles del tenant

`Realm municipalidad-san-luis → Realm Roles → Create role`

Crear todos los roles del sistema:

**Administración:**
| Rol | Descripción |
|-----|-------------|
| `TENANT_ADMIN` | Administrador de la municipalidad |
| `TENANT_CONFIG_MANAGER` | Gestión de configuración del tenant |

**Patrimonio:**
| Rol | Descripción |
|-----|-------------|
| `PATRIMONIO_GESTOR` | CRUD completo de bienes patrimoniales |
| `PATRIMONIO_OPERARIO` | Registro y actualización de bienes |
| `PATRIMONIO_VIEWER` | Solo lectura de bienes |

**Movimientos:**
| Rol | Descripción |
|-----|-------------|
| `MOVIMIENTOS_SOLICITANTE` | Crear solicitudes de movimiento |
| `MOVIMIENTOS_APROBADOR` | Aprobar/rechazar movimientos y generar actas |
| `MOVIMIENTOS_VIEWER` | Solo lectura de movimientos |

**Inventario:**
| Rol | Descripción |
|-----|-------------|
| `INVENTARIO_COORDINADOR` | Coordinar y conciliar inventarios físicos |
| `INVENTARIO_VERIFICADOR` | Verificar ítems en inventarios activos |

**Mantenimiento:**
| Rol | Descripción |
|-----|-------------|
| `MANTENIMIENTO_GESTOR` | Gestión completa de mantenimientos |
| `MANTENIMIENTO_VIEWER` | Solo lectura de mantenimientos |

**Reportes y Auditoría:**
| Rol | Descripción |
|-----|-------------|
| `REPORTES_VIEWER` | Acceso a dashboards y reportes |
| `REPORTES_SCHEDULER` | Configurar reportes programados |
| `AUDITORIA_VIEWER` | Solo lectura de logs de auditoría |

### 6.2 Crear roles compuestos

Los roles compuestos agrupan otros roles para facilitar la asignación.

`Realm Roles → TENANT_ADMIN → Associated Roles`

Agregar todos los roles del realm como asociados al `TENANT_ADMIN`.

`Realm Roles → PATRIMONIO_GESTOR → Associated Roles`

Agregar: `PATRIMONIO_VIEWER` (un gestor siempre puede ver)

`Realm Roles → MOVIMIENTOS_APROBADOR → Associated Roles`

Agregar: `MOVIMIENTOS_VIEWER`, `MOVIMIENTOS_SOLICITANTE`

`Realm Roles → INVENTARIO_COORDINADOR → Associated Roles`

Agregar: `INVENTARIO_VERIFICADOR`

`Realm Roles → MANTENIMIENTO_GESTOR → Associated Roles`

Agregar: `MANTENIMIENTO_VIEWER`

`Realm Roles → REPORTES_SCHEDULER → Associated Roles`

Agregar: `REPORTES_VIEWER`

---

## 7. Configurar el Token JWT

Aquí se configuran los claims personalizados. El token **no llevará** `email`, `given_name`, `family_name` ni ningún dato personal — solo los identificadores necesarios para operar.

### 7.1 Crear un Client Scope personalizado

`Client Scopes → Create client scope`

```
Name: sipreb-claims
Description: Claims personalizados para SIPREB
Type: Default
Protocol: OpenID Connect
Include in token scope: ON
```

### 7.2 Agregar mappers al scope

`Client Scopes → sipreb-claims → Mappers → Configure a new mapper`

#### Mapper 1 — user_id (tu BD)

```
Mapper type: User Attribute
Name: user_id
User Attribute: user_id
Token Claim Name: user_id
Claim JSON Type: String
Add to ID token: ON
Add to access token: ON
Add to userinfo: OFF
```

#### Mapper 2 — municipal_code

```
Mapper type: User Attribute
Name: municipal_code
User Attribute: municipal_code
Token Claim Name: municipal_code
Claim JSON Type: String
Add to ID token: ON
Add to access token: ON
Add to userinfo: OFF
```

#### Mapper 3 — area_id

```
Mapper type: User Attribute
Name: area_id
User Attribute: area_id
Token Claim Name: area_id
Claim JSON Type: String
Add to ID token: OFF
Add to access token: ON
Add to userinfo: OFF
```

#### Mapper 4 — position_id

```
Mapper type: User Attribute
Name: position_id
User Attribute: position_id
Token Claim Name: position_id
Claim JSON Type: String
Add to ID token: OFF
Add to access token: ON
Add to userinfo: OFF
```

#### Mapper 5 — direct_manager_id

```
Mapper type: User Attribute
Name: direct_manager_id
User Attribute: direct_manager_id
Token Claim Name: direct_manager_id
Claim JSON Type: String
Add to ID token: OFF
Add to access token: ON
Add to userinfo: OFF
```

#### Mapper 6 — Roles en el token

```
Mapper type: User Realm Role
Name: realm-roles-claim
Token Claim Name: roles
Add to ID token: OFF
Add to access token: ON
Add to userinfo: OFF
Multivalued: ON
Add to token introspection: ON
```

> Por defecto Keycloak pone los roles en `realm_access.roles`. Este mapper los pone en un claim `roles` de primer nivel, más fácil de consumir en los microservicios.

### 7.3 Asignar el scope al client

`Clients → sipreb-backend → Client Scopes → Add client scope`

Buscar `sipreb-claims` → Add → **Default**

### 7.4 Verificar el token resultante

El JWT de acceso debe verse exactamente así — sin email, sin nombre, sin datos personales:

```json
{
  "sub": "uuid-interno-keycloak",
  "iss": "http://localhost:8080/realms/municipalidad-san-luis",
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

> `preferred_username` viene del scope `openid` que siempre está activo — no se puede quitar y no es un problema. Email, first name y last name ya no aparecen porque quitaste los scopes `email` y `profile` en el paso 5.3.

Para probar: `Clients → sipreb-backend → Client Scopes → Evaluate`  
Seleccionar un usuario y hacer clic en `Generated access token`.

---

## 8. Configurar Flujos de Autenticación

### 8.1 Flujo de login estándar (usuario + contraseña)

El flujo `browser` por defecto de Keycloak es suficiente para empezar. No requiere modificación.

### 8.2 Deshabilitar registro público

`Realm Settings → Login`

```
User registration: OFF       ← los usuarios solo los crea el TENANT_ADMIN
Forgot password: ON
Remember me: OFF
Login with email: ON
```

### 8.3 Configurar OTP (opcional pero recomendado para TENANT_ADMIN)

`Authentication → Required Actions → Configure OTP → Default Action: OFF`

Activarlo solo para usuarios con rol `TENANT_ADMIN`:

`Users → {tenant_admin_user} → Required Actions → Configure OTP ✅`

---

## 9. Crear usuarios de prueba

### 9.1 Crear usuario en el realm del tenant

`Realm municipalidad-san-luis → Users → Create user`

**Pestaña General:**
```
Username: jperez
Email: (vacío — no requerido, ver paso 5.4)
First Name: (vacío)
Last Name: (vacío)
Enabled: ON
```

**Pestaña Attributes** — agregar atributos personalizados:
```
user_id           →  uuid-de-users-table
municipal_code    →  uuid-municipalidad-san-luis
area_id           →  uuid-area-patrimonio
position_id       →  uuid-cargo-tecnico-patrimonio
direct_manager_id →  uuid-jefe-area-patrimonio
```

> ⚠️ `user_id` debe ser el `id` de tu tabla `users`. Crear primero en BD → obtener UUID → crear en Keycloak con ese valor. Ver endpoint de sincronización en sección 11.10.

**Pestaña Credentials:**
```
Password: Temp1234!
Temporary: ON
```

**Pestaña Role Mapping:**
```
Assign role → PATRIMONIO_GESTOR
```

---

## 10. Estructura del proyecto

Estructura real del microservicio `vg-ms-autenticationservice` con arquitectura hexagonal y Spring WebFlux reactivo.

```
vg-ms-autenticationservice/
├── Dockerfile
├── docker-compose.yml
├── .env
├── pom.xml
│
└── src/main/java/edu/pe/vallegrande/AuthenticationService/
    │
    ├── AuthenticationServiceApplication.java
    │
    ├── application/                                    ── CAPA DE APLICACIÓN
    │   ├── service/
    │   │   ├── AssignmentServiceImpl.java              ← asigna roles a usuarios y permisos a roles
    │   │   ├── AuthServiceImpl.java                    ← login, refresh, logout (llama a Keycloak)
    │   │   ├── JwtServiceImpl.java                     ← extrae claims del token
    │   │   ├── PermissionServiceImpl.java
    │   │   ├── PersonServiceImpl.java
    │   │   ├── RoleServiceImpl.java
    │   │   └── UserServiceImpl.java                    ← crea usuario en BD + Keycloak sincronizado
    │   └── util/
    │       └── DateTimeUtil.java
    │
    ├── domain/                                         ── CAPA DE DOMINIO
    │   ├── exception/
    │   │   ├── DuplicateResourceException.java
    │   │   └── ResourceNotFoundException.java
    │   │
    │   ├── model/
    │   │   ├── assignment/
    │   │   │   ├── AssignRoleCommand.java
    │   │   │   ├── RolePermissionAssignment.java
    │   │   │   ├── RolePermissionLink.java
    │   │   │   ├── UserRoleAssignment.java
    │   │   │   └── UserRoleLink.java
    │   │   ├── auth/
    │   │   │   ├── AuthTokens.java                     ← { access_token, refresh_token, expires_in }
    │   │   │   ├── LoginCommand.java                   ← { username, password, municipal_code }
    │   │   │   ├── LoginFailureInfo.java               ← info del bloqueo por intentos fallidos
    │   │   │   ├── LoginResult.java
    │   │   │   ├── RefreshTokenCommand.java
    │   │   │   └── UserPermission.java
    │   │   ├── permission/
    │   │   │   └── PermissionModel.java
    │   │   ├── person/
    │   │   │   ├── CreatePersonCommand.java
    │   │   │   ├── Person.java
    │   │   │   └── UpdatePersonCommand.java
    │   │   ├── role/
    │   │   │   ├── RoleModel.java
    │   │   │   └── UpsertRoleCommand.java
    │   │   └── user/
    │   │       ├── BlockUserCommand.java
    │   │       ├── CreateUserCommand.java
    │   │       ├── SuspendUserCommand.java
    │   │       ├── UpdateUserCommand.java
    │   │       └── UserAccount.java
    │   │
    │   └── ports/
    │       ├── in/                                     ← Puertos de entrada (contratos de servicio)
    │       │   ├── AssignmentService.java
    │       │   ├── AuthService.java                    ← login(LoginCommand) → Mono<LoginResult>
    │       │   ├── JwtService.java
    │       │   ├── PermissionService.java
    │       │   ├── PersonService.java
    │       │   ├── RoleService.java
    │       │   └── UserService.java
    │       └── out/                                    ← Puertos de salida (contratos de repositorio)
    │           ├── AssignmentPermissionQueryPort.java
    │           ├── AuthPermissionPort.java
    │           ├── AuthUserPort.java                   ← busca usuario para login
    │           ├── CurrentUserPort.java                ← obtiene usuario del contexto de seguridad
    │           ├── PermissionPort.java
    │           ├── PersonPort.java
    │           ├── RolePermissionPort.java
    │           ├── RolePort.java
    │           ├── TokenBlacklistPort.java             ← blacklist de tokens revocados
    │           ├── UserPort.java
    │           └── UserRolePort.java
    │
    └── infrastructure/                                 ── CAPA DE INFRAESTRUCTURA
        ├── adapter/
        │   ├── in/web/
        │   │   ├── controller/
        │   │   │   ├── AssignmentController.java
        │   │   │   ├── AuthController.java             ← POST /auth/login, /refresh, /logout
        │   │   │   ├── PermissionController.java
        │   │   │   ├── PersonController.java
        │   │   │   ├── RoleController.java
        │   │   │   └── UserController.java
        │   │   ├── dto/
        │   │   │   ├── LoginRequestDto.java            ← { username, password, municipal_code }
        │   │   │   ├── LoginResponseDto.java
        │   │   │   ├── TokenResponseDto.java
        │   │   │   └── ...
        │   │   └── mapper/
        │   │       ├── AuthWebMapper.java
        │   │       └── ...
        │   │
        │   └── out/
        │       ├── persistence/
        │       │   ├── UserPersistenceAdapter.java     ← implementa UserPort, AuthUserPort
        │       │   ├── entity/User.java                ← mapea tabla users (con user_id = users.id)
        │       │   └── repository/UserRepository.java
        │       └── security/
        │           ├── InMemoryTokenBlacklistAdapter.java   ← implementa TokenBlacklistPort
        │           └── SecurityContextCurrentUserAdapter.java
        │
        └── config/
            ├── CorsConfig.java
            ├── JacksonConfig.java
            ├── R2dbcConfig.java
            └── SwaggerConfig.java
```

> **Nota sobre `KeycloakAdapter`**: si aún no existe en tu proyecto, se agrega como `infrastructure/adapter/out/keycloak/KeycloakAdapter.java` implementando un nuevo puerto de salida `KeycloakPort` en `domain/ports/out/`. Ver sección 11.7.

### Dirección de dependencias (regla hexagonal)

```
  controller (in)  →  ports/in  ←  application/service
                                          ↓
                                   ports/out  ←  adapter/out
                                                  (persistence, security, keycloak)
```

El dominio nunca importa clases de Spring, R2DBC ni Keycloak. Todo eso vive en `infrastructure/`.

---

## 11. Integración Keycloak — Spring WebFlux Reactivo

> ⚠️ **Importante**: `keycloak-spring-boot-adapter` es **bloqueante** y no es compatible con WebFlux. No usarlo. La integración correcta es con `spring-boot-starter-oauth2-resource-server` nativo.

### 11.1 Dependencias — pom.xml

```xml
<!-- WebFlux reactivo -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- OAuth2 Resource Server — compatible con WebFlux reactivo -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- R2DBC PostgreSQL reactivo -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>r2dbc-postgresql</artifactId>
</dependency>

<!-- Pool R2DBC — requerido para config de pool con Neon -->
<dependency>
  <groupId>io.r2dbc</groupId>
  <artifactId>r2dbc-pool</artifactId>
</dependency>

<!-- Redis reactivo -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

### 11.2 application.yml

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/certs
          issuer-uri: ${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}

  r2dbc:
    # Neon requiere SSL — sslMode=require obligatorio
    url: r2dbc:postgresql://${DB_HOST}/${DB_NAME}?sslMode=require
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    pool:
      initial-size: 1
      min-idle: 1
      max-size: 10
      max-idle-time: 10m    # libera conexiones antes de que Neon las cierre
      validation-query: SELECT 1

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

keycloak:
  url: ${KEYCLOAK_URL}
  realm: ${KEYCLOAK_REALM}
  client-id: ${KEYCLOAK_CLIENT_ID}     # sipreb-backend
  client-secret: ${KEYCLOAK_CLIENT_SECRET}
  admin-realm: master
  admin-client-id: admin-cli
  admin-client-secret: ${KEYCLOAK_ADMIN_SECRET}
```

> 💡 **DB_HOST en Neon**: solo el hostname, sin puerto ni `/dbname`.  
> Ejemplo: `ep-cool-forest-123456.us-east-2.aws.neon.tech`

### 11.3 SecurityConfig.java

```java
// infrastructure/config/SecurityConfig.java  ← ya existe, agregar WebFlux Security
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthConverter;

    public SecurityConfig(JwtAuthenticationConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            TenantIsolationFilter tenantFilter) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/auth/login", "/auth/refresh").permitAll()
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/auth/permissions/**").authenticated()
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

### 11.4 JwtAuthenticationConverter.java

Extrae el claim `roles` de primer nivel configurado en el mapper de Keycloak (sección 7).

```java
// infrastructure/config/SecurityConfig.java — o clase separada si ya tienes la tuya
@Component
public class JwtAuthenticationConverter implements
        Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");

        Collection<GrantedAuthority> authorities = roles == null
            ? Collections.emptyList()
            : roles.stream()
                .map(SimpleGrantedAuthority::new)   // sin prefijo ROLE_
                .collect(Collectors.toList());

        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }
}
```

### 11.5 TenantIsolationFilter.java

Valida que el `municipal_code` del JWT coincida con el header `X-Municipal-Code` del request.

```java
// infrastructure/adapter/out/security/ — junto a SecurityContextCurrentUserAdapter.java
@Component
public class TenantIsolationFilter implements WebFilter {

    private static final String TENANT_HEADER = "X-Municipal-Code";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestTenant = exchange.getRequest()
            .getHeaders().getFirst(TENANT_HEADER);

        if (requestTenant == null) {
            return chain.filter(exchange);
        }

        return exchange.getPrincipal()
            .cast(JwtAuthenticationToken.class)
            .flatMap(auth -> {
                String tokenTenant = auth.getToken()
                    .getClaimAsString("municipal_code");

                if (!requestTenant.equals(tokenTenant)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            })
            .switchIfEmpty(chain.filter(exchange));
    }
}
```

### 11.6 Login con username + password (ROPC)

Flujo **Resource Owner Password Credentials**. Habilitado con `Direct access grants: ON` del paso 5.1. El frontend llama a `POST /auth/login` del Auth Service, que internamente llama a Keycloak con `grant_type=password`.

```
POST /auth/login  { username, password, municipal_code }
         ↓
[AuthServiceImpl — application/service]
1. Resolver realm desde municipal_code
2. findByUsernameAndMunicipalCode → verificar status = ACTIVE
3. Verificar blocked_until < NOW()
         ↓ válido
4. keycloakPort.login(username, password, realm)
         ↓ 200 OK
5. resetLoginAttempts · updateLastLogin
6. return AuthTokens { access_token, refresh_token, expires_in }
         ↓ 401 Keycloak
5. login_attempts += 1 · si >= 5 → blocked_until = NOW() + 15min
7. return 401
```

```java
// application/service/AuthServiceImpl.java  ← ya existe, alinear con este flujo
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthUserPort authUserPort;    // busca usuario para login
    private final KeycloakPort keycloakPort;    // llama a Keycloak
    private final UserPort userPort;            // actualiza intentos / last_login

    @Override
    public Mono<LoginResult> login(LoginCommand command) {
        return authUserPort.findByUsernameAndMunicipalCode(
                    command.username(), command.municipalCode())
            .switchIfEmpty(Mono.error(
                new ResourceNotFoundException("Usuario no encontrado")))
            .flatMap(this::validateUserStatus)
            .flatMap(user -> {
                String realm = resolveRealm(command.municipalCode());
                return keycloakPort.login(
                            command.username(), command.password(), realm)
                    .flatMap(tokens ->
                        userPort.resetLoginAttempts(user.getId())
                            .then(userPort.updateLastLogin(user.getId()))
                            .thenReturn(LoginResult.success(tokens))
                    )
                    .onErrorResume(KeycloakAuthException.class, e ->
                        handleFailedAttempt(user).then(Mono.error(e))
                    );
            });
    }

    private Mono<UserAccount> validateUserStatus(UserAccount user) {
        if (!"ACTIVE".equals(user.getStatus()))
            return Mono.error(new ResourceNotFoundException("Cuenta inactiva"));
        if (user.getBlockedUntil() != null &&
                user.getBlockedUntil().isAfter(LocalDateTime.now()))
            return Mono.error(new ResourceNotFoundException(
                "Cuenta bloqueada hasta " + user.getBlockedUntil()));
        return Mono.just(user);
    }

    private Mono<Void> handleFailedAttempt(UserAccount user) {
        int attempts = user.getLoginAttempts() + 1;
        if (attempts >= 5)
            return userPort.blockUser(user.getId(),
                LocalDateTime.now().plusMinutes(15), "MAX_ATTEMPTS_EXCEEDED");
        return userPort.incrementLoginAttempts(user.getId());
    }
}
```

> ⚠️ **Doble capa de bloqueo**: `blocked_until` en tu BD + Brute Force Protection en Keycloak (paso 2.3). Si alguien llama directamente al endpoint de token de Keycloak, también queda bloqueado.

### 11.7 Puerto de salida — KeycloakPort.java

```java
// domain/ports/out/KeycloakPort.java  ← NUEVO puerto a agregar
public interface KeycloakPort {
    Mono<AuthTokens> login(String username, String password, String realm);
    Mono<AuthTokens> refreshToken(String refreshToken, String realm);
    Mono<Void> logout(String keycloakUserId, String realm);
    Mono<String> createUser(CreateKeycloakUserCommand command, String realm);
    Mono<Void> assignRole(String keycloakUserId, String roleName, String realm);
    Mono<Void> syncRoles(String keycloakUserId, List<String> roleNames, String realm);
}
```

### 11.8 Adaptador de salida — KeycloakAdapter.java

```java
// infrastructure/adapter/out/keycloak/KeycloakAdapter.java  ← NUEVO adaptador
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
            .onStatus(HttpStatusCode::is4xxClientError, r ->
                Mono.error(new KeycloakAuthException("Credenciales inválidas")))
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
                    "email",    cmd.email(),
                    "enabled",  true,
                    "attributes", Map.of(
                        "user_id",          List.of(cmd.userId()),       // ← tu users.id
                        "municipal_code",    List.of(cmd.municipalCode()),
                        "area_id",          List.of(cmd.areaId()),
                        "position_id",      List.of(cmd.positionId()),
                        "direct_manager_id",List.of(cmd.directManagerId())
                    )
                ))
                .retrieve()
                .toBodilessEntity()
                // Keycloak devuelve el UUID del nuevo usuario en el header Location
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

### 11.9 Sincronización de roles — BD como fuente de verdad

```
BD (roles / users_roles)  →  fuente de verdad · auditoría · lógica de negocio
Keycloak (realm roles)    →  reflejo para el JWT · control de acceso en endpoints
```

Nunca asignar roles directamente desde la UI de Keycloak. Siempre desde `AssignmentServiceImpl`.

```java
// application/service/AssignmentServiceImpl.java  ← ya existe, agregar sync Keycloak
@Override
public Mono<Void> assignRoleToUser(AssignRoleCommand command) {
    return userRolePort.assign(command)                       // 1. guarda en BD
        .then(authUserPort.findById(command.userId()))
        .flatMap(user -> {
            String realm = resolveRealm(user.getMunicipalCode());
            return keycloakPort.assignRole(               // 2. refleja en Keycloak
                user.getKeycloakId(), command.roleName(), realm
            );
        });
}
```

> **Columna `keycloak_id` necesaria**: agrega `ALTER TABLE users ADD COLUMN keycloak_id VARCHAR(36);` para guardar el `sub` de Keycloak y poder llamar a la Admin API.

### 11.10 Endpoint de sincronización de usuarios

En lugar de un scheduler automático, la sincronización se expone como `POST /auth/users/sync` protegido con rol `SUPER_ADMIN`. Permite sincronizar usuarios existentes en BD que aún no tienen `keycloak_id`, y también sincronizar uno individual.

#### Puerto de entrada

```java
// domain/ports/in/UserService.java  ← ya existe, agregar método
public interface UserService {
    // ... métodos existentes
    Mono<SyncResult> syncUsersToKeycloak(String municipalCode);
    Mono<SyncResult> syncSingleUserToKeycloak(UUID userId);
}
```

#### UserServiceImpl — lógica de sincronización

```java
// application/service/UserServiceImpl.java  ← ya existe
@Override
public Mono<SyncResult> syncUsersToKeycloak(String municipalCode) {
    return userPort.findAllWithoutKeycloakId(municipalCode)
        .flatMap(user -> syncUser(user))
        .collectList()
        .map(results -> new SyncResult(
            results.size(),
            results.stream().filter(SyncUserResult::success).count(),
            results.stream().filter(r -> !r.success()).toList()
        ));
}

@Override
public Mono<SyncResult> syncSingleUserToKeycloak(UUID userId) {
    return userPort.findById(userId)
        .flatMap(user -> syncUser(user))
        .map(r -> new SyncResult(1, r.success() ? 1 : 0,
            r.success() ? List.of() : List.of(r)));
}

private Mono<SyncUserResult> syncUser(UserAccount user) {
    String realm = resolveRealm(user.getMunicipalCode());
    return keycloakPort.createUser(
            CreateKeycloakUserCommand.from(user), realm)
        .flatMap(keycloakId ->
            userPort.updateKeycloakId(user.getId(), keycloakId)
                .then(userRolePort.findRoleNamesByUserId(user.getId()))
                .flatMap(roles -> keycloakPort.syncRoles(keycloakId, roles, realm))
                .thenReturn(SyncUserResult.ok(user.getUsername()))
        )
        .onErrorResume(e ->
            Mono.just(SyncUserResult.failed(user.getUsername(), e.getMessage()))
        );
}
```

#### UserController — endpoint POST

```java
// infrastructure/adapter/in/web/controller/UserController.java  ← ya existe
@PostMapping("/sync")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public Mono<ResponseEntity<SyncResultDto>> syncUsers(
        @RequestBody SyncUsersRequestDto dto) {
    // Si viene userId → sync individual, si no → sync por municipalidad
    if (dto.userId() != null) {
        return userService.syncSingleUserToKeycloak(dto.userId())
            .map(result -> ResponseEntity.ok(UserWebMapper.toSyncDto(result)));
    }
    return userService.syncUsersToKeycloak(dto.municipalCode())
        .map(result -> ResponseEntity.ok(UserWebMapper.toSyncDto(result)));
}
```

#### DTOs

```java
// SyncUsersRequestDto — body del POST
public record SyncUsersRequestDto(
    UUID userId,          // opcional — sync de un usuario específico
    String municipalCode  // requerido si userId es null — sync de toda la municipalidad
) {}

// SyncResultDto — respuesta
public record SyncResultDto(
    int total,
    long synced,
    List<String> failed   // usernames que fallaron
) {}
```

**Ejemplo de uso:**

```bash
# Sincronizar todos los usuarios de una municipalidad
POST /auth/users/sync
Authorization: Bearer <token-SUPER_ADMIN>
{
  "municipalCode": "uuid-municipalidad-san-luis"
}

# Sincronizar un usuario específico
POST /auth/users/sync
Authorization: Bearer <token-SUPER_ADMIN>
{
  "userId": "uuid-del-usuario"
}

# Respuesta
{
  "total": 47,
  "synced": 45,
  "failed": ["usuario_problematico_1", "usuario_problematico_2"]
}
```

> ✅ Agregar `POST /auth/users/sync` al `SecurityConfig` como ruta protegida con rol `SUPER_ADMIN`, NO en `.permitAll()`.

---

### 11.11 AuthController.java y PermissionController.java

```java
// infrastructure/adapter/in/web/controller/AuthController.java  ← ya existe
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponseDto>> login(
            @RequestBody @Valid LoginRequestDto dto) {
        return authService.login(
                new LoginCommand(dto.username(), dto.password(), dto.municipalCode()))
            .map(result -> ResponseEntity.ok(AuthWebMapper.toDto(result)))
            .onErrorResume(ResourceNotFoundException.class,
                e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponseDto>> refresh(
            @RequestBody RefreshTokenRequestDto dto) {
        return authService.refresh(
                new RefreshTokenCommand(dto.refreshToken(), dto.municipalCode()))
            .map(tokens -> ResponseEntity.ok(AuthWebMapper.toTokenDto(tokens)));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Void>> logout(@AuthenticationPrincipal Jwt jwt) {
        String userId      = jwt.getClaimAsString("user_id");   // tu users.id
        String municipalCode = jwt.getClaimAsString("municipal_code");
        String keycloakId  = jwt.getSubject();                   // sub de Keycloak
        return authService.logout(userId, keycloakId, municipalCode)
            .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
```

```java
// infrastructure/adapter/in/web/controller/PermissionController.java
@RestController
@RequestMapping("/auth/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<String>>> getPermissions(
            @PathVariable String userId,
            @AuthenticationPrincipal Jwt jwt) {
        String municipalCode = jwt.getClaimAsString("municipal_code");
        return permissionService.getPermissionsForUser(userId, municipalCode)
            .map(ResponseEntity::ok);
    }
}
```

```java
// infrastructure/adapter/out/security/RedisCacheAdapter.java  ← NUEVO
@Component
public class RedisCacheAdapter implements CachePort {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofSeconds(60);

    @Override
    public Mono<List<String>> getPermissions(String userId, String municipalCode) {
        String key = "perms:" + userId + ":" + municipalCode;
        return redisTemplate.opsForValue().get(key)
            .map(json -> parseJson(json))
            .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<Void> setPermissions(String userId, String municipalCode,
                                      List<String> permissions) {
        String key = "perms:" + userId + ":" + municipalCode;
        return redisTemplate.opsForValue()
            .set(key, toJson(permissions), TTL)
            .then();
    }

    @Override
    public Mono<Void> invalidate(String userId, String municipalCode) {
        String key = "perms:" + userId + ":" + municipalCode;
        return redisTemplate.delete(key).then();
    }
}
```

---

## 12. Automatizar onboarding

Al crear una nueva municipalidad, el Tenant Management Service debe ejecutar este flujo automáticamente vía la Admin API de Keycloak.

### 12.1 Script de onboarding (Java — KeycloakAdapter)

```typescript
// keycloak-onboarding.service.ts

const KEYCLOAK_ADMIN_URL = process.env.KEYCLOAK_URL + '/admin/realms';

// 1. Obtener token de admin
async function getAdminToken(): Promise<string> {
  const res = await fetch(`${process.env.KEYCLOAK_URL}/realms/master/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'client_credentials',
      client_id: 'admin-cli',
      client_secret: process.env.KEYCLOAK_ADMIN_SECRET,
    }),
  });
  const data = await res.json();
  return data.access_token;
}

// 2. Crear realm para el nuevo tenant
async function createTenantRealm(municipalitySlug: string, displayName: string) {
  const token = await getAdminToken();

  await fetch(KEYCLOAK_ADMIN_URL, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      realm: `municipalidad-${municipalitySlug}`,
      displayName,
      enabled: true,
      registrationAllowed: false,
      bruteForceProtected: true,
      permanentLockout: false,
      maxFailureWaitSeconds: 900,
      failureFactor: 5,
      accessTokenLifespan: 300,       // 5 minutos
      ssoSessionIdleTimeout: 1800,    // 30 minutos
      ssoSessionMaxLifespan: 28800,   // 8 horas
    }),
  });
}

// 3. Crear roles base (is_system = true)
const SYSTEM_ROLES = [
  'TENANT_ADMIN', 'TENANT_CONFIG_MANAGER',
  'PATRIMONIO_GESTOR', 'PATRIMONIO_OPERARIO', 'PATRIMONIO_VIEWER',
  'MOVIMIENTOS_SOLICITANTE', 'MOVIMIENTOS_APROBADOR', 'MOVIMIENTOS_VIEWER',
  'INVENTARIO_COORDINADOR', 'INVENTARIO_VERIFICADOR',
  'MANTENIMIENTO_GESTOR', 'MANTENIMIENTO_VIEWER',
  'REPORTES_VIEWER', 'REPORTES_SCHEDULER', 'AUDITORIA_VIEWER',
];

async function createSystemRoles(realmName: string) {
  const token = await getAdminToken();

  for (const roleName of SYSTEM_ROLES) {
    await fetch(`${KEYCLOAK_ADMIN_URL}/${realmName}/roles`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ name: roleName }),
    });
  }
}

// 4. Crear client sipreb-auth-service en el nuevo realm
async function createAuthClient(realmName: string) {
  const token = await getAdminToken();

  await fetch(`${KEYCLOAK_ADMIN_URL}/${realmName}/clients`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      clientId: 'sipreb-auth-service',
      enabled: true,
      clientAuthenticatorType: 'client-secret',
      directAccessGrantsEnabled: true,
      serviceAccountsEnabled: true,
      standardFlowEnabled: true,
      bearerOnly: false,
    }),
  });
}

// Flujo completo de onboarding
export async function onboardNewTenant(municipalitySlug: string, displayName: string) {
  const realmName = `municipalidad-${municipalitySlug}`;
  await createTenantRealm(municipalitySlug, displayName);
  await createSystemRoles(realmName);
  await createAuthClient(realmName);
  // aquí también: crear client scope sipreb-claims y asignarlo al client
  console.log(`✅ Realm ${realmName} configurado correctamente`);
}
```

---

## 13. Seguridad adicional

### 13.1 HTTPS en producción

```bash
# start con certificado SSL — Keycloak 26.x
docker run quay.io/keycloak/keycloak:26.5.4 start \
  --hostname=auth.sipreb.pe \
  --https-certificate-file=/etc/ssl/certs/cert.pem \
  --https-certificate-key-file=/etc/ssl/certs/key.pem
```

### 13.2 Configurar CORS por realm

`Realm Settings → Security Defenses → Headers`

```
X-Frame-Options: SAMEORIGIN
Content-Security-Policy: frame-src 'self'; frame-ancestors 'self'; object-src 'none';
```

`Clients → sipreb-auth-service → Web Origins`
```
https://app.sipreb.pe
https://api.sipreb.pe
```

### 13.3 Deshabilitar endpoints innecesarios

`Realm Settings → General`

```
User-managed access: OFF
```

`Realm Settings → Login`
```
User registration: OFF
```

### 13.4 Rotación de secrets

Cada vez que se necesite rotar el client secret:

`Clients → sipreb-auth-service → Credentials → Regenerate`

Actualizar la variable `KEYCLOAK_CLIENT_SECRET` en el Auth Service y reiniciar.

### 13.5 Configurar eventos y auditoría

`Realm Settings → Events → Config`

```
Save Events: ON
Saved Types: LOGIN, LOGIN_ERROR, LOGOUT, TOKEN_EXCHANGE, ...
Events Expiration: 30 days
```

`Realm Settings → Events → Admin Events`

```
Save Admin Events: ON
Include Representation: ON
```

---

## 14. Variables de entorno

Variables que el Auth Service necesita. Con Neon **no hay `DB_PORT`** separado.

```env
# ── Keycloak ────────────────────────────────────────────────
KEYCLOAK_URL=http://localhost:8080
KEYCLOAK_ADMIN_SECRET=<secret-del-admin-cli>

# Por tenant (resolver dinámicamente según el municipal_code del request)
KEYCLOAK_REALM=municipalidad-{slug}
KEYCLOAK_CLIENT_ID=sipreb-backend
KEYCLOAK_CLIENT_SECRET=<secret-generado-en-paso-5.2>

# ── Neon PostgreSQL (Auth Service) ──────────────────────────
# Solo el hostname — sin puerto, sin /dbname
DB_HOST=ep-cool-forest-123456.us-east-2.aws.neon.tech
DB_NAME=auth_service
DB_USER=<neon-user>
DB_PASSWORD=<neon-password>

# ── Neon PostgreSQL (Keycloak) ───────────────────────────────
# Estos van en el docker-compose.yml de Keycloak, no en el Auth Service
# KC_DB_URL=jdbc:postgresql://<host>.neon.tech/keycloak?sslmode=require
# KC_DB_USERNAME=<neon-user>
# KC_DB_PASSWORD=<neon-password>

# ── Redis ────────────────────────────────────────────────────
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PERMISSIONS_TTL=60
```

### Neon — separación de bases de datos recomendada

```
Proyecto Neon: sipreb-prod
├── Database: keycloak        → Keycloak (auto-creada al primer arranque, sin script)
├── Database: auth_service    → Auth Service (R2DBC)
├── Database: master_tenant   → Tenant Management Service
├── Database: muni_san_luis   → tenant específico
└── Database: muni_los_olivos → tenant específico
```

### Resolución dinámica de realm

```java
// application/util/TenantRealmResolver.java
@Component
public class TenantRealmResolver {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final UserPort userPort;   // o un TenantPort si tienes acceso a master_tenant

    public String resolve(String municipalCode) {
        return cache.computeIfAbsent(municipalCode,
            code -> "municipalidad-" + fetchSlug(code));
    }

    private String fetchSlug(String municipalCode) {
        // Consultar BD para obtener el slug del tenant
        return userPort.findMunicipalSlug(municipalCode).block();
    }
}
```

### Resolución dinámica de realm

Ya que el sistema es multi-tenant, el Auth Service necesita resolver el realm a partir del `municipal_code` del request:

```typescript
// tenant-realm.resolver.ts
const realmCache = new Map<string, string>();

export async function getRealmForTenant(municipalCode: string): Promise<string> {
  if (realmCache.has(municipalCode)) return realmCache.get(municipalCode)!;

  // Consultar master_tenant para obtener el slug
  const tenant = await masterDb.query(
    'SELECT slug FROM municipalidades WHERE id = $1',
    [municipalCode]
  );

  const realm = `municipalidad-${tenant.rows[0].slug}`;
  realmCache.set(municipalCode, realm);
  return realm;
}
```

---

## 📎 Referencias útiles

| Recurso | URL |
|---------|-----|
| Documentación oficial Keycloak | https://www.keycloak.org/documentation |
| Admin REST API | `{KEYCLOAK_URL}/admin/realms` |
| OpenID Configuration por realm | `{KEYCLOAK_URL}/realms/{realm}/.well-known/openid-configuration` |
| JWKS (claves públicas para verificar tokens) | `{KEYCLOAK_URL}/realms/{realm}/protocol/openid-connect/certs` |
| Endpoint de token | `{KEYCLOAK_URL}/realms/{realm}/protocol/openid-connect/token` |
| Endpoint de userinfo | `{KEYCLOAK_URL}/realms/{realm}/protocol/openid-connect/userinfo` |

---

*Versión 3.1 — SIPREB 2025 — Keycloak 26.5.4 · Spring Boot WebFlux · Arquitectura Hexagonal · Neon PostgreSQL · sipreb-backend*
