<div align="center">

<h2>🧭 Tenant Management Service</h2>

<p>
  Servicio de gestión de tenants para la plataforma. <br/>
  Despliegue simple con Docker, listo para NeonDB y entorno reactivo.
</p>

<img alt="Docker" src="https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white" />
<img alt="Spring" src="https://img.shields.io/badge/Spring%20Boot-reactive-6DB33F?logo=spring&logoColor=white" />
<img alt="NeonDB" src="https://img.shields.io/badge/NeonDB-postgres-00E599?logo=postgresql&logoColor=white" />

</div>

---

## 🐳 Despliegue con Docker

### 📦 Construir la imagen

```powershell
docker build -t tenant-management-service:latest .
```

### 🚀 Ejecutar el contenedor

Aquí está con tus credenciales de NeonDB, exponiendo el puerto `5003`, y usando tu configuración real:

```powershell
docker run -d \
  --name tenant-management-service \
  -p 5003:5003 \
  -e DB_URL="r2dbc:postgresql://neondb_owner:npg_JD4jeC7SIPom@ep-snowy-dream-ad4r62wl-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslMode=VERIFY_FULL" \
  -e DB_USERNAME="neondb_owner" \
  -e DB_PASSWORD="npg_JD4jeC7SIPom" \
  -e SERVER_PORT=5003 \
  -e SPRING_APPLICATION_NAME="tenant-management-service" \
  -e SPRING_MAIN_WEB_APPLICATION_TYPE="reactive" \
  -e MS_CONFIGURATIONSERVICE_URL="http://localhost:5004/api/v1" \
  tenant-management-service:latest
```

> 💡 Tip: En Windows PowerShell, si prefieres sin continuaciones `\`, usa versión en una sola línea:

```powershell
docker run -d --name tenant-management-service -p 5003:5003 -e DB_URL="r2dbc:postgresql://neondb_owner:npg_JD4jeC7SIPom@ep-snowy-dream-ad4r62wl-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslMode=VERIFY_FULL" -e DB_USERNAME="neondb_owner" -e DB_PASSWORD="npg_JD4jeC7SIPom" -e SERVER_PORT=5003 -e SPRING_APPLICATION_NAME="tenant-management-service" -e SPRING_MAIN_WEB_APPLICATION_TYPE="reactive" -e MS_CONFIGURATIONSERVICE_URL="http://localhost:5004/api/v1" tenant-management-service:latest
```

### 🔧 Variables de entorno

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_URL` | Cadena R2DBC para PostgreSQL (NeonDB) | `r2dbc:postgresql://...neon.tech:5432/neondb?sslMode=VERIFY_FULL` |
| `DB_USERNAME` | Usuario de la base de datos | `neondb_owner` |
| `DB_PASSWORD` | Password del usuario de DB | `npg_************` |
| `SERVER_PORT` | Puerto HTTP del servicio | `5003` |
| `SPRING_APPLICATION_NAME` | Nombre de la app Spring | `tenant-management-service` |
| `SPRING_MAIN_WEB_APPLICATION_TYPE` | Tipo de app | `reactive` |
| `MS_CONFIGURATIONSERVICE_URL` | URL del Configuration Service | `http://localhost:5004/api/v1` |

### ✅ Checklist rápido

- 🔌 DB accesible desde el contenedor (firewall/red).
- 🔐 Credenciales de NeonDB correctas y con SSL habilitado.
- 🔗 `MS_CONFIGURATIONSERVICE_URL` accesible desde Docker.
- 🧪 Probar healthcheck: `GET http://localhost:5003/actuator/health` (si está habilitado).

### 🧹 Limpieza

```powershell
docker stop tenant-management-service; docker rm tenant-management-service
```

---

