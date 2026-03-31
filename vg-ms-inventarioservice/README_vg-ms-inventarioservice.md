# Auditoría Exhaustiva: vg-ms-inventarioservice

**Fecha de Auditoría:** Marzo 2026
**Versión Evaluada:** Spring Boot 3.5.7 + WebFlux + R2DBC
**Alcance:** Seguridad, Arquitectura, Persistencia, Patrones Reactivos, Testing
**Estado General:** 3.4/10 (Críticos bloqueantes identificados)

---

## 1. RESUMEN EJECUTIVO

El servicio de inventario físico (`vg-ms-inventarioservice`) implementa:

- Creación y gestión de inventarios físicos por tipo (`GENERAL`, `SELECTIVE`, `SPECIAL`)
- Generación automática de detalles por activos obtenidos de servicios externos
- Flujo operativo `PLANNED -> IN_PROCESS -> COMPLETED`
- Consolidación de datos de formularios (áreas, categorías, ubicaciones, usuarios)

**Hallazgos Críticos:** 8 problemas de seguridad/consistencia requieren corrección inmediata antes de producción expuesta. El servicio contiene secretos en repositorio, CORS permisivo con credenciales y controles de identidad delegados al cliente.

---

## 2. PROBLEMAS CRÍTICOS

### 2.1 Credenciales de Base de Datos en `application.yaml` (CRÍTICO - SEGURIDAD)

**Ubicación:**

