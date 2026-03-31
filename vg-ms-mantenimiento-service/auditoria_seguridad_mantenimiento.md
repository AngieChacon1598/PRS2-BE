# Informe de Seguridad: Brecha de Autorización Identificada en ms-maintenanceService

**Fecha:** 20 de Marzo de 2026  
**Servicio:** `ms-maintenanceService`  
**Estado:** ⚠️ Crítico (Nivel Lógico)

---

## 1. DESCRIPCIÓN DE LA BRECHA

Durante el análisis del microservicio de **Mantenimiento**, se ha identificado una vulnerabilidad de **Autorización Inconsistente** (Broken Object Level Authorization / BOLA). 

### Hallazgo:
El microservicio implementa correctamente la fase de **Autenticación** (verifica que el token JWT sea válido y consulta permisos en Redis), pero **falla en la fase de Autorización** (no exige esos permisos específicos para ejecutar cada acción).

### Ubicación del problema:
En el archivo [`MaintenanceController.java`](sipreb/vg-ms-mantenimiento-service/src/main/java/pe/edu/vallegrande/ms_maintenanceService/infrastructure/adapter/in/rest/MaintenanceController.java), los métodos **no tienen anotaciones de seguridad** (`@PreAuthorize`).

### Riesgo:
Cualquier usuario autenticado en el ecosistema (por ejemplo, un usuario con el rol más bajo de otro microservicio) puede realizar las siguientes acciones si conoce la URL:
*   **Crear** nuevos registros de mantenimiento (`POST /api/v1/maintenances`).
*   **Actualizar** o modificar cualquier mantenimiento existente (`PUT /api/v1/maintenances/{id}`).
*   **Cerrar/Completar** mantenimientos (`POST /api/v1/maintenances/{id}/complete`).

---

## 2. ANÁLISIS DE PERMISOS REALES (Base de Datos)

De acuerdo con la base de datos del módulo de mantenimiento, los permisos configurados son:

| Acción (DB) | Recurso (DB) | Permiso Spring (Authority) | Descripción |
| :--- | :--- | :--- | :--- |
| `read` | NULL | **`mantenimiento:read`** | Ver historial y consultas |
| `create` | NULL | **`mantenimiento:create`** | Programar nuevas órdenes |
| `update` | NULL | **`mantenimiento:update`** | Editar datos de mantenimiento |
| `close` | NULL | **`mantenimiento:close`** | Cerrar órdenes de mantenimiento |
| `alert` | `configure` | **`mantenimiento:alert:configure`** | Configurar alertas del sistema |

---

## 3. RECOMENDACIÓN DE MEJORA (+ IMPLEMENTACIÓN)

Basándonos en la arquitectura de **PBAC (Permission-Based Access Control)**, se recomienda mapear los endpoints del controlador a los permisos reales de la base de datos.

### Mapeo sugerido para `MaintenanceController.java`:

| Método Java | Operación HTTP | Autoridad Requerida |
| :--- | :--- | :--- |
| `findAll` / `findById` | GET `/maintenances/**` | `@PreAuthorize("hasAuthority('mantenimiento:read')")` |
| `create` | POST `/maintenances` | `@PreAuthorize("hasAuthority('mantenimiento:create')")` |
| `update` | PUT `/maintenances/{id}` | `@PreAuthorize("hasAuthority('mantenimiento:update')")` |
| `completeMaintenance` | POST `/maintenances/{id}/complete` | `@PreAuthorize("hasAuthority('mantenimiento:close')")` |
| `start`/`suspend`/`reschedule` | POST `/maintenances/...` | `@PreAuthorize("hasAuthority('mantenimiento:update')")` |

---

## 4. PRÓXIMOS PASOS RECOMENDADOS

1.  **Cerrar la Brecha:** Modificar el `MaintenanceController.java` añadiendo las anotaciones de la tabla anterior.
2.  **Validar Roles:** Asegurarse de que el rol de `MANTENIMIENTO_GESTOR` tenga asignados estos permisos en la base de datos corporativa.
3.  **Audit Logs:** Implementar un log que registre intentos de acceso fallidos (cuando alguien sin el permiso reciba un 403 Forbidden).

---
*Reporte actualizado con los permisos reales extraídos de la base de datos.*