> 🛡️ Seguridad: Evita commitear credenciales reales en repositorios. Usa variables de entorno o `docker run --env-file .env` cuando sea posible.

 <div align="center">
  
  <h1 style="margin-bottom: 0.2rem;">SIPREB – Technical Overview</h1>
  <p style="margin-top: 0; font-size: 1.05rem;">Service: <strong>vg-ms-tenantmanagmentservice</strong> (Configuration Service)</p>
  <p>Reactive backend with Spring Boot WebFlux + R2DBC (PostgreSQL)</p>
 
   <img alt="Java" src="https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white" />
   <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.1.5-6DB33F?logo=springboot&logoColor=white" />
   <img alt="WebFlux" src="https://img.shields.io/badge/WebFlux-reactive-0ea5e9" />
   <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-R2DBC-316192?logo=postgresql&logoColor=white" />
   <img alt="Maven Wrapper" src="https://img.shields.io/badge/Maven-wrapper-C71A36?logo=apachemaven&logoColor=white" />
  <img alt="Docker Ready" src="https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white" />

  <br/>
  <img alt="CI" src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white" />
  <img alt="Coverage" src="https://img.shields.io/badge/Coverage-—-8A2BE2" />
  <img alt="Release" src="https://img.shields.io/badge/Release-SemVer-FF4088" />
  <img alt="Conventional Commits" src="https://img.shields.io/badge/Commits-Conventional-000000?logo=git&logoColor=white" />

   <p>
     <a href="#prop%C3%B3sito">Propósito</a> ·
     <a href="#funcionalidades">Funcionalidades</a> ·
     <a href="#stack-tecnol%C3%B3gico">Stack</a> ·
     <a href="#modelo-principal-municipality">Modelo</a> ·
     <a href="#ejecuci%C3%B3n-local-maven">Ejecución</a> ·
     <a href="#docker--docker-compose">Docker</a> ·
     <a href="#endpoints-principales">Endpoints</a> ·
     <a href="#documentaci%C3%B3n-y-salud">Docs/Salud</a> ·
     <a href="#arquitectura-alto-nivel">Arquitectura</a> ·
     <a href="#buenas-pr%C3%A1cticas-y-seguridad">Seguridad</a> ·
     <a href="#ejemplos-de-uso">Ejemplos</a> ·
     <a href="#formato-de-errores-est%C3%A1ndar">Errores</a> ·
     <a href="#estructura-sugerida">Estructura</a> ·
     <a href="#roadmap">Roadmap</a>
   </p>
 </div>

