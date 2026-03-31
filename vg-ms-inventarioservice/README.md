# 📦 Microservicio de Inventario Físico

Microservicio reactivo para la gestión de inventarios físicos de activos municipales, construido con Spring Boot 3 y WebFlux.

## 🎯 Descripción

Este microservicio forma parte de un ecosistema de microservicios para la gestión de activos municipales. Proporciona funcionalidades completas para la planificación, ejecución y seguimiento de inventarios físicos, permitiendo diferentes tipos de inventarios (general, selectivo, especial) con validación de reglas de negocio específicas.

### Características Principales

- **Arquitectura Reactiva**: Implementado con Spring WebFlux y R2DBC para operaciones no bloqueantes
- **Gestión de Inventarios**: Creación, actualización, inicio, completado y eliminación lógica de inventarios
- **Tipos de Inventario**: Soporte para inventarios GENERAL, SELECTIVE, SPECIAL y RECONCILIATION
- **Detalles de Inventario**: Seguimiento detallado de cada activo con estados (FOUND, MISSING, SURPLUS, DAMAGED)
- **Integración con Microservicios**: Comunicación con servicios de Activos, Configuración y Usuarios
- **Validación de Reglas**: Validación automática según el tipo de inventario
- **Auditoría**: Registro completo de creación, modificación y eliminación con trazabilidad de usuarios
- **Documentación API**: Swagger/OpenAPI integrado para exploración de endpoints

## 🏗️ Arquitectura

### Stack Tecnológico

- **Framework**: Spring Boot 3.5.7
- **Programación Reactiva**: Spring WebFlux, Project Reactor
- **Base de Datos**: PostgreSQL con R2DBC (acceso reactivo)
- **Documentación**: SpringDoc OpenAPI 2.2.0
- **Build Tool**: Maven
- **Java Version**: 17

### Estructura del Proyecto

```
src/main/java/pe/edu/vallegrande/ms_inventory/
├── config/              # Configuraciones y clientes HTTP
│   ├── AssetService.java
│   ├── ConfigurationService.java
│   ├── UserService.java
│   ├── CorsConfig.java
│   ├── R2dbcConfig.java
│   └── JsonNode Converters
├── controller/          # Controladores REST
│   ├── PhysicalInventoryController.java
│   └── PhysicalInventoryDetailController.java
├── dto/                 # Data Transfer Objects
│   ├── PhysicalInventoryDTO.java
│   ├── PhysicalInventoryDetailDTO.java
│   ├── InventoryFormDataDTO.java
│   └── [otros DTOs]
├── exception/           # Manejo de excepciones
│   ├── GlobalExceptionHandler.java
│   ├── BadRequestException.java
│   └── ResourceNotFoundException.java
├── model/               # Entidades de dominio
│   ├── PhysicalInventory.java
│   ├── PhysicalInventoryDetail.java
│   └── User.java
├── repository/          # Repositorios R2DBC
│   ├── PhysicalInventoryRepository.java
│   ├── PhysicalInventoryDetailRepository.java
│   └── UserRepository.java
└── service/             # Lógica de negocio
    ├── PhysicalInventoryService.java
    ├── PhysicalInventoryDetailService.java
    └── impl/
```

## 🚀 Instalación y Ejecución

### Prerrequisitos

- Java 17 o superior
- Maven 3.9+
- PostgreSQL (o acceso a instancia Neon)
- Docker (opcional)

### Ejecución Local

1. **Clonar el repositorio**
```bash
git clone https://gitlab.com/vallegrande/as232s5_prs2/vg-ms-inventarioservice.git
cd vg-ms-inventarioservice
```

2. **Configurar variables de entorno** (opcional)

Editar `src/main/resources/application.yaml` con tus credenciales de base de datos.

3. **Compilar y ejecutar**
```bash
# Compilar
mvn clean package

# Ejecutar
java -jar target/ms-inventory-0.0.1-SNAPSHOT.jar
```

4. **Acceder a la aplicación**
- API: http://localhost:5006
- Swagger UI: http://localhost:5006/swagger-ui/index.html
- API Docs: http://localhost:5006/v3/api-docs

### Ejecución con Docker

#### Opción 1: Usar imagen pre-construida

```bash
# Descargar la imagen
docker pull hilaryvivanco/vg-ms-inventarioservice:1.0

# Ejecutar el contenedor
docker run -d \
  --name ms-inventory \
  -p 5006:5006 \
  -e SPRING_R2DBC_URL=r2dbc:postgresql://your-db-host:5432/neondb \
  -e SPRING_R2DBC_USERNAME=your-username \
  -e SPRING_R2DBC_PASSWORD=your-password \
  hilaryvivanco/vg-ms-inventarioservice:1.0
```

#### Opción 2: Construir imagen localmente

```bash
# Construir la imagen
docker build -t hilaryvivanco/vg-ms-inventarioservice:1.0 .

# Ejecutar el contenedor
docker run -d \
  --name ms-inventory \
  -p 5006:5006 \
  hilaryvivanco/vg-ms-inventarioservice:1.0
```

#### Actualizar imagen en Docker Hub