- [src/main/resources/application.yaml](src/main/resources/application.yaml#L3)
- [src/main/resources/application.yaml](src/main/resources/application.yaml#L4)
- [src/main/resources/application.yaml](src/main/resources/application.yaml#L5)

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://ep-fragrant-cell-a4yrhw50-pooler.us-east-1.aws.neon.tech:5432/neondb?sslMode=require&channelBinding=require
    username: neondb_owner
    password: npg_yeJFA58BMUfS
```

**Riesgo:**

- Exposición directa de credenciales productivas/sensibles en el repositorio.

**Impacto de Producción:**

- Acceso no autorizado para lectura/escritura de inventarios y detalles.

**Remediación:**

```yaml
spring:
  r2dbc:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

- Rotar credenciales y eliminar secretos del historial.

---

### 2.2 CORS Abierto con Credenciales Habilitadas (CRÍTICO - SEGURIDAD)

**Ubicación:**

- [CorsConfig.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/CorsConfig.java#L15)
- [CorsConfig.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/CorsConfig.java#L16)
- [CorsConfig.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/CorsConfig.java#L17)
- [CorsConfig.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/CorsConfig.java#L18)

```java
config.setAllowCredentials(true);
config.addAllowedOriginPattern("*");
config.addAllowedHeader("*");
config.addAllowedMethod("*");
```

**Riesgo:**

- Configuración insegura: credenciales permitidas y wildcard de origen simultáneo.
- Amplía superficie para abuso de API desde cualquier origen web.

**Remediación:**

- Definir lista cerrada de orígenes por ambiente.
- Desactivar credenciales cuando exista wildcard.

---

### 2.3 Sin Capa de Seguridad In-Service y Identidad por Query Param (CRÍTICO - AUTORIZACIÓN)

**Ubicación:**

- Sin dependencias de seguridad en [pom.xml](pom.xml#L32)
- Endpoints sensibles recibiendo `userId` como parámetro:
  - [PhysicalInventoryController.java](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryController.java#L75)
  - [PhysicalInventoryController.java](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryController.java#L95)
  - [PhysicalInventoryController.java](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryController.java#L102)

**Riesgo:**

- Cualquier cliente que alcance el servicio puede suplantar identidad enviando un `userId` arbitrario.
- No hay controles de rol/tenant ni extracción de identidad desde token.

**Impacto de Producción:**

- Cancelaciones, inicio y cierre de inventarios por actores no autorizados.

**Remediación:**

- Incorporar OAuth2 Resource Server con JWT.
- Obtener actor desde contexto de seguridad, no desde query string.

---

### 2.4 Auditoría Manipulable por Cliente + Fallback a UUID Cero (CRÍTICO - TRAZABILIDAD)

**Ubicación:**

- Campo expuesto en DTO: [PhysicalInventoryDTO.java](src/main/java/pe/edu/vallegrande/ms_inventory/dto/PhysicalInventoryDTO.java#L28)
- Mapeo directo desde request: [PhysicalInventoryController.java](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryController.java#L62)
- Fallback interno: [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L84)
- UUID por defecto: [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L85)
- Constraint NOT NULL: [schema.sql](src/main/resources/db/schema.sql#L41)

```java
if (inventory.getCreatedBy() == null)
    inventory.setCreatedBy(UUID.fromString("00000000-0000-0000-0000-000000000000"));
```

**Riesgo:**

- `createdBy` puede ser spoofeado por cliente o quedar con valor técnico no atribuible.

**Remediación:**

- Derivar `createdBy` del token autenticado.
- Eliminar `createdBy` del payload de entrada.

---

### 2.5 Configuración R2DBC con `basePackages` Desalineado (CRÍTICO - ARRANQUE/REPOSITORIOS)

**Ubicación:**

- Escaneo configurado en [R2dbcConfig.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/R2dbcConfig.java#L23)
- Repositorios reales en paquete `infraestructure.repository`, por ejemplo [PhysicalInventoryRepository.java](src/main/java/pe/edu/vallegrande/ms_inventory/infraestructure/repository/PhysicalInventoryRepository.java#L1)

```java
@EnableR2dbcRepositories(basePackages = "pe.edu.vallegrande.ms_inventory.repository")
```

**Riesgo:**

- Paquete de escaneo no coincide con ubicación real de repositorios.
- Riesgo de errores de wiring o comportamiento no determinista según autoconfiguración.

**Remediación:**

- Corregir `basePackages` al paquete real (`...infraestructure.repository`) o eliminar configuración redundante.

---

### 2.6 Integraciones WebClient Sin `timeout/retry/onStatus` (CRÍTICO - DISPONIBILIDAD)

**Ubicación:**

- [AssetService.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/AssetService.java#L24)
- [ConfigurationService.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/ConfigurationService.java#L23)
- [UserService.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/UserService.java#L21)

**Riesgo:**

- Sin política de timeout/retry, fallas aguas abajo degradan este servicio.
- No hay mapeo explícito de estados HTTP de error.

**Hallazgo adicional:**

- Ruta sin slash inicial en [ConfigurationService.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/ConfigurationService.java#L38)

```java
.uri("api/v1/physical-locations")
```

Esto es inconsistente con el resto de URIs (`"/api/..."`) y puede causar resoluciones inesperadas.

---

### 2.7 `GlobalExceptionHandler` Expone Mensajes Internos (CRÍTICO - SEGURIDAD OPERACIONAL)

**Ubicación:** [GlobalExceptionHandler.java](src/main/java/pe/edu/vallegrande/ms_inventory/exception/GlobalExceptionHandler.java#L18)

```java
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
```

**Riesgo:**

- Fuga de detalles internos de negocio/infraestructura al cliente.

**Remediación:**

- Mensaje genérico al cliente.
- Log interno enriquecido con correlation ID.

---

### 2.8 Borrado Lógico de Detalle Marca Estado `DAMAGED` (CRÍTICO - SEMÁNTICA DE DATOS)

**Ubicación:** [PhysicalInventoryDetailServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryDetailServiceImpl.java#L99)

```java
existing.setFoundStatus("DAMAGED"); // Eliminación lógica
```

**Riesgo:**

- Se mezcla semántica de integridad de activo (`DAMAGED`) con semántica de ciclo de vida (`deleted/inactive`).
- Distorsiona reportes e indicadores operativos.

**Remediación:**

- Incorporar campo explícito de borrado lógico (`active` o `deletedAt`).
- Mantener `foundStatus` exclusivamente para estado físico del bien.

---

## 3. ANÁLISIS ARQUITECTURA

### 3.1 Estructura General (POSITIVO - Base Hexagonal Parcial)

Se observan puertos y adaptadores en la implementación:

- Puerto de persistencia: [PhysicalInventoryPersistencePort](src/main/java/pe/edu/vallegrande/ms_inventory/domain/port/out/PhysicalInventoryPersistencePort.java)
- Adaptador de persistencia: [PhysicalInventoryPersistenceAdapter.java](src/main/java/pe/edu/vallegrande/ms_inventory/infraestructure/repository/PhysicalInventoryPersistenceAdapter.java#L15)
- Casos de uso y servicio de aplicación: [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L32)

**Evaluación:** Buena intención arquitectónica, pero con fugas de capa.

---

### 3.2 Fugas de Capa Detectadas (MEDIUM)

**Evidencia:**

- Controlador usando puerto de salida directamente: [PhysicalInventoryDetailController.java](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryDetailController.java#L18)
- Servicio de aplicación acoplado a repositorio de infraestructura: [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L39)

**Impacto:**

- Aumenta acoplamiento y complica evolución/pruebas por interfaz de caso de uso.

---

### 3.3 Controladores REST (2 Controladores Identificados)

| Controlador | Seguridad | Hallazgo |
|---|---|---|
| [PhysicalInventoryController](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryController.java) | Sin guardas | `userId` en query param para acciones críticas |
| [PhysicalInventoryDetailController](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryDetailController.java) | Sin guardas | Payload directo de entidad sin validación |

---

### 3.4 Esquema SQL (POSITIVO - Restricciones Relevantes)

**Ubicación:** [schema.sql](src/main/resources/db/schema.sql)

**Fortalezas:**

- Restricciones de tipo/estado en inventarios:
  - [schema.sql](src/main/resources/db/schema.sql#L47)
  - [schema.sql](src/main/resources/db/schema.sql#L50)
- Restricción de estado en detalle:
  - [schema.sql](src/main/resources/db/schema.sql#L91)

---

## 4. PATRONES REACTIVOS (R2DBC + WebFlux)

### 4.1 Flujo Reactivo de Creación y Detalle (POSITIVO)

**Ubicación:** [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L90)

**Detalle:**

- Encadena guardado de cabecera + consulta de activos + creación de detalles en lotes.
- Uso de `buffer(50)` y concurrencia limitada:
  - [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L175)
  - [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L192)

---

### 4.2 Potencial Patrón N+1 en Listado con Detalles (ALTO)

**Ubicación:** [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L323)

**Detalle:**

- `findAll()` + `listByInventoryId(...)` por cada inventario.
- Puede degradar rendimiento con volumen alto.

---

### 4.3 Logging Muy Verboso en Flujos de Negocio (MEDIUM)

**Ubicación:**

- [application.yaml](src/main/resources/application.yaml#L17)
- [application.yaml](src/main/resources/application.yaml#L20)
- [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L402)

**Riesgo:**

- Ruido elevado, costos de I/O y exposición operacional.

---

## 5. VALIDACIONES Y SEGURIDAD DE ENTRADA

### 5.1 DTOs Sin Restricciones de Bean Validation (CRÍTICO)

**Ubicación:**

- [PhysicalInventoryDTO.java](src/main/java/pe/edu/vallegrande/ms_inventory/dto/PhysicalInventoryDTO.java#L11)
- [PhysicalInventoryDetailDTO.java](src/main/java/pe/edu/vallegrande/ms_inventory/dto/PhysicalInventoryDetailDTO.java#L10)

**Detalle:**

- No existen anotaciones `@NotNull`, `@Size`, `@Pattern`, etc.

---

### 5.2 Controladores Sin `@Valid` (CRÍTICO)

**Ubicación:**

- [PhysicalInventoryController.java](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryController.java#L43)
- [PhysicalInventoryDetailController.java](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryDetailController.java#L31)

**Impacto:**

- Reglas de entrada dependen exclusivamente de lógica manual en servicio.

---

### 5.3 Dependencia de Validación Incompleta (MEDIUM)

**Ubicación:** [pom.xml](pom.xml#L68)

**Detalle:**

- Se incluye `jakarta.validation-api`, pero no `spring-boot-starter-validation`.
- Riesgo de tener API sin proveedor integrado de validación en runtime.

---

## 6. PROBLEMAS DE TESTING

### 6.1 Cobertura Muy Baja vs Tamaño del Servicio (CRÍTICO)

**Métricas:**

- Clases Java en `src/main/java`: **35**
- Tests en `src/test/java`: **2**

**Tests existentes:**

- [PhysicalInventoryServiceImplTest.java](src/test/java/pe/edu/vallegrande/ms_inventory/PhysicalInventoryServiceImplTest.java)
- [MsInventoryApplicationTests.java](src/test/java/pe/edu/vallegrande/ms_inventory/MsInventoryApplicationTests.java)

**Cobertura observada:**

- Casos de negocio básicos en create/start/complete:
  - [PhysicalInventoryServiceImplTest.java](src/test/java/pe/edu/vallegrande/ms_inventory/PhysicalInventoryServiceImplTest.java#L43)
  - [PhysicalInventoryServiceImplTest.java](src/test/java/pe/edu/vallegrande/ms_inventory/PhysicalInventoryServiceImplTest.java#L77)
  - [PhysicalInventoryServiceImplTest.java](src/test/java/pe/edu/vallegrande/ms_inventory/PhysicalInventoryServiceImplTest.java#L134)

**Gap principal:**

- Sin pruebas de controladores, validaciones HTTP, mapeos de errores ni clientes externos.

---

## 7. PROBLEMAS DE CONFIGURACIÓN

### 7.1 Logging `DEBUG/TRACE` Activo por Defecto (MEDIUM)

**Ubicación:**

- [application.yaml](src/main/resources/application.yaml#L17)
- [application.yaml](src/main/resources/application.yaml#L18)
- [application.yaml](src/main/resources/application.yaml#L19)
- [application.yaml](src/main/resources/application.yaml#L20)

**Riesgo:**

- Elevado volumen de logs y mayor exposición operacional.

---

### 7.2 URLs Externas Compartidas Sin Segmentación de Dominio Técnico (MEDIUM)

**Ubicación:**

- [application.yaml](src/main/resources/application.yaml#L38)
- [application.yaml](src/main/resources/application.yaml#L40)
- [application.yaml](src/main/resources/application.yaml#L42)

**Detalle:**

- Múltiples dependencias apuntan a mismo host base; sin política de resiliencia por cliente.

---

## 8. ANÁLISIS DE EXCEPCIONES

### 8.1 Manejo Global 500 con Mensaje Crudo (ALTO)

**Ubicación:** [GlobalExceptionHandler.java](src/main/java/pe/edu/vallegrande/ms_inventory/exception/GlobalExceptionHandler.java#L18)

**Impacto:**

- Exposición de información interna al consumidor API.

---

### 8.2 Uso de `RuntimeException` Genérica en Flujos Operativos (MEDIUM)

**Ubicación:**

- [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L302)
- [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L309)
- [PhysicalInventoryDetailServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryDetailServiceImpl.java#L61)

**Impacto:**

- Menor precisión semántica de errores y contratos HTTP inconsistentes.

---

## 9. BASE DE DATOS - R2DBC Y CONSISTENCIA

### 9.1 Restricciones de Estado y Tipo Bien Definidas (POSITIVO)

**Ubicación:**

- [schema.sql](src/main/resources/db/schema.sql#L47)
- [schema.sql](src/main/resources/db/schema.sql#L50)
- [schema.sql](src/main/resources/db/schema.sql#L91)

---

### 9.2 Reglas de Auditoría SQL Correctas, pero Débiles en Aplicación (CRÍTICO)

**Ubicación:**

- [schema.sql](src/main/resources/db/schema.sql#L41)
- [PhysicalInventoryController.java](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryController.java#L62)
- [PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java#L85)

**Evaluación:**

- SQL exige `created_by`, pero la aplicación permite spoofing o fallback técnico no auditado.

---

## 10. PUNTUACIÓN COMPONENTES

- Seguridad: **2.0/10**
- Arquitectura: **5.0/10**
- Datos y consistencia: **4.0/10**
- Reactividad y performance: **5.0/10**
- Testing y confiabilidad: **2.5/10**

**Puntaje final:** **3.4/10**

---

## 11. RECOMENDACIONES PRIORITARIAS

### P0 (CRÍTICO - Hacer ya)

1. Externalizar y rotar credenciales de BD.
2. Corregir CORS (`allowCredentials` + wildcard) y cerrar orígenes.
3. Implementar OAuth2/JWT en el servicio.
4. Eliminar `userId` por query param; usar identidad de token.
5. Corregir `@EnableR2dbcRepositories(basePackages=...)`.
6. Blindar auditoría (`createdBy` server-side, no desde payload).
7. Añadir timeout/retry/onStatus en todos los `WebClient`.

### P1 (ALTO - Antes de salida productiva)

1. Agregar DTOs validados y `@Valid` en endpoints.
2. Reemplazar `RuntimeException` por excepciones de dominio.
3. Sanitizar respuestas 500 (sin `ex.getMessage()`).
4. Corregir semántica de borrado lógico en detalle.

### P2 (MEDIUM - Estabilización)

1. Reducir verbosidad de logs por ambiente.
2. Introducir pruebas de integración para controladores y clientes externos.
3. Optimizar patrón `listAllWithDetails` para evitar N+1.

---

## 12. CONCLUSIÓN

`vg-ms-inventarioservice` tiene una base funcional valiosa en su flujo reactivo de inventarios, pero requiere correcciones urgentes en seguridad, trazabilidad y resiliencia para estar listo para producción. La ejecución de P0 y P1 es obligatoria para minimizar riesgo operativo y de exposición de datos.

---

## APÉNDICE A: Archivos Críticos Auditados

- [pom.xml](pom.xml)
- [src/main/resources/application.yaml](src/main/resources/application.yaml)
- [src/main/resources/db/schema.sql](src/main/resources/db/schema.sql)
- [src/main/java/pe/edu/vallegrande/ms_inventory/config/CorsConfig.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/CorsConfig.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/config/R2dbcConfig.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/R2dbcConfig.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/config/AssetService.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/AssetService.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/config/ConfigurationService.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/ConfigurationService.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/config/UserService.java](src/main/java/pe/edu/vallegrande/ms_inventory/config/UserService.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/exception/GlobalExceptionHandler.java](src/main/java/pe/edu/vallegrande/ms_inventory/exception/GlobalExceptionHandler.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryController.java](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryController.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryDetailController.java](src/main/java/pe/edu/vallegrande/ms_inventory/controller/PhysicalInventoryDetailController.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryServiceImpl.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryDetailServiceImpl.java](src/main/java/pe/edu/vallegrande/ms_inventory/application/impl/PhysicalInventoryDetailServiceImpl.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/infraestructure/repository/PhysicalInventoryRepository.java](src/main/java/pe/edu/vallegrande/ms_inventory/infraestructure/repository/PhysicalInventoryRepository.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/infraestructure/repository/UserRepository.java](src/main/java/pe/edu/vallegrande/ms_inventory/infraestructure/repository/UserRepository.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/dto/PhysicalInventoryDTO.java](src/main/java/pe/edu/vallegrande/ms_inventory/dto/PhysicalInventoryDTO.java)
- [src/main/java/pe/edu/vallegrande/ms_inventory/dto/PhysicalInventoryDetailDTO.java](src/main/java/pe/edu/vallegrande/ms_inventory/dto/PhysicalInventoryDetailDTO.java)
- [src/test/java/pe/edu/vallegrande/ms_inventory/PhysicalInventoryServiceImplTest.java](src/test/java/pe/edu/vallegrande/ms_inventory/PhysicalInventoryServiceImplTest.java)
- [src/test/java/pe/edu/vallegrande/ms_inventory/MsInventoryApplicationTests.java](src/test/java/pe/edu/vallegrande/ms_inventory/MsInventoryApplicationTests.java)