<div align="center">
![Banner](https://img.shields.io/badge/-%20SIPREB%20Municipalities%20Service-0ea5e9?labelColor=111827&style=for-the-badge)
</div>

---
 
> Microservicio backend para la gestión de municipalidades (catálogo maestro): creación, consulta, actualización y eliminación, con validaciones de RUC y código de ubigeo en tiempo real. Implementado con arquitectura reactiva (Spring WebFlux + R2DBC) y PostgreSQL.

## Propósito
- **Centralizar la configuración de municipalidades** para otros servicios del ecosistema.
- **Garantizar calidad de datos** mediante validaciones de formato y unicidad de RUC (11 dígitos) y ubigeo (6 dígitos).
- **Ofrecer APIs reactivas** con alta concurrencia y baja latencia.

## Funcionalidades
- **CRUD completo** de municipalidades.
- **Validación** de RUC y ubigeo con soporte para `excludeId` al editar.
- **CORS** habilitado para `http://localhost:5173` (ajustable por configuración).
- **Observabilidad** con Actuator (salud, métricas básicas).
- **Documentación** OpenAPI/Swagger UI.

## Stack Tecnológico
- Java 17, Spring Boot 3
- Spring WebFlux (reactivo)
- Spring Data R2DBC + PostgreSQL (compatible Neon)
- Lombok
- Spring Boot Actuator
- springdoc-openapi-starter-webflux-ui

## Modelo principal: Municipality

| Campo | Tipo | Reglas/Notas |
|---|---|---|
| `id` | UUID | Identificador de la entidad |
| `name` | String | Requerido |
| `ruc` | String(11) | Requerido, 11 dígitos, único |
| `ubigeoCode` | String(6) | Requerido, 6 dígitos, único |
| `municipalityType` | String | Requerido, ej. `DISTRICT`/`PROVINCIAL` |
| `department` | String | Requerido |
| `province` | String | Requerido |
| `district` | String | Requerido |
| `address` | String | Opcional |
| `phoneNumber` | String | Máx 50 |
| `mobileNumber` | String | Máx 20 |
| `email` | String | Formato correo válido |
| `website` | String | Opcional |
| `mayorName` | String | Opcional |
| `isActive` | Boolean | Por defecto `true` |
| `createdAt` | ZonedDateTime | Generado automáticamente |
| `updatedAt` | ZonedDateTime | Actualizado automáticamente |

## Requisitos previos
- JDK 17
- Maven Wrapper (incluido)
- PostgreSQL accesible (local o Neon)
- Docker (opcional para despliegue local rápido)

## Configuración (variables de entorno)
 Define estas variables en tu entorno, en un archivo `.env` (para Docker) o en tu plataforma de despliegue:

| Variable | Descripción | Ejemplo/Default |
|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | `5001` |
| `SPRING_R2DBC_URL` | URL de conexión R2DBC PostgreSQL | `r2dbc:postgresql://<host>:5432/<db>?sslmode=VERIFY_FULL` |
| `SPRING_R2DBC_USERNAME` | Usuario BD | `<user>` |
| `SPRING_R2DBC_PASSWORD` | Password BD | `<password>` |

Nota: `src/main/resources/application.yml` incluye valores por defecto pensados para desarrollo. En producción, siempre sobrescríbelos con variables seguras.

## Quick Start

### Windows (PowerShell)
```powershell
./mvnw.cmd clean package
$env:SERVER_PORT = '5001'
$env:SPRING_R2DBC_URL = 'r2dbc:postgresql://<host>:5432/<db>?sslmode=VERIFY_FULL'
$env:SPRING_R2DBC_USERNAME = '<user>'
$env:SPRING_R2DBC_PASSWORD = '<password>'
./mvnw.cmd spring-boot:run
```

### macOS/Linux (bash)
```bash
./mvnw clean package
export SERVER_PORT=5001
export SPRING_R2DBC_URL='r2dbc:postgresql://<host>:5432/<db>?sslmode=VERIFY_FULL'
export SPRING_R2DBC_USERNAME='<user>'
export SPRING_R2DBC_PASSWORD='<password>'
./mvnw spring-boot:run
```

## Ejecución local (Maven)
```powershell
./mvnw.cmd clean package
$env:SERVER_PORT = '5001'
$env:SPRING_R2DBC_URL = 'r2dbc:postgresql://<host>:5432/<db>?sslmode=VERIFY_FULL'
$env:SPRING_R2DBC_USERNAME = '<user>'
$env:SPRING_R2DBC_PASSWORD = '<password>'
./mvnw.cmd spring-boot:run
```

## Docker / Docker Compose
1) Define `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SERVER_PORT` en tu entorno o `.env`.
2) Levanta el servicio:
```bash
docker compose up --build -d
```
- Acceso local por defecto (compose): `http://localhost:5003`

