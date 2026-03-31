# Auditoría Exhaustiva: vg-ms-configurationservice

**Fecha de Auditoría:** Marzo 2026
**Versión Evaluada:** Spring Boot 3.5.6 + WebFlux + R2DBC
**Alcance:** Seguridad, Arquitectura, Persistencia, Patrones Reactivos, Testing
**Estado General:** 3.8/10 (Críticos bloqueantes identificados)

---

## 1. RESUMEN EJECUTIVO

El servicio de configuración (`vg-ms-configurationservice`) concentra catálogos y parámetros transversales del ecosistema:

- Áreas, ubicaciones físicas, cargos y categorías
- Proveedores y tipos de documento
- Reglas `position_allowed_roles` usadas por otros servicios
- Reportes PDF/Excel de posiciones

**Hallazgos Críticos:** 7 problemas requieren corrección inmediata antes de producción expuesta. El servicio mantiene credenciales en repositorio, no tiene capa de seguridad propia y presenta riesgos de consistencia multi-tenant.

---

## 2. PROBLEMAS CRÍTICOS

### 2.1 Credenciales de Base de Datos Expuestas en `application.yml` (CRÍTICO - SEGURIDAD)

**Ubicación:**

- [src/main/resources/application.yml](src/main/resources/application.yml#L6)
- [src/main/resources/application.yml](src/main/resources/application.yml#L7)
- [src/main/resources/application.yml](src/main/resources/application.yml#L8)

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://ep-summer-cloud-adbsahn8-pooler.c-2.us-east-1.aws.neon.tech/neondb
    username: neondb_owner
    password: npg_2ZfaetrWy4OG
```

**Riesgo:**

- Cualquier actor con acceso al repositorio obtiene acceso directo a la BD.
- Exposición de datos de configuración con impacto en múltiples servicios.

**Impacto de Producción:**

- Alteración maliciosa de catálogos y reglas operativas.
- Compromiso de integridad en procesos dependientes.

**Remediación:**

```yaml
spring:
  r2dbc:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

- Rotar credenciales actuales inmediatamente.
- Eliminar secretos del historial Git.

---

### 2.2 Servicio Sin Seguridad de Aplicación (CRÍTICO - AUTORIZACIÓN)

**Evidencia:**

- Dependencias sin `spring-boot-starter-security` ni resource server en [pom.xml](pom.xml#L27)
- Endpoints CRUD sin anotaciones de rol, por ejemplo [AreasController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/AreasController.java#L13)
- No se encontraron clases de seguridad/autenticación en `src/main/java`

**Riesgo:**

- Si el gateway se omite o se configura mal, el microservicio queda abierto.
- No existe defensa en profundidad a nivel del servicio.

**Impacto de Producción:**

- Acceso no autorizado para crear/editar/eliminar configuración de negocio.

**Remediación:**

- Incorporar OAuth2 Resource Server con JWT.
- Proteger endpoints con autorización por rol/tenant.
- Mantener controles del gateway, pero no depender exclusivamente de él.

---

### 2.3 CORS Global Abierto a Cualquier Origen (CRÍTICO - SUPERFICIE DE ATAQUE)

**Ubicación:**

- [CorsConfig.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/config/CorsConfig.java#L21)
- [CorsConfig.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/config/CorsConfig.java#L24)
- [CorsConfig.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/config/CorsConfig.java#L27)
- [CorsConfig.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/config/CorsConfig.java#L30)

```java
config.setAllowCredentials(false);
config.addAllowedOriginPattern("*");
config.addAllowedHeader("*");
config.addAllowedMethod("*");
```

**Riesgo:**

- Cualquier origen puede invocar APIs del servicio.
- Aumenta la probabilidad de abuso de endpoints desde navegadores.

**Remediación:**

- Lista cerrada de orígenes por ambiente.
- Lista mínima de métodos y headers permitidos.

---

### 2.4 Entrada de Entidades de Dominio Sin Validación (CRÍTICO - INTEGRIDAD DE DATOS)

**Ubicación:**

- [AreasController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/AreasController.java#L42)
- [SupplierController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/SupplierController.java#L37)
- [PositionAllowedRoleController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/PositionAllowedRoleController.java#L61)

**Riesgo:**

- `@RequestBody` mapea directamente a entidades de dominio.
- No hay `@Valid` ni DTOs de entrada para controlar contratos.
- Riesgo de mass assignment y campos críticos manipulables por cliente.

**Impacto de Producción:**

- Persistencia de datos inconsistentes o fuera de regla de negocio.
- Mayor acoplamiento API-dominio y más riesgo de regresión al evolucionar modelos.

**Remediación:**

- Introducir DTOs request/response por endpoint.
- Aplicar Bean Validation y validaciones semánticas.

---

### 2.5 Inconsistencia `created_by` en `position_allowed_roles` (CRÍTICO - CONSISTENCIA)

**Ubicación SQL:**

- [src/main/resources/db/position_allowed_roles.sql](src/main/resources/db/position_allowed_roles.sql#L8)

**Ubicación Servicio/API:**

- [PositionAllowedRoleService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionAllowedRoleService.java#L52)
- [PositionAllowedRoleController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/PositionAllowedRoleController.java#L61)

```sql
created_by UUID NOT NULL
```

```java
entity.setCreatedAt(LocalDateTime.now());
// No se setea createdBy server-side
return repository.save(entity);
```

**Riesgo:**

- Si cliente no envía `createdBy`, falla inserción.
- Si cliente lo envía, la auditoría queda sujeta a spoofing.

**Remediación:**

- Poblar `createdBy` solo desde contexto autenticado.
- Rechazar `createdBy` en payload externo.

---

### 2.6 `municipalityId` Hardcodeado en Servicios (CRÍTICO - RIESGO MULTI-TENANT)

**Ubicación:**

- [SupplierService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/SupplierService.java#L54)
- [PositionService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionService.java#L39)

```java
supplier.setMunicipalityId(UUID.fromString("24ad12a5-d9e5-4cdd-91f1-8fd0355c9473"));
position.setMunicipalityId(UUID.fromString("24ad12a5-d9e5-4cdd-91f1-8fd0355c9473"));
```

**Riesgo:**

- Asignación por defecto fija puede mezclar datos entre municipios.
- Vulnera aislamiento esperado en despliegues multi-tenant.

**Remediación:**

- Resolver `municipalityId` desde claims/contexto de autorización.
- Rechazar requests sin contexto de tenant válido.

---

### 2.7 Dependencias R2DBC Duplicadas y Versionadas en Conflicto (CRÍTICO - ESTABILIDAD)

**Ubicación:**

- [pom.xml](pom.xml#L42)
- [pom.xml](pom.xml#L81)
- [pom.xml](pom.xml#L84)

**Detalle:**

- Se declara `r2dbc-postgresql` vía Spring Boot BOM.
- También se declara `io.r2dbc:r2dbc-postgresql:0.8.2.RELEASE` explícitamente.

**Riesgo:**

- Conflictos de clases y comportamiento impredecible en runtime.

**Remediación:**

- Mantener una sola dependencia del driver, gestionada por el BOM de Spring Boot.

---

## 3. ANÁLISIS ARQUITECTURA

### 3.1 Estructura General (POSITIVO - Capas Claras)

El código separa responsabilidades en `application`, `domain` e `infrastructure`:

- Servicios de aplicación en [application/service](src/main/java/pe/edu/vallegrande/configurationservice/application/service)
- Modelos y excepciones de dominio en [domain/model](src/main/java/pe/edu/vallegrande/configurationservice/domain/model)
- Adaptadores REST y repositorios en [infrastructure/adapters](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters)

**Evaluación:** La separación de capas es buena base para evolución, pero queda debilitada por ausencia de seguridad y contratos DTO.

---

### 3.2 Controladores REST (10 Controladores Identificados)

| Controlador | Estado Seguridad | Hallazgo |
|---|---|---|
| [AreasController](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/AreasController.java) | Sin guardas | Endpoints duplicados (`/` y `/GetAll`) |
| [PhysicalLocationsController](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/PhysicalLocationsController.java) | Sin guardas | Mismo patrón duplicado |
| [SupplierController](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/SupplierController.java) | Sin guardas | Payload directo de entidad |
| [PositionAllowedRoleController](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/PositionAllowedRoleController.java) | Sin guardas | Auditoría delegada al cliente |
| [PositionController](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/PositionController.java) | Sin guardas | Exposición directa si bypass gateway |
| [SystemConfigurationController](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/SystemConfigurationController.java) | Sin guardas | Sin control de rol/tenant |

**Evaluación:** Arquitectura REST funcional, pero sin seguridad in-service.

---

### 3.3 Servicio de Reportes (POSITIVO + RIESGO)

**Ubicación:** [PositionReportService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionReportService.java#L45)

**Aspectos positivos:**

- Generación PDF y Excel bien estructurada en métodos dedicados.
- Uso de DTO de reporte y resumen.

**Riesgo identificado:**

- Carga total en memoria con `collectList()` para resumen/exportaciones:
  - [PositionReportService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionReportService.java#L63)
  - [PositionReportService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionReportService.java#L73)
  - [PositionReportService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionReportService.java#L77)

---

### 3.4 Esquema SQL (POSITIVO - Restricciones Útiles)

**Ubicación:**

- [src/main/resources/db/supplier.sql](src/main/resources/db/supplier.sql#L38)
- [src/main/resources/db/supplier.sql](src/main/resources/db/supplier.sql#L39)
- [src/main/resources/db/supplier.sql](src/main/resources/db/supplier.sql#L40)
- [src/main/resources/db/position_allowed_roles.sql](src/main/resources/db/position_allowed_roles.sql#L10)

**Fortalezas:**

- Restricción única de documento por tipo en proveedores.
- Check constraints para `company_type` y `classification`.
- Constraint de unicidad para combinación cargo-área-rol-municipio.

---

## 4. PATRONES REACTIVOS (R2DBC + WebFlux)

### 4.1 Uso de `Flux`/`Mono` en CRUD (POSITIVO)

Ejemplos:

- [AreaService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/AreaService.java#L19)
- [PhysicalLocationService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PhysicalLocationService.java#L19)
- [SupplierService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/SupplierService.java#L32)

**Evaluación:** Patrón reactivo aplicado de forma consistente para operaciones de CRUD.

---

### 4.2 Ausencia de Política de Errores de Dominio en `getById` (MEDIUM)

**Ubicación:**

- [AreaService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/AreaService.java#L31)
- [PhysicalLocationService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PhysicalLocationService.java#L31)
- [DocumentTypeService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/DocumentTypeService.java#L24)

**Detalle:**

- Múltiples `getById` retornan `repository.findById` sin `switchIfEmpty` hacia excepción 404 de dominio.

**Impacto:**

- Respuestas ambiguas y contratos HTTP no uniformes para recursos inexistentes.

---

### 4.3 Carga Completa en Memoria para Reportes (ALTO)

**Ubicación:**

- [PositionReportService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionReportService.java#L63)
- [PositionReportService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionReportService.java#L73)
- [PositionReportService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionReportService.java#L77)

**Riesgo:**

- Con grandes volúmenes, aumenta latencia y uso de memoria.

**Remediación:**

- Implementar exportación por lotes/paginación/streaming.

---

## 5. VALIDACIONES Y SEGURIDAD DE ENTRADA

### 5.1 Contratos de Entrada Acoplados al Dominio (CRÍTICO)

**Ubicación:**

- [AreasController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/AreasController.java#L42)
- [SupplierController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/SupplierController.java#L37)
- [PositionAllowedRoleController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/PositionAllowedRoleController.java#L61)

**Detalle:**

- Se reciben entidades de dominio directamente en el request.
- No hay `@Valid` en endpoints.

---

### 5.2 Sin Mecanismo de Autorización de Campo Sensible (ALTO)

**Ubicación:**

- [PositionAllowedRoleService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionAllowedRoleService.java#L52)
- [SupplierService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/SupplierService.java#L50)

**Detalle:**

- Campos de auditoría/tenant pueden ser influenciados por payload o defaults no autenticados.

---

## 6. PROBLEMAS DE TESTING

### 6.1 Cobertura Insuficiente vs Tamaño del Servicio (CRÍTICO)

**Métricas:**

- Clases Java en `src/main/java`: **56**
- Tests en `src/test/java`: **4**

**Tests existentes:**

- [ConfigurationserviceApplicationTests.java](src/test/java/pe/edu/vallegrande/configurationservice/ConfigurationserviceApplicationTests.java)
- [ConnectionTest.java](src/test/java/pe/edu/vallegrande/configurationservice/ConnectionTest.java)
- [PositionServiceTest.java](src/test/java/pe/edu/vallegrande/configurationservice/PositionServiceTest.java)
- [SupplierServiceTest.java](src/test/java/pe/edu/vallegrande/configurationservice/SupplierServiceTest.java)

**Gap principal:**

- Sin cobertura representativa en controladores, manejo de errores, reportes y contratos HTTP.

---

## 7. PROBLEMAS DE CONFIGURACIÓN

### 7.1 Niveles de Log `DEBUG` Activos por Defecto (MEDIUM)

**Ubicación:**

- [application.yml](src/main/resources/application.yml#L23)
- [application.yml](src/main/resources/application.yml#L24)
- [application.yml](src/main/resources/application.yml#L25)

```yaml
org.springframework.r2dbc: DEBUG
org.springframework.data.r2dbc: DEBUG
reactor.core.publisher: DEBUG
```

**Riesgo:** ruido, costo I/O y posible exposición operacional innecesaria.

---

### 7.2 Scripts SQL Parciales para el Dominio Completo (MEDIUM)

**Ubicación de scripts existentes:**

- [configuracion_tenant.sql](src/main/resources/db/configuracion_tenant.sql)
- [position_allowed_roles.sql](src/main/resources/db/position_allowed_roles.sql)
- [supplier.sql](src/main/resources/db/supplier.sql)

**Detalle:**

- El servicio maneja más agregados que los cubiertos explícitamente en DDL disponible.

---

## 8. ANÁLISIS DE EXCEPCIONES

### 8.1 `GlobalExceptionHandler` Filtra Mensaje Interno en 500 (ALTO)

**Ubicación:** [GlobalExceptionHandler.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/config/GlobalExceptionHandler.java#L26)

```java
.body("Error interno del servidor: " + ex.getMessage());
```

**Riesgo:**

- Exposición de detalles internos a clientes externos.

**Remediación:**

- Responder con mensaje genérico al cliente.
- Log técnico interno con correlation ID.

---

### 8.2 Uso de `RuntimeException` Genérica en Servicios (MEDIUM)

**Ubicación:**

- [PositionAllowedRoleService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionAllowedRoleService.java#L27)
- [PositionAllowedRoleService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionAllowedRoleService.java#L63)
- [PositionAllowedRoleService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionAllowedRoleService.java#L70)
- [PositionAllowedRoleService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionAllowedRoleService.java#L84)

**Impacto:**

- Contratos de error inconsistentes y menor trazabilidad semántica.

---

## 9. BASE DE DATOS - R2DBC Y CONSISTENCIA

### 9.1 Restricciones en `suppliers` Bien Definidas (POSITIVO)

**Ubicación:**

- [supplier.sql](src/main/resources/db/supplier.sql#L38)
- [supplier.sql](src/main/resources/db/supplier.sql#L39)
- [supplier.sql](src/main/resources/db/supplier.sql#L40)

**Evaluación:** Constraints relevantes para calidad de dato.

---

### 9.2 `position_allowed_roles` con Restricción de Auditoría Correcta, Pero No Garantizada por Servicio (CRÍTICO)

**Ubicación:**

- [position_allowed_roles.sql](src/main/resources/db/position_allowed_roles.sql#L8)
- [PositionAllowedRoleService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionAllowedRoleService.java#L52)

**Evaluación:** El modelo SQL es correcto, la implementación de aplicación no lo asegura correctamente.

---

## 10. PUNTUACIÓN COMPONENTES

- Seguridad: **2.5/10**
- Arquitectura: **5.5/10**
- Datos y consistencia: **4.0/10**
- Reactividad y performance: **5.0/10**
- Testing y confiabilidad: **3.0/10**

**Puntaje final:** **3.8/10**

---

## 11. RECOMENDACIONES PRIORITARIAS

### P0 (CRÍTICO - Hacer ya)

1. Externalizar y rotar credenciales de BD.
2. Incorporar seguridad OAuth2/JWT en el microservicio.
3. Cerrar CORS a orígenes permitidos.
4. Eliminar `municipalityId` hardcodeado y resolverlo desde contexto autenticado.
5. Corregir `createdBy` server-side en `position_allowed_roles`.
6. Eliminar dependencia duplicada/incompatible de R2DBC.

### P1 (ALTO - Antes de salida productiva)

1. Introducir DTOs y `@Valid` en todos los controladores.
2. Estandarizar 404 con excepciones de dominio (`switchIfEmpty`).
3. Evitar exposición de `ex.getMessage()` en respuestas 500.
4. Reducir logging `DEBUG` por defecto.

### P2 (MEDIUM - Estabilización)

1. Replantear exportaciones PDF/Excel para alto volumen (streaming/paginación).
2. Completar scripts DDL para cobertura total del dominio.
3. Ampliar cobertura de tests de integración/controladores.

---

## 12. CONCLUSIÓN

`vg-ms-configurationservice` tiene una base funcional correcta para CRUD y reportes, pero no está listo para producción expuesta por debilidades críticas de seguridad y aislamiento multi-tenant. Al resolver P0 y P1, el servicio puede evolucionar a un estado estable y predecible para operación empresarial.

---

## APÉNDICE A: Archivos Críticos Auditados

- [pom.xml](pom.xml)
- [src/main/resources/application.yml](src/main/resources/application.yml)
- [src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/config/CorsConfig.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/config/CorsConfig.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/config/GlobalExceptionHandler.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/config/GlobalExceptionHandler.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/AreasController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/AreasController.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/SupplierController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/SupplierController.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/PositionAllowedRoleController.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/adapters/input/rest/PositionAllowedRoleController.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/application/service/AreaService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/AreaService.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/application/service/PhysicalLocationService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PhysicalLocationService.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/application/service/DocumentTypeService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/DocumentTypeService.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionAllowedRoleService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionAllowedRoleService.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/application/service/SupplierService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/SupplierService.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionService.java)
- [src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionReportService.java](src/main/java/pe/edu/vallegrande/configurationservice/application/service/PositionReportService.java)
- [src/main/resources/db/position_allowed_roles.sql](src/main/resources/db/position_allowed_roles.sql)
- [src/main/resources/db/supplier.sql](src/main/resources/db/supplier.sql)
- [src/main/resources/db/configuracion_tenant.sql](src/main/resources/db/configuracion_tenant.sql)
- [src/test/java/pe/edu/vallegrande/configurationservice/ConfigurationserviceApplicationTests.java](src/test/java/pe/edu/vallegrande/configurationservice/ConfigurationserviceApplicationTests.java)
- [src/test/java/pe/edu/vallegrande/configurationservice/ConnectionTest.java](src/test/java/pe/edu/vallegrande/configurationservice/ConnectionTest.java)
- [src/test/java/pe/edu/vallegrande/configurationservice/PositionServiceTest.java](src/test/java/pe/edu/vallegrande/configurationservice/PositionServiceTest.java)
- [src/test/java/pe/edu/vallegrande/configurationservice/SupplierServiceTest.java](src/test/java/pe/edu/vallegrande/configurationservice/SupplierServiceTest.java)
