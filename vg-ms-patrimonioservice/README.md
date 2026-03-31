# Patrimonio Service - Microservicio de Gestión de Activos

Microservicio para la gestión de bienes patrimoniales institucionales. Desarrollado con Spring WebFlux (programación reactiva) y Arquitectura Hexagonal.

**Stack tecnológico:**
- Java 17
- Spring Boot 3.x + WebFlux
- PostgreSQL 16 (R2DBC para acceso reactivo)
- Arquitectura Hexagonal (Puertos y Adaptadores)

## � Tabla de Contenidos

- [📋 Descripción](#-descripción)
- [🚀 Tecnologías](#-tecnologías)
- [🏗️ Arquitectura](#️-arquitectura)
  - [Arquitectura Hexagonal](#arquitectura-hexagonal-puertos-y-adaptadores)
  - [Microservicios Integrados](#microservicios-integrados)
- [📁 Estructura del Proyecto](#-estructura-del-proyecto)
  - [Principios de Arquitectura Hexagonal](#-principios-de-la-arquitectura-hexagonal-aplicados)
  - [Detalles de Componentes por Capa](#-detalles-de-componentes-por-capa)
- [🔧 Instalación y Configuración](#-instalación-y-configuración)
- [🐳 Despliegue con Docker](#-despliegue-con-docker)
- [📡 Endpoints API](#-endpoints-api)
- [📝 Ejemplos de Uso](#-ejemplos-de-uso)
- [🔐 Seguridad y Autenticación](#-seguridad-y-autenticación)
- [🔄 Flujo de Procesos de Negocio](#-flujo-de-procesos-de-negocio)
- [🛠️ Tecnologías Reactivas](#️-tecnologías-reactivas)
- [📐 Migración de Arquitectura](#-migración-de-arquitectura)
- [🧪 Testing](#-testing-con-postman)
- [📚 Documentación Adicional](#-documentación-adicional)
- [📝 Changelog](#-changelog)

## Descripción General

Este servicio maneja todo lo relacionado con el patrimonio de la institución. Básicamente permite:

- **Gestión de Activos**: CRUD completo de bienes patrimoniales (laptops, mobiliario, equipos, etc.)
- **Depreciaciones**: Cálculo automático mensual usando método lineal. Ya no hay que calcular a mano en Excel.
- **Proceso de Bajas**: Todo el flujo para dar de baja activos (desde la solicitud hasta la ejecución física). Incluye asignación de comisión evaluadora y dictámenes técnicos.
- **Trazabilidad**: Quién tiene qué, dónde está ubicado, en qué área. Todo rastreado.
- **Integración con otros servicios**: Se conecta al servicio de Configuración (para áreas, categorías, ubicaciones) y al de Autenticación (para responsables y usuarios).

## Tecnologías Usadas

- Java 17
- Spring Boot 3.x
- Spring WebFlux (programación reactiva con Mono y Flux)
- Spring Data R2DBC (acceso reactivo a PostgreSQL)
- PostgreSQL 16 (usamos Neon DB en producción)
- WebClient para consumir otros microservicios
- Lombok (para no escribir getters/setters)
- Maven

## Arquitectura

### Por qué Arquitectura Hexagonal

Migramos de MVC tradicional a Arquitectura Hexagonal (Puertos y Adaptadores) para tener mejor separación de responsabilidades. Los beneficios que hemos visto:

- **Independencia de frameworks**: La lógica de negocio (dominio) no sabe nada de Spring, R2DBC o PostgreSQL. Podríamos cambiar de BD sin tocar el core del negocio.
- **Inversión de dependencias**: El dominio define qué necesita (puertos/interfaces) y la infraestructura lo implementa (adaptadores). El dominio no depende de la infraestructura.
- **Más fácil de testear**: Podemos probar la lógica de negocio sin levantar Spring ni conectarnos a BD.
- **Flexibilidad**: Si mañana queremos cambiar de PostgreSQL a MongoDB, solo tocamos los adaptadores de persistencia.
- **Orden**: Cada capa tiene su responsabilidad clara. No más mezcla de lógica de negocio en los controladores.

```
┌─────────────────────────────────────────────────────────────────┐
│                     INFRAESTRUCTURA                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │        Adaptadores de Entrada (Input Adapters)           │   │
│  │  - REST Controllers (AssetRestController, etc.)          │   │
│  │  - Manejo de peticiones HTTP                             │   │
│  │  - Validación de entrada                                 │   │
│  │  - Serialización JSON                                    │   │
│  └────────────────────┬─────────────────────────────────────┘   │
│                       │                                          │
│         ┌─────────────▼──────────────────────────┐              │
│         │      CAPA DE APLICACIÓN                │              │
│         │  ┌──────────────────────────────────┐  │              │
│         │  │  Puertos de Entrada (Input)      │  │              │
│         │  │  - AssetUseCase                  │  │              │
│         │  │  - DepreciationUseCase           │  │              │
│         │  │  - AssetDisposalUseCase          │  │              │
│         │  └──────────────────────────────────┘  │              │
│         │  ┌──────────────────────────────────┐  │              │
│         │  │  Servicios de Aplicación         │  │              │
│         │  │  - AssetService                  │  │              │
│         │  │  - DepreciationService           │  │              │
│         │  │  - AssetDisposalService          │  │              │
│         │  │  (Implementan casos de uso)      │  │              │
│         │  └──────────────────────────────────┘  │              │
│         │  ┌──────────────────────────────────┐  │              │
│         │  │  Puertos de Salida (Output)      │  │              │
│         │  │  - AssetPersistencePort          │  │              │
│         │  │  - AreasPort, CategoriesPort     │  │              │
│         │  │  - ResponsiblePort, etc.         │  │              │
│         │  └──────────────────────────────────┘  │              │
│         └──────────────┬────────────┬────────────┘              │
│                        │            │                            │
│  ┌─────────────────────▼────────────▼──────────────────────┐   │
│  │              CAPA DE DOMINIO (Núcleo)                    │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  Entidades de Dominio                              │  │   │
│  │  │  - Asset, Depreciation                             │  │   │
│  │  │  - AssetDisposal, AssetDisposalDetail              │  │   │
│  │  │  (Lógica de negocio pura, sin dependencias)        │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  Excepciones de Dominio                            │  │   │
│  │  │  - AssetNotFoundException                          │  │   │
│  │  │  - AssetDisposalNotFoundException                  │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                        ▲            ▲                            │
│  ┌─────────────────────┴────────────┴──────────────────────┐   │
│  │        Adaptadores de Salida (Output Adapters)          │   │
│  │  ┌────────────────────┐  ┌────────────────────────────┐ │   │
│  │  │  Persistence       │  │  External Services         │ │   │
│  │  │  - R2DBC Repos     │  │  - WebClient Adapters      │ │   │
│  │  │  - Asset           │  │  - Areas, Categories       │ │   │
│  │  │  - Depreciation    │  │  - Locations, Suppliers    │ │   │
│  │  │  - AssetDisposal   │  │  - Responsible (Auth)      │ │   │
│  │  └────────────────────┘  └────────────────────────────┘ │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────┬─────────────────────────────────────┬───────────────┘
             │                                     │
             ▼                                     ▼
    ┌─────────────────┐                  ┌──────────────────────┐
    │   PostgreSQL    │                  │  Microservicios      │
    │   (Neon DB)     │                  │  - Config (5004)     │
    └─────────────────┘                  │  - Auth (5002)       │
                                         └──────────────────────┘
```

### Servicios Externos que Consumimos

Este servicio no trabaja solo, se integra con:

- **Configuration Service** (puerto 5004) - De aquí sacamos:
  - Áreas, Categorías de activos, Ubicaciones físicas, Proveedores
- **Authentication Service** (puerto 5002) - De aquí obtenemos:
  - Datos de personas/responsables, validación de tokens JWT

Todo se consume de forma reactiva usando WebClient. Importante: propagamos el token JWT del usuario original a estos servicios.

## 📁 Estructura del Proyecto

El proyecto sigue la **Arquitectura Hexagonal**, organizado en tres capas principales:

```
vg-ms-patrimonioservice/
├── src/
│   ├── main/
│   │   ├── java/pe/edu/vallegrande/patrimonio_service/
│   │   │   │
│   │   │   ├── 📦 domain/                           # CAPA DE DOMINIO (Núcleo)
│   │   │   │   ├── model/                           # Entidades de dominio
│   │   │   │   │   ├── Asset.java                   # Entidad Activo
│   │   │   │   │   ├── Depreciation.java            # Entidad Depreciación
│   │   │   │   │   ├── AssetDisposal.java           # Entidad Solicitud de Baja
│   │   │   │   │   └── AssetDisposalDetail.java     # Entidad Detalle de Baja
│   │   │   │   │
│   │   │   │   └── exception/                       # Excepciones de dominio
│   │   │   │       ├── AssetNotFoundException.java
│   │   │   │       ├── AssetDisposalNotFoundException.java
│   │   │   │       └── AssetDisposalDetailNotFoundException.java
│   │   │   │
│   │   │   ├── 📦 application/                      # CAPA DE APLICACIÓN
│   │   │   │   ├── dto/                             # Data Transfer Objects
│   │   │   │   │   ├── AssetRequest.java / AssetResponse.java
│   │   │   │   │   ├── DepreciationRequest.java / DepreciationResponse.java
│   │   │   │   │   ├── AssetDisposalRequest.java / AssetDisposalResponse.java
│   │   │   │   │   ├── AssetDisposalDetailRequest.java / Response.java
│   │   │   │   │   ├── TechnicalOpinionRequest.java
│   │   │   │   │   ├── AssignCommitteeRequest.java
│   │   │   │   │   ├── ResolveDisposalRequest.java
│   │   │   │   │   └── ExecuteRemovalRequest.java
│   │   │   │   │
│   │   │   │   ├── ports/                           # Interfaces de puertos
│   │   │   │   │   ├── input/                       # Puertos de entrada (casos de uso)
│   │   │   │   │   │   ├── AssetUseCase.java
│   │   │   │   │   │   ├── AssetServicePort.java
│   │   │   │   │   │   ├── DepreciationUseCase.java
│   │   │   │   │   │   ├── DepreciationServicePort.java
│   │   │   │   │   │   ├── AssetDisposalUseCase.java
│   │   │   │   │   │   ├── AssetDisposalServicePort.java
│   │   │   │   │   │   ├── AssetDisposalDetailUseCase.java
│   │   │   │   │   │   └── AssetDisposalDetailServicePort.java
│   │   │   │   │   │
│   │   │   │   │   └── output/                      # Puertos de salida (interfaces)
│   │   │   │   │       ├── AssetPersistencePort.java
│   │   │   │   │       ├── DepreciationPersistencePort.java
│   │   │   │   │       ├── AssetDisposalPersistencePort.java
│   │   │   │   │       ├── AssetDisposalDetailPersistencePort.java
│   │   │   │   │       ├── AreasPort.java
│   │   │   │   │       ├── CategoriesPort.java
│   │   │   │   │       ├── LocationsPort.java
│   │   │   │   │       ├── ResponsiblePort.java
│   │   │   │   │       └── SuppliersPort.java
│   │   │   │   │
│   │   │   │   └── service/                         # Implementación de casos de uso
│   │   │   │       ├── AssetService.java
│   │   │   │       ├── DepreciationService.java
│   │   │   │       ├── AssetDisposalService.java
│   │   │   │       └── AssetDisposalDetailService.java
│   │   │   │
│   │   │   └── 📦 infrastructure/                   # CAPA DE INFRAESTRUCTURA
│   │   │       ├── adapters/
│   │   │       │   ├── input/                       # Adaptadores de entrada
│   │   │       │   │   └── rest/                    # Controladores REST
│   │   │       │   │       ├── AssetRestController.java
│   │   │       │   │       ├── DepreciationRestController.java
│   │   │       │   │       ├── AssetDisposalRestController.java
│   │   │       │   │       ├── AssetDisposalDetailRestController.java
│   │   │       │   │       └── dto/                 # DTOs externos (áreas, categorías, etc.)
│   │   │       │   │           ├── AreasDTO.java
│   │   │       │   │           ├── CategoriesDTO.java
│   │   │       │   │           ├── PhysicalLocationsDTO.java
│   │   │       │   │           ├── ResponsibleDTO.java
│   │   │       │   │           └── SuppliersDTO.java
│   │   │       │   │
│   │   │       │   └── output/                      # Adaptadores de salida
│   │   │       │       ├── persistence/             # Adaptadores de persistencia
│   │   │       │       │   ├── AssetPersistenceAdapter.java
│   │   │       │       │   ├── DepreciationPersistenceAdapter.java
│   │   │       │       │   ├── AssetDisposalPersistenceAdapter.java
│   │   │       │       │   ├── AssetDisposalDetailPersistenceAdapter.java
│   │   │       │       │   └── repository/          # Repositorios R2DBC
│   │   │       │       │       ├── AssetRepository.java
│   │   │       │       │       ├── DepreciationRepository.java
│   │   │       │       │       ├── AssetDisposalRepository.java
│   │   │       │       │       └── AssetDisposalDetailRepository.java
│   │   │       │       │
│   │   │       │       └── client/                  # Adaptadores de clientes externos
│   │   │       │           ├── AreasClientAdapter.java
│   │   │       │           ├── CategoriesClientAdapter.java
│   │   │       │           ├── LocationsClientAdapter.java
│   │   │       │           ├── ResponsibleClientAdapter.java
│   │   │       │           └── SuppliersClientAdapter.java
│   │   │       │
│   │   │       └── config/                          # Configuración de infraestructura
│   │   │           ├── CorsConfig.java
│   │   │           ├── DatabaseConfig.java
│   │   │           ├── WebClientConfig.java
│   │   │           └── AuthTokenFilter.java         # Interceptor de tokens JWT
│   │   │
│   │   └── resources/
│   │       ├── application.yml                      # Configuración del servicio
│   │       └── db/
│   │           └── schema.sql                       # Esquema DDL de la base de datos
│   │
│   └── test/
│       └── java/pe/edu/vallegrande/patrimonio_service/
│           └── PatrimonioServiceApplicationTests.java
│
├── postman_collection.json                          # Colección Postman (42 endpoints)
├── docker-compose-keycloak.yml
├── Dockerfile
├── docker-build.sh
├── pom.xml                                          # Configuración Maven
└── README.md
```

### Principios que Aplicamos

Cuando estructuramos el proyecto, seguimos estos principios:

1. **Dominio Puro**: Las entidades en `domain/model/` no tienen ni una sola anotación de Spring. Son POJOs puros con lógica de negocio.
2. **Puertos (Interfaces)**: En `application/ports/` definimos contratos. El dominio dice "necesito guardar un Asset" pero no le importa cómo (PostgreSQL, MongoDB, en memoria, etc.).
3. **Adaptadores de Entrada**: Los controladores REST en `infrastructure/adapters/input/rest/` son la puerta de entrada HTTP. Traducen requests HTTP a comandos de la aplicación.
4. **Adaptadores de Salida**: 
   - `infrastructure/adapters/output/persistence/` - Implementan cómo guardamos datos (R2DBC en nuestro caso)
   - `infrastructure/adapters/output/client/` - Implementan cómo consumimos APIs externas (WebClient)
5. **Inversión de Dependencias**: La regla de oro - la infraestructura depende del dominio, NUNCA al revés. El dominio no sabe que existe Spring.
6. **Servicios de Aplicación**: Orquestan los casos de uso. Coordinan entre puertos pero no tienen lógica de negocio compleja.

### Detalles de Cada Capa

#### Capa de Dominio (`domain/`)
**Qué hace**: La lógica de negocio pura. Aquí vive el corazón de la aplicación.

- **model/** - Entidades de dominio
  - `Asset.java`: Representa un bien patrimonial con sus reglas de negocio
  - `Depreciation.java`: Cálculos de depreciación
  - `AssetDisposal.java`: Proceso de baja
  - `AssetDisposalDetail.java`: Detalle de cada activo en una baja

- **exception/** - Excepciones específicas del negocio
  - `AssetNotFoundException`, `AssetDisposalNotFoundException`, etc.
  - Nota: Estas no extienden de excepciones de Spring. Son completamente independientes.

#### Capa de Aplicación (`application/`)
**Qué hace**: Orquesta los casos de uso. Es el "qué" hace el sistema, no el "cómo".

- **ports/input/** - Define qué puede hacer el sistema (contratos)
  - `AssetUseCase.java`: "El sistema puede crear, actualizar, eliminar activos"
  - `DepreciationUseCase.java`: "El sistema puede calcular depreciaciones"
  - `AssetDisposalUseCase.java`: "El sistema puede gestionar bajas"
  - Son interfaces. No implementación aquí.

- **ports/output/** - Define qué necesita el sistema del exterior
  - `AssetPersistencePort.java`: "Necesito guardar/recuperar activos" (pero no dice cómo)
  - `AreasPort.java`, `CategoriesPort.java`: "Necesito consultar áreas y categorías"
  - También son interfaces.

- **service/** - Implementación de los casos de uso
  - `AssetService.java`, `DepreciationService.java`, etc.
  - Aquí sí hay código. Orquestan llamadas a puertos.
  - Usan `Flux<T>` y `Mono<T>` para trabajar de forma reactiva.

- **dto/** - Objetos para transferir datos
  - Request/Response para cada operación
  - Validaciones de entrada
  - Sirven de "traductor" entre el mundo HTTP y el dominio

#### Capa de Infraestructura (`infrastructure/`)
**Qué hace**: Los detalles técnicos. Spring, bases de datos, HTTP, todo lo "sucio".

- **adapters/input/rest/** - Controladores REST
  - Exponen endpoints HTTP
  - Anotan con `@RestController`, `@RequestMapping`, etc.
  - Validan tokens JWT
  - Convierten JSON a DTOs y viceversa

- **adapters/output/persistence/** - Acceso a base de datos
  - Implementan los `*PersistencePort` usando R2DBC
  - Repositorios Spring Data R2DBC
  - Mapean entre entidades de dominio y entidades de BD
  - Todo reactivo con `Flux` y `Mono`

- **adapters/output/client/** - Consumo de servicios externos
  - Implementan `*Port` para servicios externos (áreas, categorías, responsables, etc.)
  - Usan WebClient
  - Propagan el token JWT del usuario
  - Manejan errores de red, timeouts, etc.

- **config/** - Configuración de Spring
  - Beans y seguridad
  - WebClient configuration
  - Filtros (como el `AuthTokenFilter` que captura el token JWT)

## Instalación y Configuración

### Lo que necesitas tener instalado

- Java 17 (mínimo)
- Maven 3.8+
- PostgreSQL 12+ (o una cuenta en Neon DB que es lo que usamos en producción)
- Docker (si quieres correrlo en contenedor)
- Los otros microservicios corriendo:
  - Configuration Service en puerto 5004
  - Authentication Service en puerto 5002

### Variables de Entorno

Puedes configurar estas variables o editar directamente el `application.yml`:

```bash
# Base de datos
export DB_URL="r2dbc:postgresql://usuario:password@host:5432/database?sslMode=VERIFY_FULL"
export DB_USERNAME="neondb_owner"
export DB_PASSWORD="tu_password"

# Puerto del servicio
export SERVER_PORT=5003
```

### Pasos para levantar el servicio

1. **Clonar el repo**:
```bash
git clone <repository-url>
cd vg-ms-patrimonioservice
```

2. **Configurar `application.yml`** con tus datos:
```yaml
server:
  port: 5003

spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/patrimonio_db
    username: tu_usuario
    password: tu_password

services:
  ms-configurationService:
    url: http://localhost:5004/api
  ms-autenticacionService:
    url: http://localhost:5002/api/v1
```

3. **Crear las tablas en la BD**:
```bash
psql -U tu_usuario -d patrimonio_db -f src/main/resources/db/schema.sql
```

4. **Compilar**:
```bash
./mvnw clean package
```

5. **Ejecutar**:
```bash
./mvnw spring-boot:run
```

El servicio quedará corriendo en `http://localhost:5003`

### Nota sobre la Arquitectura al Desarrollar

Cuando agregues features nuevos, recuerda:
- Los controladores REST van en `infrastructure/adapters/input/rest/`
- Los servicios que implementan casos de uso van en `application/service/`
- Los adaptadores de BD van en `infrastructure/adapters/output/persistence/`
- Los adaptadores de clientes externos van en `infrastructure/adapters/output/client/`
- Las entidades del dominio van en `domain/model/` y deben ser independientes de Spring

### 🐳 Despliegue con Docker

#### Construir la imagen

```bash
docker build -t patrimonio-service:latest .
```

#### Ejecutar el contenedor

```bash
docker run -d \
  --name patrimonio-service \
  -p 5003:5003 \
  -e DB_URL="r2dbc:postgresql://host:5432/db" \
  -e DB_USERNAME="user" \
  -e DB_PASSWORD="password" \
  -e SERVER_PORT=5003 \
  patrimonio-service:latest
```

#### Usar imagen Docker de Configuration Service

Para integrar con el Configuration Service, asegúrate de levantar su contenedor:

```bash
docker pull angie14/configurationservice:latest

docker run -d \
  --name configuration-service \
  -p 5004:5004 \
  angie14/configurationservice:latest
```

Luego configura la URL en `application.yml`:
```yaml
services:
  ms-configurationService:
    url: http://configuration-service:5004/api  # Si usas Docker network
    # O http://localhost:5004/api si corres localmente
```

#### Docker Compose (recomendado)

Crea un `docker-compose.yml` para levantar todos los servicios juntos:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: patrimonio_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  configuration-service:
    image: williams31/vg-ms-patrimonioservice:latest

    ports:
      - "5004:5004"
    depends_on:
      - postgres

  authentication-service:
    image: your-auth-service-image:latest  # Ajustar según tu imagen
    ports:
      - "5002:5002"
    depends_on:
      - postgres

  patrimonio-service:
    build: .
    ports:
      - "5003:5003"
    environment:
      DB_URL: "r2dbc:postgresql://postgres:5432/patrimonio_db"
      DB_USERNAME: "postgres"
      DB_PASSWORD: "postgres"
      SERVER_PORT: 5003
    depends_on:
      - postgres
      - configuration-service
      - authentication-service

volumes:
  postgres_data:
```

Ejecutar:
```bash
docker-compose up -d
```

## 📡 Endpoints API

Base URL: `http://localhost:5003/api/v1`

> **Nota**: Todos los endpoints requieren el header `Authorization: Bearer <token>` (excepto los de integración con servicios externos). Los controladores REST están ubicados en la capa de infraestructura (`infrastructure/adapters/input/rest/`).

### 📦 Assets (Bienes Patrimoniales)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/assets` | Crear nuevo activo |
| `GET` | `/api/v1/assets` | Listar todos los activos |
| `GET` | `/api/v1/assets/{id}` | Obtener activo por ID |
| `PUT` | `/api/v1/assets/{id}` | Actualizar activo |
| `DELETE` | `/api/v1/assets/{id}` | Eliminar activo |
| `PATCH` | `/api/v1/assets/{id}/status` | Cambiar estado del activo |
| `GET` | `/api/v1/assets/status/{status}` | Filtrar por estado (ACTIVO, INACTIVO, etc.) |
| `GET` | `/api/v1/assets/code/{assetCode}` | Buscar por código patrimonial |

### 📉 Depreciations (Depreciaciones)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/depreciations` | Crear/calcular depreciación |
| `GET` | `/api/v1/depreciations` | Listar todas las depreciaciones |
| `GET` | `/api/v1/depreciations/{assetId}` | Depreciaciones de un activo |
| `GET` | `/api/v1/depreciations/asset/{assetId}` | Depreciaciones por activo (alternativo) |
| `GET` | `/api/v1/depreciations/year/{fiscalYear}` | Depreciaciones por año fiscal |
| `GET` | `/api/v1/depreciations/asset/{assetId}/year/{fiscalYear}/month/{calculationMonth}` | Depreciación específica por período |
| `DELETE` | `/api/v1/depreciations/{id}` | Eliminar depreciación |
| `PATCH` | `/api/v1/depreciations/{id}/approve?approvedBy={userId}` | Aprobar cálculo |
| `GET` | `/api/v1/depreciations/auto/{assetId}?initialValue=&residualValue=&usefulLifeMonths=&acquisitionDate=` | Generar depreciaciones automáticas |

### 🗑️ Asset Disposals (Proceso de Bajas)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/asset-disposals` | Crear solicitud de baja |
| `GET` | `/api/v1/asset-disposals` | Listar todas las bajas |
| `GET` | `/api/v1/asset-disposals/{id}` | Obtener baja por ID |
| `GET` | `/api/v1/asset-disposals/status/{status}` | Filtrar por estado |
| `GET` | `/api/v1/asset-disposals/file-number/{fileNumber}` | Buscar por número de expediente |
| `GET` | `/api/v1/asset-disposals/requested-by/{userId}` | Bajas solicitadas por usuario |
| `PUT` | `/api/v1/asset-disposals/{id}/assign-committee` | Asignar comisión evaluadora |
| `PUT` | `/api/v1/asset-disposals/{id}/resolve` | Emitir resolución |
| `PUT` | `/api/v1/asset-disposals/{id}/cancel?cancelledBy={userId}` | Cancelar proceso |
| `PUT` | `/api/v1/asset-disposals/{id}/finalize` | Finalizar proceso |
| `DELETE` | `/api/v1/asset-disposals/{id}` | Eliminar solicitud |

### 📋 Asset Disposal Details (Detalles de Bajas)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/asset-disposal-details` | Crear detalle de baja |
| `GET` | `/api/v1/asset-disposal-details/{id}` | Obtener detalle por ID |
| `GET` | `/api/v1/asset-disposal-details/disposal/{disposalId}` | Detalles de una solicitud |
| `GET` | `/api/v1/asset-disposal-details/asset/{assetId}` | Detalles por activo |
| `PUT` | `/api/v1/asset-disposal-details/{id}/technical-opinion` | Añadir dictamen técnico |
| `PATCH` | `/api/v1/asset-disposal-details/{id}/execute-removal` | Ejecutar baja física |
| `DELETE` | `/api/v1/asset-disposal-details/{id}` | Eliminar detalle |

### 🔗 Integración con Servicios Externos (Proxy)

Estos endpoints consumen datos del **Configuration Service** y **Authentication Service**:

| Método | Endpoint | Descripción | Origen |
|--------|----------|-------------|--------|
| `GET` | `/api/v1/areas` | Listar áreas | Config Service |
| `GET` | `/api/v1/areas/{id}` | Obtener área | Config Service |
| `GET` | `/api/v1/categories` | Listar categorías | Config Service |
| `GET` | `/api/v1/categories/{id}` | Obtener categoría | Config Service |
| `GET` | `/api/v1/locations` | Listar ubicaciones físicas | Config Service |
| `GET` | `/api/v1/locations/{id}` | Obtener ubicación | Config Service |
| `GET` | `/api/v1/suppliers` | Listar proveedores | Config Service |
| `GET` | `/api/v1/suppliers/{id}` | Obtener proveedor | Config Service |
| `GET` | `/api/v1/responsible` | Listar responsables/personas | Auth Service (con propagación de token) |
| `GET` | `/api/v1/responsible/{id}` | Obtener responsable | Auth Service (con propagación de token) |

## Funcionalidades Detalladas

### Módulo de Activos (Assets)

Qué hace este módulo: maneja todo el ciclo de vida de los bienes patrimoniales.

**Lo que puedes hacer**:
- Registrar activos nuevos con toda su información (código, categoría, ubicación, responsable, proveedor, etc.)
- Actualizar datos de activos existentes
- Cambiar el estado de un activo (ACTIVO, INACTIVO, EN_MANTENIMIENTO, EN_BAJA, DADO_DE_BAJA)
- Llevar trazabilidad de quién modificó qué y cuándo
- Buscar activos por código, estado, categoría, ubicación
- Validar que las categorías, ubicaciones, responsables y proveedores existan (consulta a otros servicios)

**Reglas de negocio que aplicamos**:
1. El código de activo (`assetCode`) tiene que ser único por municipalidad. No se permiten duplicados.
2. El valor de adquisición debe ser mayor a cero (obvio, pero hay que validarlo)
3. La vida útil tiene que ser mayor a 0 meses
4. El valor residual debe ser menor al valor de adquisición
5. Al crear un activo, el `currentValue` arranca igual al `acquisitionValue`
6. No puedes eliminar un activo que ya tiene depreciaciones o que está en proceso de baja (protección de integridad)

### Módulo de Depreciaciones (Depreciations)

Qué hace: calcula y registra la depreciación mensual de los activos.

**Lo que puedes hacer**:
- Crear manualmente una depreciación para un mes específico
- Generar automáticamente todas las depreciaciones mensuales desde la fecha de adquisición hasta ahora
- Consultar depreciaciones por año fiscal, mes, o activo específico
- Aprobar cálculos de depreciación
- Ver el historial completo de depreciación acumulada y valor en libros

**Reglas de negocio**:
1. **Método**: usamos depreciación lineal (distribución uniforme a lo largo de la vida útil)
2. **Fórmula**: `Depreciación mensual = (Valor inicial - Valor residual) / Vida útil (meses)`
3. **Valor en libros**: `Valor inicial - Depreciación acumulada`
4. No se puede depreciar un activo más allá de su valor residual. Si ya llegó, se detiene.
5. No permitimos depreciaciones duplicadas para el mismo activo en el mismo período (año/mes)
6. Las depreciaciones arrancan desde la fecha de adquisición, no antes

**Estados posibles**:
- `CALCULADO`: Ya se registró pero no está aprobada
- `APROBADO`: Validada y aprobada por quien corresponda
- `ANULADO`: Se canceló

### Módulo de Bajas de Activos (Asset Disposal)

Qué hace: gestiona todo el proceso formal para dar de baja activos.

**El flujo completo**:
1. Alguien solicita la baja de uno o varios activos
2. Se asigna una comisión evaluadora
3. La comisión emite dictámenes técnicos para cada activo
4. Se emite una resolución administrativa (aprobada o rechazada)
5. Si se aprueba, se ejecuta la baja física
6. Se finaliza el proceso

**Estados del proceso**:
```
PENDIENTE → EN_COMISION → EVALUADO → RESUELTO → EJECUTADO → FINALIZADO
    ↓
CANCELADO (se puede cancelar en cualquier momento antes de RESUELTO)
```

**Reglas de negocio**:
1. Solo activos en estado `ACTIVO` o `INACTIVO` pueden incluirse en una baja
2. La comisión debe tener al menos 1 miembro asignado
3. Cada activo en la baja necesita su propio dictamen técnico
4. No puedes ejecutar la baja sin tener la resolución aprobada
5. Al ejecutar la baja, el activo automáticamente cambia a `DADO_DE_BAJA`
6. Una vez finalizado, ya no se puede modificar nada

**Tipos de baja**:
- `BAJA`: Eliminación por obsolescencia o mal estado
- `DONACION`: Se dona a otra institución
- `VENTA`: Se vende y se recupera algo de valor
- `DESTRUCCION`: Destrucción física (no se puede recuperar)

### Módulo de Detalles de Baja (Asset Disposal Details)

Qué hace: maneja individualmente cada activo dentro de una solicitud de baja.

**Lo que registramos**:
- Qué activos están incluidos en cada solicitud de baja
- Dictamen técnico de cada uno (estado, recomendación, valor recuperable)
- Fecha y responsable de la ejecución física de la baja
- Observaciones específicas por activo

**Campos del dictamen técnico**:
- Estado de conservación actual
- Recomendación (procede o no procede la baja)
- Valor recuperable estimado
- Observaciones
- Quién hizo el dictamen

## Documentación de DTOs

Aquí están todos los DTOs que usamos. Los campos con "REQUERIDO" son obligatorios, el resto son opcionales.

### AssetRequest (Crear/Actualizar Activo)

Este DTO se usa cuando creates o actualizas un activo. Tiene muchísimos campos porque un activo patrimonial tiene mucha info.

```json
{
  // IDENTIFICACIÓN
  "municipalityId": "uuid",          // Opcional: ID de municipalidad
  "assetCode": "string",             // REQUERIDO: Código único (ej: "ASSET-2024-001")
  "internalCode": "string",          // Opcional: Código interno si lo manejan
  "sbnCode": "string",               // Opcional: Código SBN (Superintendencia)
  "description": "string",           // REQUERIDO: Descripción del bien
  
  // CLASIFICACIÓN
  "categoryId": "uuid",              // REQUERIDO: Categoría (ej: Equipos de Cómputo)
  "subcategoryId": "uuid",           // Opcional: Subcategoría si aplica
  
  // CARACTERÍSTICAS FÍSICAS
  "brand": "string",                 // Opcional: Marca (Dell, HP, etc.)
  "model": "string",                 // Opcional: Modelo
  "serialNumber": "string",          // Opcional: Número de serie
  "assetPlate": "string",            // Opcional: Placa patrimonial física
  "color": "string",                 // Opcional
  "dimensions": "string",            // Opcional: Dimensiones (ej: "50x30x20 cm")
  "weight": "decimal",               // Opcional: Peso en kg
  "material": "string",              // Opcional: Material (madera, metal, etc.)
  
  // IDENTIFICADORES TECNOLÓGICOS (para control de inventario)
  "qrCode": "string",                // Opcional: Código QR
  "barcode": "string",               // Opcional: Código de barras
  "rfidTag": "string",               // Opcional: Tag RFID
  
  // ADQUISICIÓN
  "supplierId": "uuid",              // Opcional: De quién se compró
  "acquisitionDate": "2024-01-15",   // REQUERIDO: Cuándo se adquirió
  "acquisitionType": "string",       // Opcional: COMPRA, DONACION, FABRICACION
  "invoiceNumber": "string",         // Opcional: Número de factura
  "purchaseOrderNumber": "string",   // Opcional: Número de orden de compra
  "pecosaNumber": "string",          // Opcional: Número PECOSA
  
  // VALORIZACIÓN
  "acquisitionValue": "decimal",     // REQUERIDO: Cuánto costó (debe ser > 0)
  "currency": "string",              // Opcional: Moneda (PEN, USD)
  "currentValue": "decimal",         // Opcional: Valor actual (si no se pasa, usa acquisitionValue)
  "residualValue": "decimal",        // Opcional: Valor residual estimado (debe ser < acquisitionValue)
  "accumulatedDepreciation": "decimal", // Opcional: Depreciación acumulada
  
  // VIDA ÚTIL
  "usefulLife": "integer",           // REQUERIDO: Vida útil en meses (debe ser > 0)
  "nextDepreciationDate": "2024-02-01", // Opcional: Próxima fecha de depreciación
  "warrantyExpirationDate": "2025-01-15", // Opcional: Hasta cuándo tiene garantía
  
  // UBICACIÓN Y RESPONSABILIDAD
  "currentLocationId": "uuid",       // REQUERIDO: Dónde está físicamente
  "currentResponsibleId": "uuid",    // REQUERIDO: Quién es responsable
  "currentAreaId": "uuid",           // REQUERIDO: A qué área pertenece
  "entryDate": "2024-01-15T10:00:00", // Opcional: Cuándo ingresó
  
  // ESTADO Y CONTROL
  "assetStatus": "string",           // Opcional: ACTIVO, INACTIVO, EN_MANTENIMIENTO, etc.
  "conservationStatus": "string",    // Opcional: BUENO, REGULAR, MALO
  "lastInventoryDate": "2024-01-01", // Opcional: Último inventario
  "observations": "string"           // Opcional: Cualquier observación adicional
}
```

**Validaciones importantes**:
- `assetCode`: Tiene que ser único, máximo 50 caracteres
- `acquisitionValue`: Debe ser mayor a 0
- `residualValue`: Tiene que ser menor que `acquisitionValue`
- `usefulLife`: Mayor a 0
- Los UUIDs (categoryId, locationId, etc.) deben existir en los servicios correspondientes

### DepreciationRequest

Para crear una depreciación manualmente:

```json
{
  "assetId": "uuid",                 // REQUERIDO: De qué activo
  "fiscalYear": 2024,                // REQUERIDO: Año fiscal
  "calculationMonth": 11,            // REQUERIDO: Mes (1-12)
  "initialValue": "decimal",         // REQUERIDO: Valor base para el cálculo
  "usefulLifeYears": "integer",      // Opcional: Vida útil en años
  "residualValue": "decimal",        // Opcional: Valor residual
  "depreciationMethod": "string",    // Opcional: Por defecto "LINEAL"
  "observations": "string"           // Opcional
}
```

**Validaciones**:
- `assetId`: Tiene que existir en la BD
- `fiscalYear`: Debe ser >= año de adquisición del activo
- `calculationMonth`: Entre 1 y 12
- No puede haber depreciaciones duplicadas (mismo activo/año/mes)

### AssetDisposalRequest

Para iniciar un proceso de baja:

```json
{
  "municipalityId": "uuid",           // Opcional
  "disposalType": "string",           // REQUERIDO: BAJA, DONACION, VENTA, DESTRUCCION
  "disposalReason": "string",         // REQUERIDO: OBSOLESCENCIA, DETERIORO, etc.
  "reasonDescription": "string",      // REQUERIDO: Justificación detallada
  "technicalReportAuthorId": "uuid",  // Opcional: Quién elabora el informe
  "observations": "string",           // Opcional
  "requiresDestruction": true,        // Opcional: ¿Hay que destruirlo?
  "allowsDonation": false,            // Opcional: ¿Se puede donar?
  "recoverableValue": "decimal",      // Opcional: Valor que se puede recuperar
  "requestedBy": "uuid"               // REQUERIDO: Quién solicita
}
```

### AssignCommitteeRequest

Para asignar la comisión evaluadora:

```json
{
  "committeeMembers": ["uuid1", "uuid2", "uuid3"], // REQUERIDO: Mínimo 1 miembro
  "assignedBy": "uuid",                            // REQUERIDO: Quién asigna
  "assignedDate": "2024-11-05T10:00:00"           // REQUERIDO: Cuándo se asignó
}
```

### TechnicalOpinionRequest

Para el dictamen técnico:

```json
{
  "technicalOpinion": "string",       // REQUERIDO: El dictamen detallado
  "opinionStatus": "string",          // REQUERIDO: PROCEDE o NO_PROCEDE
  "estimatedValue": "decimal",        // Opcional: Valor estimado
  "evaluatedBy": "uuid",              // REQUERIDO: Quién evaluó
  "evaluationDate": "2024-11-10T14:30:00" // REQUERIDO
}
```

### ResolveDisposalRequest

Para emitir la resolución administrativa:

```json
{
  "resolutionNumber": "string",       // REQUERIDO: Número de resolución
  "resolutionDate": "2024-11-15",     // REQUERIDO
  "approved": true,                   // REQUERIDO: Aprobado o rechazado
  "resolvedBy": "uuid",               // REQUERIDO: Quién resuelve
  "resolutionObservations": "string"  // Opcional
}
```

### ExecuteRemovalRequest

Para ejecutar la baja física:

```json
{
  "removalDate": "2024-11-20",        // REQUERIDO: Cuándo se ejecutó la baja
  "executedBy": "uuid",               // REQUERIDO: Quién la ejecutó
  "executionObservations": "string",  // Opcional
  "finalDestination": "string"        // Opcional: Qué pasó con el bien
}
```

## 🔄 Diagramas de Secuencia

### Caso de Uso: Crear un Activo

```
Cliente → REST Controller → Application Service → Domain → Persistence Adapter → DB
                ↓                    ↓                           ↓
         Validation          Client Adapters            R2DBC Repository
                           (Categories, Locations, 
                            Responsible, Suppliers)
```

**Flujo detallado**:
```
1. Cliente envía POST /api/v1/assets con AssetRequest
2. AssetRestController (Input Adapter) recibe la petición
3. Controller valida el JWT token (AuthTokenFilter)
4. Controller convierte Request → Domain
5. AssetService (Application) orquesta el caso de uso:
   a. Valida datos del request
   b. Consulta servicios externos (categorías, ubicaciones, etc.) vía Client Adapters
   c. Crea entidad Asset (Domain)
   d. Establece valores por defecto
6. AssetPersistenceAdapter implementa el guardado:
   a. Convierte Domain Entity → DB Entity
   b. Ejecuta AssetRepository.save() (R2DBC)
7. Se retorna Mono<Asset> que fluye de vuelta:
   DB → Adapter → Service → Controller → Cliente
8. Controller convierte Domain → AssetResponse
9. Cliente recibe HTTP 201 Created con el activo creado
```

### Caso de Uso: Proceso Completo de Baja

```
┌─────────┐   ┌──────────┐   ┌────────────┐   ┌──────────┐   ┌─────────┐
│Solicitante│  │ Comisión │  │ Evaluador  │  │Autorizador│  │Ejecutor│
└────┬────┘   └────┬─────┘   └─────┬──────┘   └────┬─────┘   └────┬────┘
     │             │               │               │              │
     │ 1. POST /asset-disposals   │               │              │
     ├──────────────────────────────────────────────────────────>│
     │             │               │               │              │
     │ 2. POST /asset-disposal-details (añade activos)           │
     ├──────────────────────────────────────────────────────────>│
     │             │               │               │              │
     │             │ 3. PUT /assign-committee      │              │
     │             ├───────────────────────────────────────────>│
     │             │               │               │              │
     │             │ 4. PUT /technical-opinion     │              │
     │             │               ├───────────────────────────>│
     │             │               │               │              │
     │             │               │ 5. PUT /resolve (aprueba)   │
     │             │               │               ├──────────────>│
     │             │               │               │              │
     │             │               │               │ 6. PATCH /execute-removal
     │             │               │               │              ├──────>│
     │             │               │               │              │       │
     │             │               │               │              │ 7. Actualiza
     │             │               │               │              │    Asset a
     │             │               │               │              │ DADO_DE_BAJA
     │             │               │               │              │<──────┘
     │             │               │               │              │
     │             │               │               │ 8. PUT /finalize
     │             │               │               │              ├──────>│
```

## 📘 Guía de Desarrollo

### Cómo Añadir un Nuevo Caso de Uso

**Ejemplo**: Añadir funcionalidad de "Transferencia de Activos"

#### 1. Definir el Puerto de Entrada (Application Layer)

Crear interfaz en `application/ports/input/AssetTransferUseCase.java`:
```java
public interface AssetTransferUseCase {
    Mono<TransferResponse> transferAsset(UUID assetId, TransferRequest request);
    Flux<TransferResponse> getTransferHistory(UUID assetId);
}
```

#### 2. Crear DTOs (Application Layer)

En `application/dto/`:
```java
@Data
public class TransferRequest {
    private UUID newLocationId;
    private UUID newResponsibleId;
    private UUID newAreaId;
    private LocalDateTime transferDate;
    private String transferReason;
}
```

#### 3. Definir Puerto de Salida si es necesario (Application Layer)

Si necesitas persistencia adicional, crear en `application/ports/output/`:
```java
public interface TransferPersistencePort {
    Mono<Transfer> save(Transfer transfer);
    Flux<Transfer> findByAssetId(UUID assetId);
}
```

#### 4. Implementar el Servicio (Application Layer)

Crear `application/service/AssetTransferService.java`:
```java
@Service
public class AssetTransferService implements AssetTransferUseCase {
    
    private final AssetPersistencePort assetPort;
    private final TransferPersistencePort transferPort;
    
    @Override
    public Mono<TransferResponse> transferAsset(UUID assetId, TransferRequest request) {
        return assetPort.findById(assetId)
            .switchIfEmpty(Mono.error(new AssetNotFoundException()))
            .flatMap(asset -> {
                // Lógica de negocio aquí
                asset.setCurrentLocationId(request.getNewLocationId());
                asset.setCurrentResponsibleId(request.getNewResponsibleId());
                // ...
                return assetPort.save(asset);
            })
            .map(this::toResponse);
    }
}
```

#### 5. Implementar Adaptador de Persistencia (Infrastructure Layer)

En `infrastructure/adapters/output/persistence/`:
```java
@Component
public class TransferPersistenceAdapter implements TransferPersistencePort {
    
    private final TransferRepository repository;
    
    @Override
    public Mono<Transfer> save(Transfer transfer) {
        // Convertir Domain → Entity → guardar
        return repository.save(toEntity(transfer))
            .map(this::toDomain);
    }
}
```

#### 6. Crear Controlador REST (Infrastructure Layer)

En `infrastructure/adapters/input/rest/`:
```java
@RestController
@RequestMapping("/api/v1/transfers")
public class TransferRestController {
    
    private final AssetTransferUseCase transferUseCase;
    
    @PostMapping("/asset/{assetId}")
    public Mono<ResponseEntity<TransferResponse>> transfer(
        @PathVariable UUID assetId,
        @RequestBody TransferRequest request) {
        
        return transferUseCase.transferAsset(assetId, request)
            .map(response -> ResponseEntity.ok(response));
    }
}
```

### Convenciones que Usamos

Si vas a añadir código nuevo, sigue estas convenciones para mantener consistencia:

**Nombres de clases**:
- Entidades de dominio: Sustantivos simples (`Asset`, `Depreciation`, `AssetDisposal`)
- Servicios: Termina en `Service` (`AssetService`, `DepreciationService`)
- Adaptadores: Termina en `Adapter` (`AssetPersistenceAdapter`, `AreasClientAdapter`)
- Controladores REST: Termina en `RestController` (`AssetRestController`)

**Nombres de métodos**:
- CRUD básico: `create`, `getById`, `getAll`, `update`, `delete`
- Consultas: `findBy...`, `searchBy...` (ej: `findByStatus`, `searchByCode`)
- Acciones de negocio: verbos claros (`approve`, `cancel`, `execute`, `assign`)

**Trabajando con Mono/Flux** (programación reactiva):
- Usa `.switchIfEmpty()` cuando necesites manejar casos donde no se encuentra nada
- Usa `.flatMap()` para operaciones que retornan otro Mono/Flux
- Usa `.map()` para transformaciones síncronas simples
- Usa `.zipWith()` para combinar múltiples streams en paralelo
- No bloquees el flujo reactivo (nada de `.block()`)

**Excepciones**:
- Crea excepciones específicas en `domain/exception/`
- Extienden de `RuntimeException`
- Nombres descriptivos terminados en `Exception`
- No uses excepciones de Spring en el dominio

## Manejo de Errores

### Excepciones que Usamos

Todas están en `domain/exception/`. Estas son nuestras excepciones personalizadas:

| Excepción | Cuándo la tiramos | Código HTTP |
|-----------|----------------|-------------|
| `AssetNotFoundException` | No encontramos un activo por su ID | 404 NOT FOUND |
| `AssetDisposalNotFoundException` | No existe la solicitud de baja | 404 NOT FOUND |
| `AssetDisposalDetailNotFoundException` | No existe el detalle de baja | 404 NOT FOUND |
| `DepreciationNotFoundException` | No encontramos la depreciación | 404 NOT FOUND |
| `InvalidDisposalStateException` | Intentaron hacer algo inválido en el estado actual (ej: ejecutar una baja sin resolución) | 400 BAD REQUEST |

### Cómo Manejamos Errores en Cada Capa

#### En el Dominio
Aquí simplemente lanzamos excepciones directas:
```java
// Si algo está mal, tiramos excepción
if (asset == null) {
    throw new AssetNotFoundException("Asset not found: " + id);
}
```

#### En los Servicios (Aplicación)
Usamos operadores reactivos para manejar errores:
```java
// No bloqueamos, usamos .switchIfEmpty() para errores
return persistencePort.findById(id)
    .switchIfEmpty(Mono.error(
        new AssetNotFoundException("Asset not found with ID: " + id)))
    .flatMap(asset -> {
        // Lógica de negocio
        return persistencePort.save(asset);
    });
```

#### En los Controladores (Infraestructura)
Tenemos un handler global con `@ControllerAdvice`:
```java
// Este handler atrapa todas las excepciones y devuelve respuestas HTTP apropiadas
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AssetNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(AssetNotFoundException ex) {
        return Mono.just(ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage())));
    }
    
    @ExceptionHandler(InvalidDisposalStateException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidState(InvalidDisposalStateException ex) {
        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage())));
    }
}
```

### Manejo de Errores en WebClient (Servicios Externos)

```java
return webClient.get()
    .uri("/api/categories/{id}", categoryId)
    .retrieve()
    .onStatus(HttpStatus.NOT_FOUND::equals, 
        response -> Mono.error(new CategoryNotFoundException()))
    .onStatus(HttpStatus::is5xxServerError,
        response -> Mono.error(new ExternalServiceException()))
    .bodyToMono(CategoryDTO.class)
    .timeout(Duration.ofSeconds(5))
    .onErrorResume(WebClientException.class, 
        ex -> Mono.error(new ServiceCommunicationException(ex)));
```

### Formato de Respuesta de Error

```json
{
  "timestamp": "2026-01-26T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Asset not found with ID: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/api/v1/assets/123e4567-e89b-12d3-a456-426614174000"
}
```

## 📝 Ejemplos de Uso

### 1. Crear un Asset (Activo)

```bash
curl -X POST http://localhost:5003/api/v1/assets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "assetCode": "ASSET-2024-001",
    "name": "Laptop Dell Latitude 5420",
    "description": "Laptop corporativa para desarrollo",
    "categoryId": "a1b2c3d4-e5f6-7a89-b012-3456789abcd",
    "supplierId": "99999999-aaaa-bbbb-cccc-dddddddddddd",
    "responsibleId": "22222222-3333-4444-5555-666666666666",
    "locationId": "11111111-2222-3333-4444-555555555555",
    "acquisitionValue": 1500.00,
    "residualValue": 300.00,
    "usefulLifeMonths": 36,
    "acquisitionDate": "2024-01-15T00:00:00",
    "status": "ACTIVO"
  }'
```

### 2. Calcular Depreciación Mensual

```bash
curl -X POST http://localhost:5003/api/v1/depreciations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "assetId": "asset-uuid-here",
    "fiscalYear": 2024,
    "calculationMonth": 11,
    "monthlyDepreciation": 33.33,
    "accumulatedDepreciation": 366.63,
    "bookValue": 1133.37,
    "status": "CALCULADO"
  }'
```

### 3. Generar Depreciaciones Automáticas

```bash
curl -X GET "http://localhost:5003/api/v1/depreciations/auto/{assetId}?initialValue=1500.00&residualValue=300.00&usefulLifeMonths=36&acquisitionDate=2024-01-15T00:00:00" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Crear Solicitud de Baja

```bash
curl -X POST http://localhost:5003/api/v1/asset-disposals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "fileNumber": "EXP-2024-001",
    "disposalType": "BAJA",
    "requestedBy": "user-uuid-here",
    "requestDate": "2024-11-01T00:00:00",
    "justification": "Equipos obsoletos no funcionales",
    "status": "PENDIENTE"
  }'
```

### 5. Asignar Comisión Evaluadora

```bash
curl -X PUT http://localhost:5003/api/v1/asset-disposals/{id}/assign-committee \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "committeeMembers": ["user-id-1", "user-id-2", "user-id-3"],
    "assignedBy": "user-uuid-here",
    "assignedDate": "2024-11-05T00:00:00"
  }'
```

### 6. Listar Áreas (Desde Configuration Service)

```bash
curl -X GET http://localhost:5003/api/v1/areas \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 7. Listar Responsables (Desde Authentication Service)

```bash
curl -X GET http://localhost:5003/api/v1/responsible \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Testing

### Colección de Postman

Incluimos una colección completa de Postman con todos los endpoints (42 en total). Está en el archivo `postman_collection.json`.

**Cómo usarla**:
1. Importa `postman_collection.json` en Postman
2. Configura estas variables de entorno:
   - `baseUrl`: `http://localhost:5003/api/v1`
   - `authToken`: Tu token JWT (obténlo del servicio de autenticación)
3. Las peticiones están organizadas por módulos:
   - Áreas, Categorías, Ubicaciones, Responsables, Proveedores (proxy a servicios externos)
   - Activos (Assets)
   - Depreciaciones (Depreciations)
   - Bajas de Activos (Asset Disposals y Details)

### Tests Automatizados

Para correr los tests:

```bash
# Todos los tests
./mvnw test

# Con reporte de cobertura
./mvnw test jacoco:report

# Ver el reporte de cobertura (se abre en el navegador)
open target/site/jacoco/index.html
```

Nota: Aún estamos trabajando en ampliar la cobertura de tests. Prioridad: tests del dominio y servicios de aplicación.

## Seguridad y Autenticación

Usamos JWT tokens para autenticación. Además, propagamos el token a los servicios externos que consumimos.

### Cómo funciona el AuthTokenFilter

Tenemos un filtro (`AuthTokenFilter`) en `infrastructure/config/` que intercepta todas las peticiones:
1. Extrae el header `Authorization: Bearer <token>`
2. Lo guarda en el contexto de Reactor (usando la clave `authToken`)
3. Todas las llamadas salientes (WebClient) automáticamente incluyen este token

Es decir, el token "viaja" desde el cliente original hasta los servicios externos.

### Propagación de Token entre Servicios

Los adaptadores de cliente (en `infrastructure/adapters/output/client/`) tienen un `ExchangeFilterFunction` que:
- Lee el token del contexto de Reactor
- Lo inyecta en las peticiones HTTP hacia servicios externos
- Así el servicio externo sabe quién es el usuario original

**Ejemplo del flujo**:
```
Cliente → [Authorization: Bearer xxx] → REST Controller (Patrimonio Service)
                                             ↓ (AuthTokenFilter guarda token en contexto)
                                             ↓ (Application Service procesa)
                                             ↓ (Client Adapter lee token del contexto)
                                             ↓ [Authorization: Bearer xxx]
                                        Authentication Service
```

Bonito, ¿no? El usuario se autentica una vez y el token se propaga automáticamente.

## 🔄 Flujo de Procesos de Negocio

### Proceso de Baja de Activos (Asset Disposal)

```
1. SOLICITUD
   └─> POST /asset-disposals (PENDIENTE)
       └─> POST /asset-disposal-details (añadir activos)

2. ASIGNACIÓN DE COMISIÓN
   └─> PUT /asset-disposals/{id}/assign-committee (EN_COMISION)

3. EVALUACIÓN TÉCNICA
   └─> PUT /asset-disposal-details/{id}/technical-opinion (dictamen)

4. RESOLUCIÓN
   └─> PUT /asset-disposals/{id}/resolve (RESUELTO)

5. EJECUCIÓN DE BAJA
   └─> PATCH /asset-disposal-details/{id}/execute-removal (EJECUTADO)

6. FINALIZACIÓN
   └─> PUT /asset-disposals/{id}/finalize (FINALIZADO)
```

### Cálculo de Depreciación Automática

El método `GET /depreciations/auto/{assetId}` genera depreciaciones mensuales desde la fecha de adquisición hasta el mes actual:

- **Fórmula**: Depreciación mensual = `(Valor inicial - Valor residual) / Vida útil (meses)`
- **Acumulado**: Suma de depreciaciones mensuales
- **Valor en libros**: `Valor inicial - Depreciación acumulada`

## 📚 Documentación Adicional

- **[postman_collection.json](./postman_collection.json)** - Colección Postman completa con 42 endpoints
- **[schema.sql](src/main/resources/db/schema.sql)** - Esquema DDL de la base de datos
- **[application.yml](src/main/resources/application.yml)** - Configuración del servicio

### Recursos sobre Arquitectura Hexagonal

- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Spring Boot Hexagonal Architecture Example](https://reflectoring.io/spring-hexagonal/)

## Tecnologías Reactivas

Este proyecto usa programación reactiva. Si no estás familiarizado, aquí va lo básico:

**Conceptos clave**:
- **Flux<T>**: Es como un stream de 0 a N elementos. Lo usamos para listas. Ej: `Flux<Asset>` para lista de activos.
- **Mono<T>**: Es como un stream de 0 o 1 elemento. Lo usamos para un solo objeto. Ej: `Mono<Asset>` para un activo.
- **R2DBC**: Driver reactivo para PostgreSQL. No bloquea threads esperando la BD.
- **WebClient**: Cliente HTTP reactivo. Reemplaza a RestTemplate (que era bloqueante).
- **Backpressure**: Control de flujo automático para que no nos saturemos con datos.

**Ventajas que hemos visto**:
- Mayor throughput con menos recursos (CPU, RAM)
- Escala mejor. Podemos manejar miles de conexiones concurrentes sin problema
- Menos latencia en operaciones de I/O (BD, HTTP)
- Todo es no-bloqueante. No se desperdician threads esperando respuestas.

**Advertencia para nuevos**: La programación reactiva tiene una curva de aprendizaje. No uses `.block()` a menos que realmente lo necesites (casi nunca). Si bloqueas, pierdes todos los beneficios.

## Migración de Arquitectura

### De MVC a Hexagonal: Nuestra Experiencia

Originalmente este proyecto estaba en MVC tradicional. Lo migramos a Arquitectura Hexagonal en enero de 2026. Aquí está la diferencia:

#### Cómo era antes (MVC):
```
Controller → Service → Repository → Base de Datos
              ↓
           WebClient → Servicios Externos
```
Todo mezclado. Los servicios tenían lógica de negocio, llamadas a BD y consumo de APIs. Difícil de testear y mantener.

#### Cómo es ahora (Hexagonal):
```
REST Adapter → Application Service (Use Cases) → Domain
                      ↓                 ↓
              Persistence Adapter   Client Adapter
```
Separación clara. El dominio solo tiene lógica de negocio. La infraestructura maneja los detalles técnicos.

### Qué Ganamos con la Migración:

**1. Separación Clara de Responsabilidades**
- Dominio: Lógica de negocio pura (cero dependencias de frameworks)
- Aplicación: Orquestación de casos de uso
- Infraestructura: Detalles técnicos (REST, BD, WebClient)

**2. Inversión de Dependencias**
- El dominio no conoce Spring, R2DBC ni WebClient
- Los adaptadores implementan interfaces que el dominio define
- Si queremos cambiar de PostgreSQL a MongoDB, solo tocamos los adaptadores

**3. Más Fácil de Testear**
- Los tests del dominio no necesitan Spring ni BD
- Podemos mockear los puertos fácilmente
- Tests más rápidos y enfocados

**4. Flexibilidad Real**
- Cambiar de BD: solo afecta adaptadores de persistencia
- Cambiar de REST a GraphQL: solo afecta adaptadores de entrada
- Añadir nuevos casos de uso: no tocas infraestructura

**5. Código más Ordenado**
- Cada capa evoluciona independientemente
- Más fácil trabajar en equipo (cada uno puede trabajar en su capa)
- Reutilizar lógica de negocio en diferentes contextos (CLI, gRPC, etc.)

**Desafíos que enfrentamos**:
- Curva de aprendizaje inicial (sobre todo entender puertos y adaptadores)
- Más archivos y carpetas (pero más organizados)
- Al principio parece "over-engineering" pero vale la pena en proyectos medianos/grandes

## 👥 Equipo de Desarrollo

- **Backend Team** - Instituto Valle Grande
- **Arquitectura**: Arquitectura Hexagonal + Microservicios reactivos con Spring WebFlux
- **Patrón de Diseño**: Puertos y Adaptadores
- **Integración**: WebClient para comunicación entre servicios

## 📄 Licencia

Este proyecto es propiedad de **Instituto Valle Grande**.

## 🔗 Enlaces Relacionados

### Spring Framework y Programación Reactiva
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [R2DBC - Reactive Relational Database Connectivity](https://r2dbc.io/)
- [Project Reactor](https://projectreactor.io/)
- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)
- [WebClient Documentation](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)

### Arquitectura Hexagonal
- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Spring Boot Hexagonal Architecture Example](https://reflectoring.io/spring-hexagonal/)
- [Ports and Adapters Pattern](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/)

---

**Última actualización:** 22 de enero de 2026  
**Versión:** 3.0.0 - Arquitectura Hexagonal  
**Puerto por defecto:** 5003  
**Base URL:** `http://localhost:5003/api/v1`

### Changelog

#### v3.0.0 (Enero 2026) - Migración a Arquitectura Hexagonal
La gran migración. Reestructuramos todo el proyecto:
- Migramos de MVC tradicional a Arquitectura Hexagonal completa
- Reorganizamos en 3 capas bien definidas: Domain, Application, Infrastructure
- Implementamos Puertos y Adaptadores
- Separación total de responsabilidades (el dominio ya no conoce Spring)
- Mejoramos muchísimo la testabilidad y mantenibilidad
- Aplicamos inversión de dependencias correctamente

#### v2.0.0 (Diciembre 2025) - Arquitectura Reactiva
El cambio a programación reactiva:
- Implementamos Spring WebFlux (adiós bloqueos)
- Migramos a R2DBC para acceso reactivo a PostgreSQL
- Cambiamos RestTemplate por WebClient
- Implementamos sistema de propagación de tokens JWT entre servicios

#### v1.0.0 (Noviembre 2025) - Versión Inicial
La primera versión funcional:
- Arquitectura MVC tradicional
- Gestión básica de activos y depreciaciones
- Proceso de bajas de activos implementado