## Endpoints principales
 Base path: `/api/municipalities`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/municipalities` | Listar municipalidades |
| GET | `/api/municipalities/{id}` | Obtener por ID |
| POST | `/api/municipalities` | Crear municipalidad |
| PUT | `/api/municipalities/{id}` | Reemplazo completo |
| PATCH | `/api/municipalities/{id}` | Actualización parcial |
| DELETE | `/api/municipalities/{id}` | Eliminación lógica/física según implementación |
| GET | `/api/municipalities/validate/tax-id/{taxId}?excludeId={uuid?}` | Validar RUC (formato/unicidad) |
| GET | `/api/municipalities/validate/ubigeo-code/{ubigeoCode}?excludeId={uuid?}` | Validar ubigeo |

Respuestas: `200 OK`, `201 CREATED`, `204 NO_CONTENT`, `400 BAD_REQUEST`, `404 NOT_FOUND`.

<details>
<summary>Ejemplos de payload (expandir)</summary>

```json
{
  "name": "Municipalidad de Lima",
  "ruc": "20123456789",
  "ubigeoCode": "150101",
  "municipalityType": "PROVINCIAL",
  "department": "Lima",
  "province": "Lima",
  "district": "Lima",
  "email": "info@muni.gob.pe",
  "isActive": true
}
```
</details>

## Documentación y salud
- Swagger UI: `GET /swagger-ui.html`
- OpenAPI JSON: `GET /v3/api-docs`
- Health: `GET /actuator/health`

## Arquitectura (alto nivel)

```mermaid
flowchart LR
  A[Client / Frontend] -->|HTTP| B[Controller\nMunicipalityController]
  B --> C[Service\nMunicipalityService]
  C --> D[Repository\nR2DBC]
  D --> E[(PostgreSQL)]
```

## Buenas prácticas y seguridad
- No publiques credenciales en el repositorio. Usa variables de entorno o gestores de secretos.
- Habilita SSL/TLS hacia la base de datos (por ejemplo `sslmode=VERIFY_FULL` en Neon).
- Revisa CORS antes de exponer públicamente el servicio.

## Enlaces rápidos
- **Swagger UI**: `/swagger-ui.html`
- **OpenAPI JSON**: `/v3/api-docs`
- **Health**: `/actuator/health`
- **Base API**: `/api/municipalities`

## Ejemplos de uso
- Crear municipalidad
  ```bash
  curl -X POST http://localhost:5001/api/municipalities \
    -H "Content-Type: application/json" \
    -d '{
      "name":"Municipalidad de Lima",
      "ruc":"20123456789",
      "ubigeoCode":"150101",
      "municipalityType":"PROVINCIAL",
      "department":"Lima",
      "province":"Lima",
      "district":"Lima",
      "email":"info@muni.gob.pe",
      "isActive":true
    }'
  ```
- Validar RUC (excluyendo un id durante edición)
  ```bash
  curl "http://localhost:5001/api/municipalities/validate/tax-id/20123456789?excludeId=00000000-0000-0000-0000-000000000000"
  ```
- Obtener por id
  ```bash
  curl "http://localhost:5001/api/municipalities/{uuid}"
  ```

## Formato de errores (estándar)
```json
{
  "timestamp": "2025-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Tax ID (RUC) must be 11 digits",
  "path": "/api/municipalities"
}
```

## Estructura sugerida
```text
src/
├─ main/java/pe/edu/vallegrande/configurationservice/
│  ├─ controller/        # REST API (WebFlux)
│  ├─ dto/               # DTOs y responses de validación
│  ├─ exception/         # Manejo global de errores
│  ├─ model/             # Entidades (R2DBC)
│  ├─ repository/        # Repositorios R2DBC
│  └─ service/impl/      # Lógica de negocio
└─ main/resources/
   ├─ application.yml    # Configuración
   └─ schema.sql         # DDL (init)
```

## Roadmap
- [ ] Paginación y filtros por `department/province/district`
- [ ] Índices únicos en BD para `ruc` y `ubigeoCode`
- [ ] Tests de integración (WebTestClient + Testcontainers)
- [ ] Seguridad (JWT / API Key) y rate limiting
- [ ] Observabilidad extendida (Prometheus/Grafana)

---
 
<div align="center">
  <sub> SIPREB · Configuration Service · Hecho con ❤️ en WebFlux</sub>
</div>

## Licencia
Uso interno académico/organizacional. Ajusta según políticas del proyecto.
