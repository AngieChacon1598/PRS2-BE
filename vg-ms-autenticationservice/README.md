# 🔐 Microservicio de Autenticación y Autorización

> Sistema empresarial de gestión de usuarios, roles y permisos con arquitectura reactiva

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/downloads/#java17)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-purple.svg)](https://docs.spring.io/spring-framework/reference/web/webflux.html)
[![R2DBC](https://img.shields.io/badge/R2DBC-Reactive-red.svg)](https://r2dbc.io/)
[![Docker Hub](https://img.shields.io/badge/Docker%20Hub-Repository-blue.svg)](https://hub.docker.com/r/henrylunazco/vg-ms-autenticationservice)

Microservicio completo de autenticación y autorización construido con **Spring Boot WebFlux**, **R2DBC**, **Keycloak**, **JWT** y **PostgreSQL**. Implementa autenticación centralizada mediante Keycloak, gestión de identidades reactiva y multi-tenancy.

---

## 📋 Imagen 

```bash
docker pull henrylunazco/vg-ms-autenticationservice:v1
```

---

---

## 📋 Tabla de Contenidos

- [Características](#-características-principales)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura](#-arquitectura-del-sistema)
- [Modelo de Datos](#-modelo-de-datos)
- [Endpoints API](#-endpoints-principales)
- [Instalación](#-instalación-rápida)
- [Configuración](#-configuración)
- [Uso](#-ejemplos-de-uso)
- [Seguridad](#-seguridad)
- [Documentación](#-documentación-api)

---

## ✨ Características Principales

- JWT Authentication (access + refresh tokens) via Keycloak
- Centralized Identity Management con Keycloak
- Rate limiting y protección DDoS
- Bloqueo automático gestionado por Keycloak
- Multi-tenancy (municipal_code) compatible con Realms/Attributes de Keycloak
- Auto-asignación de roles basada en cargos (Integración con ConfigService)

### 👥 Gestión
- CRUD completo de usuarios, personas, roles y permisos
- Búsquedas avanzadas y filtros múltiples
- Auditoría automática (created_by, updated_by)
- Soft delete y restauración
- Jerarquía organizacional

### 🎭 RBAC
- Roles jerárquicos (SUPER_ADMIN, ADMIN, USER_MANAGER, VIEWER)
- Permisos granulares por módulo y acción
- Asignación flexible con expiración
- Cálculo de permisos efectivos

### 🛠️ Técnico
- Arquitectura reactiva (WebFlux + R2DBC)
- PostgreSQL con conexiones no-bloqueantes
- Swagger/OpenAPI documentation
- Spring Boot Actuator
- Versionado optimista

---

## 🚀 Stack Tecnológico

| Categoría | Tecnología | Versión |
|-----------|-----------|---------|
| **Backend** | Java | 17 LTS |
| | Spring Boot | 3.5.7 |
| | Spring WebFlux | 6.x |
| | Spring Data R2DBC | 3.x |
| | Spring Security | 6.x |
| **Base de Datos** | PostgreSQL | 15+ |
| | R2DBC PostgreSQL | 1.x |
| **Seguridad** | JJWT | 0.12.3 |
| | BCrypt | - |
| **Documentación** | SpringDoc OpenAPI | 2.7.0 |
| | Jakarta Validation | 3.x |
| **Herramientas** | Maven | 3.9+ |
| | Lombok | 1.18.x |
| | SLF4J + Logback | 2.x |

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────┐
│          CAPA DE PRESENTACIÓN               │
│  Controllers + Security Filters             │
│  - AuthController  - JwtFilter              │
│  - UserController  - RateLimitFilter        │
│  - RoleController  - CorsFilter             │
└─────────────────┬───────────────────────────┘
                  │ DTOs
                  ▼
┌─────────────────────────────────────────────┐
│       CAPA DE LÓGICA DE NEGOCIO             │
│  Services + Exception Handlers              │
│  - AuthService    - UserService             │
│  - RoleService    - PermissionService       │
│  - JwtService     - GlobalExceptionHandler  │
└─────────────────┬───────────────────────────┘
                  │ Entities
                  ▼
┌─────────────────────────────────────────────┐
│        CAPA DE PERSISTENCIA                 │
│  Repositories (Spring Data R2DBC)           │
│  - UserRepository  - RoleRepository         │
│  - PersonRepository - PermissionRepository  │
└─────────────────┬───────────────────────────┘
                  │ R2DBC
                  ▼
┌─────────────────────────────────────────────┐
│           PostgreSQL 15+                    │
└─────────────────────────────────────────────┘
```

---

## 📊 Modelo de Datos

### Diagrama ER Simplificado

```
persons (1) ──── (1) users (N) ──── (M) users_roles (M) ──── (1) roles
                                                                  │
                                                                  │ (N)
                                                                  │
                                                                  ▼ (M)
                                                           roles_permissions
                                                                  │
                                                                  │ (M)
                                                                  ▼ (1)
                                                            permissions
```

### Tablas Principales

**users** - Credenciales y estado
- `id`, `username`, `password_hash`
- `person_id`, `area_id`, `position_id`, `direct_manager_id`
- `municipal_code`, `status`, `last_login`
- `login_attempts`, `blocked_until`, `preferences`
- Auditoría: `created_by`, `created_at`, `updated_by`, `updated_at`, `version`

**persons** - Información personal
- `id`, `document_type_id`, `document_number`
- `person_type` (N=Natural, J=Jurídica)
- `first_name`, `last_name`, `middle_name`
- `birth_date`, `gender`, `personal_email`, `personal_phone`
- `address`, `municipal_code`, `status`

**roles** - Roles del sistema
- `id`, `name`, `description`
- `is_system` (protegidos), `active`
- `municipal_code`, `created_by`, `created_at`

**permissions** - Permisos granulares
- `id`, `module`, `action`, `resource`
- `description`, `status`, `municipal_code`

**users_roles** - Asignación usuarios-roles
- `user_id`, `role_id`, `assigned_by`, `assigned_at`
- `expiration_date`, `active`, `municipal_code`

**roles_permissions** - Asignación roles-permisos
- `role_id`, `permission_id`, `status`, `municipal_code`

---

## 🌐 Endpoints Principales

**Base URL:** `http://localhost:5002/api/v1`

### Autenticación

```bash
POST   /auth/login              # Iniciar sesión
POST   /auth/refresh            # Renovar token
POST   /auth/logout             # Cerrar sesión
POST   /auth/validate           # Validar token
```

### Usuarios

```bash
GET    /users                   # Listar usuarios (paginado)
GET    /users/{id}              # Obtener usuario
GET    /users/username/{name}   # Buscar por username
POST   /users                   # Crear usuario
PUT    /users/{id}              # Actualizar usuario
DELETE /users/{id}              # Eliminar (soft delete)
PATCH  /users/{id}/suspend      # Suspender usuario
PATCH  /users/{id}/block        # Bloquear usuario
```

### Personas

```bash
GET    /persons                       # Listar personas
GET    /persons/{id}                  # Obtener persona
GET    /persons/document/{type}/{num} # Buscar por documento
POST   /persons                       # Crear persona
PUT    /persons/{id}                  # Actualizar persona
DELETE /persons/{id}                  # Eliminar persona
```

### Roles

```bash
GET    /roles                   # Listar roles
GET    /roles/{id}              # Obtener rol
POST   /roles                   # Crear rol
PUT    /roles/{id}              # Actualizar rol
DELETE /roles/{id}              # Eliminar rol
```

### Permisos

```bash
GET    /permissions             # Listar permisos
GET    /permissions/{id}        # Obtener permiso
POST   /permissions             # Crear permiso
PUT    /permissions/{id}        # Actualizar permiso
DELETE /permissions/{id}        # Eliminar permiso
```

### Asignaciones

```bash
GET    /users/{userId}/roles                    # Roles del usuario
POST   /users/{userId}/roles/{roleId}           # Asignar rol
DELETE /users/{userId}/roles/{roleId}           # Quitar rol
GET    /roles/{roleId}/permissions              # Permisos del rol
POST   /roles/{roleId}/permissions/{permId}     # Asignar permiso
DELETE /roles/{roleId}/permissions/{permId}     # Quitar permiso
GET    /users/{userId}/effective-permissions    # Permisos efectivos
```

---

## 🚀 Instalación Rápida

### Prerrequisitos

```bash
# Verificar instalaciones
java -version    # Java 17+
mvn -version     # Maven 3.9+
psql --version   # PostgreSQL 15+
```

### Paso a Paso

```bash
# 1. Clonar repositorio
git clone https://github.com/tu-usuario/authentication-service.git
cd authentication-service

# 2. Configurar base de datos
psql -U postgres
CREATE DATABASE ms_authentication_service;
\c ms_authentication_service
\i src/main/resources/Database/schema.sql
\i src/main/resources/Database/data.sql
\q

# 3. Configurar variables de entorno
export DATABASE_URL="r2dbc:postgresql://localhost:5432/ms_authentication_service"
export DB_USERNAME="postgres"
export DB_PASSWORD="tu_password"
export JWT_SECRET="tu-clave-secreta-minimo-256-bits"

# 4. Compilar
mvn clean install -DskipTests

# 5. Ejecutar
mvn spring-boot:run

# 6. Verificar
curl http://localhost:5002/actuator/health
# Respuesta: {"status":"UP"}
```

### Acceso Swagger

```
http://localhost:5002/swagger-ui.html
```

### Usuario por Defecto

```
Username: dgonzales
Password: SuperAdmin
Role: SUPER_ADMIN
```

---

## ⚙️ Configuración

### Variables de Entorno

| Variable | Descripción | Default | Requerido |
|----------|-------------|---------|-----------|
| `DATABASE_URL` | URL R2DBC PostgreSQL | localhost:5432 | ✅ |
| `DB_USERNAME` | Usuario BD | postgres | ✅ |
| `DB_PASSWORD` | Contraseña BD | postgres | ✅ |
| `JWT_SECRET` | Clave JWT (min 256 bits) | - | ✅ |
| `JWT_EXPIRATION` | Expiración access token (ms) | 3600000 | ❌ |
| `JWT_REFRESH_EXPIRATION` | Expiración refresh token (ms) | 86400000 | ❌ |
| `PORT` | Puerto servidor | 5002 | ❌ |

### application.yml

```yaml
spring:
  application:
    name: AuthenticationService
  r2dbc:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    pool:
      initial-size: 1
      max-size: 10

server:
  port: ${PORT:5002}

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:3600000}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:86400000}

logging:
  level:
    root: INFO
    "[edu.pe.vallegrande]": DEBUG
  file:
    name: logs/authentication-service.log
```

---

## 💡 Ejemplos de Uso

### 1. Autenticación Completa

```bash
# Login
curl -X POST http://localhost:5002/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "dgonzales",
    "password": "SuperAdmin"
  }'

# Respuesta
{
  "userId": "40a5c2fa-ab64-4a6e-8335-2a136ebeed1a",
  "username": "dgonzales",
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600,
  "municipalCode": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
}

# Usar token
curl -X GET http://localhost:5002/api/v1/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."

# Renovar token
curl -X POST http://localhost:5002/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "eyJhbGciOiJIUzI1NiIs..."}'
```

### 2. Crear Usuario

```bash
curl -X POST http://localhost:5002/api/v1/users \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jperez",
    "password": "Password123!",
    "personId": "40a5c2fa-ab64-4a6e-8335-2a136ebeed1a",
    "status": "ACTIVE"
  }'
```

### 3. Asignar Rol a Usuario

```bash
curl -X POST http://localhost:5002/api/v1/users/{userId}/roles/{roleId} \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "expirationDate": "2025-12-31",
    "active": true
  }'
```

### 4. Integración JavaScript

```javascript
// Configuración Axios
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:5002/api/v1',
  headers: { 'Content-Type': 'application/json' }
});

// Interceptor para token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para refresh
api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      const { data } = await axios.post('/auth/refresh', { refreshToken });
      localStorage.setItem('accessToken', data.accessToken);
      return api(error.config);
    }
    return Promise.reject(error);
  }
);

// Servicios
export const authService = {
  async login(username, password) {
    const { data } = await api.post('/auth/login', { username, password });
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data;
  },
  
  async logout() {
    await api.post('/auth/logout');
    localStorage.clear();
  }
};

export const userService = {
  getAll: (filters) => api.get('/users', { params: filters }),
  getById: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  delete: (id) => api.delete(`/users/${id}`)
};
```

---

## 🔒 Seguridad

### Características

- **JWT** con HS256, access (1h) + refresh (24h)
- **BCrypt** con cost factor 10
- **Rate Limiting** por IP
- **Bloqueo automático** tras 5 intentos fallidos
- **CORS** configurado
- **Auditoría** automática con JWT
- **Validaciones** Bean Validation + custom
- **HTTPS** recomendado en producción

### Mejores Prácticas

```yaml
# ✅ Producción
jwt:
  secret: ${JWT_SECRET}  # Variable de entorno
  expiration: 1800000     # 30 min (más seguro)

logging:
  level:
    root: WARN

# ❌ Nunca hacer
jwt:
  secret: "hardcoded-secret"  # ¡NO!
```

**Recomendaciones:**
- Usar variables de entorno/gestores de secretos
- Rotar JWT_SECRET periódicamente
- Implementar HTTPS en producción
- Configurar firewall y WAF
- Mantener dependencias actualizadas
- Auditorías de seguridad regulares

---

## 📚 Documentación API

### Swagger UI

```
http://localhost:5002/swagger-ui.html
```

**Características:**
- Documentación completa de endpoints
- Pruebas interactivas
- Esquemas request/response
- Autenticación JWT integrada

**Uso:**
1. Acceder a Swagger UI
2. Login en `/api/v1/auth/login`
3. Copiar `accessToken`
4. Click en "Authorize"
5. Ingresar `Bearer {token}`
6. Probar endpoints

### OpenAPI JSON

```
http://localhost:5002/v3/api-docs
```

### Spring Boot Actuator

```bash
# Health check
curl http://localhost:5002/actuator/health

# Info
curl http://localhost:5002/actuator/info

# Métricas
curl http://localhost:5002/actuator/metrics
```

---

## 📊 Roles y Permisos Predefinidos

### Roles del Sistema

| Rol | Descripción | Módulos |
|-----|-------------|---------|
| **SUPER_ADMIN** | Acceso total | users, persons, roles, permissions, assignments |
| **ADMIN** | Gestión de usuarios | users, persons, roles |
| **USER_MANAGER** | Gestión básica | users (read/write), persons |
| **VIEWER** | Solo lectura | users (read), persons (read) |

### Módulos y Acciones

| Módulo | Acciones | Recursos |
|--------|----------|----------|
| `users` | read, write, delete, manage | * |
| `persons` | read, write, delete | * |
| `roles` | read, write, delete, manage | * |
| `permissions` | read, write, delete | * |
| `assignments` | read, write, delete | * |

---

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Tests específicos
mvn test -Dtest=AuthServiceTest

# Con cobertura
mvn test jacoco:report
```

---

## 📦 Despliegue

### JAR Ejecutable

```bash
# Compilar
mvn clean package -DskipTests

# Ejecutar
java -jar target/AuthenticationService-0.0.1-SNAPSHOT.jar
```

### Docker

#### Opción 1: Usar imagen de Docker Hub (Recomendado)

```bash
# Pull de la imagen
docker pull henrylunazco/vg-ms-autenticationservice:v1

# Ejecutar
docker run -d \
  --name auth-service \
  -p 5002:5002 \
  -e SPRING_R2DBC_URL="r2dbc:postgresql://host.docker.internal:5432/authentication_db" \
  -e SPRING_R2DBC_USERNAME="postgres" \
  -e SPRING_R2DBC_PASSWORD="postgres" \
  -e KEYCLOAK_URL="http://host.docker.internal:8080" \
  henrylunazco/vg-ms-autenticationservice:v1
```

**Docker Hub:** [henrylunazco/vg-ms-autenticationservice](https://hub.docker.com/r/henrylunazco/vg-ms-autenticationservice)

#### Opción 2: Construir localmente

```bash
# Build
docker build -t auth-service:latest .

# Run
docker run -d \
  --name auth-service \
  -p 5002:5002 \
  -e SPRING_R2DBC_URL="..." \
  -e SPRING_R2DBC_USERNAME="..." \
  -e SPRING_R2DBC_PASSWORD="..." \
  auth-service:latest
```

#### Opción 3: Docker Compose

```bash
# Iniciar todo (PostgreSQL + Auth Service)
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener
docker-compose down
```

---

## 🐛 Troubleshooting

### Error de Conexión BD

```
Caused by: io.r2dbc.postgresql.ExceptionFactory$PostgresqlNonTransientException
```

**Solución:** Verificar DATABASE_URL, credenciales y que PostgreSQL esté corriendo.

### Token Expirado

```
401 Unauthorized: Token has expired
```

**Solución:** Usar refresh token para renovar o hacer login nuevamente.

### Usuario Bloqueado

```
403 Forbidden: User account is temporarily blocked
```

**Solución:** Esperar 30 minutos o que un admin desbloquee con `/users/{id}/unblock`.

---

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abrir Pull Request

---

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver archivo `LICENSE` para más detalles.

---

## 👥 Equipo

- **Desarrollador Principal:** Henry Lunazco
- **Organización:** Valle Grande
- **Email:** hlunazco@vallegrande.edu.pe

---

## 🔗 Enlaces Útiles

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring WebFlux Guide](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [R2DBC Documentation](https://r2dbc.io/)
- [JWT Introduction](https://jwt.io/introduction)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**¿Necesitas ayuda?** Abre un issue en el repositorio o contacta al equipo de desarrollo.