```bash
# Construir nueva versión
docker build -t hilaryvivanco/vg-ms-inventarioservice:1.1 .

# Etiquetar como latest
docker tag hilaryvivanco/vg-ms-inventarioservice:1.1 hilaryvivanco/vg-ms-inventarioservice:latest

# Subir a Docker Hub
docker push hilaryvivanco/vg-ms-inventarioservice:1.1
docker push hilaryvivanco/vg-ms-inventarioservice:latest
```

## 📡 API Endpoints

### Inventarios Físicos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/inventories` | Listar todos los inventarios |
| GET | `/api/v1/inventories/{id}` | Obtener inventario por ID |
| GET | `/api/v1/inventories/with-details` | Listar inventarios con detalles |
| GET | `/api/v1/inventories/form-data` | Obtener datos para formularios |
| POST | `/api/v1/inventories` | Crear nuevo inventario |
| PUT | `/api/v1/inventories/{id}` | Actualizar inventario |
| PUT | `/api/v1/inventories/{id}/start` | Iniciar inventario |
| PUT | `/api/v1/inventories/{id}/complete` | Completar inventario |
| DELETE | `/api/v1/inventories/{id}?userId={uuid}` | Eliminar inventario (lógico) |

### Detalles de Inventario

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/inventory-details` | Listar todos los detalles |
| GET | `/api/v1/inventory-details/by-inventory/{id}` | Detalles por inventario |
| POST | `/api/v1/inventory-details` | Crear detalle |
| PUT | `/api/v1/inventory-details/{id}` | Actualizar detalle |
| DELETE | `/api/v1/inventory-details/{id}` | Eliminar detalle |

## 🔧 Configuración

### Variables de Entorno

```yaml
# Base de datos
SPRING_R2DBC_URL: r2dbc:postgresql://host:5432/database
SPRING_R2DBC_USERNAME: username
SPRING_R2DBC_PASSWORD: password

# Puerto del servidor
SERVER_PORT: 5006

# URLs de microservicios externos
SERVICES_ASSET_URL: http://localhost:5003
SERVICES_CONFIGURATION_URL: http://localhost:5004
SERVICES_USER_URL: http://localhost:5002
```

### Tipos de Inventario

- **GENERAL**: Inventario completo sin filtros
- **SELECTIVE**: Inventario con un único filtro (área, categoría o ubicación)
- **SPECIAL**: Inventario especial con filtros opcionales
- **RECONCILIATION**: Inventario de reconciliación

### Estados de Inventario

- **PLANNED**: Planificado (inicial)
- **IN_PROCESS**: En proceso
- **COMPLETED**: Completado
- **SUSPENDED**: Suspendido
- **CANCELLED**: Cancelado (eliminado lógicamente)

### Estados de Detalle

- **FOUND**: Bien encontrado
- **MISSING**: Bien faltante
- **SURPLUS**: Bien sobrante
- **DAMAGED**: Bien dañado

## 🔗 Integración con Microservicios

Este microservicio se integra con:

- **ms-assets** (puerto 5003): Gestión de activos
- **ms-configuration** (puerto 5004): Áreas, categorías y ubicaciones
- **ms-users** (puerto 5002): Gestión de usuarios

## 📊 Modelo de Datos

### Tabla: physical_inventories

Almacena la información principal de cada inventario físico.

**Campos principales:**
- `id`: UUID (PK)
- `municipality_id`: UUID
- `inventory_number`: VARCHAR(50) UNIQUE
- `inventory_type`: VARCHAR(30)
- `inventory_status`: VARCHAR(30)
- `description`: TEXT
- `area_id`, `category_id`, `location_id`: UUID (filtros)
- `planned_start_date`, `planned_end_date`: DATE
- `general_responsible_id`: UUID
- `inventory_team`: JSONB
- `created_by`, `updated_by`: UUID
- `created_at`, `updated_at`: TIMESTAMP

### Tabla: physical_inventory_detail

Almacena el detalle de cada activo en el inventario.

**Campos principales:**
- `id`: UUID (PK)
- `inventory_id`: UUID (FK lógica)
- `asset_id`: UUID
- `found_status`: VARCHAR(30)
- `actual_conservation_status`: VARCHAR(30)
- `verified_by`: UUID
- `verification_date`: TIMESTAMP
- `observations`: TEXT
- `photographs`: JSONB

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report
```

## 📝 Changelog

### Version 1.1 (Actual)
- ✅ Corrección en eliminación lógica de inventarios
- ✅ Eliminada validación de usuario contra servicio externo (evita error 401)
- ✅ Mejora en manejo de errores

### Version 1.0
- ✅ Implementación inicial
- ✅ CRUD completo de inventarios y detalles
- ✅ Integración con microservicios externos
- ✅ Validación de reglas de negocio
- ✅ Documentación Swagger

## 👥 Autor

**Hilary Vivanco**
- Docker Hub: [ImageInventory](https://hub.docker.com/r/hilaryvivanco/vg-ms-inventarioservice)
- GitLab: [Valle Grande - AS232S5_PRS2](https://gitlab.com/vallegrande/as232s5_prs2)

## 📄 Licencia

Este proyecto es parte del sistema de gestión de activos de Valle Grande.

## 🤝 Contribución

Para contribuir al proyecto:

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📞 Soporte

Para reportar problemas o solicitar nuevas funcionalidades, por favor crea un issue en el repositorio de GitLab.

---

**Estado del Proyecto**: ✅ Activo y en desarrollo
