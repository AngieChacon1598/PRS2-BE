# Guía de Integración - Microservicio de Movimientos

## Propósito del Microservicio

Este microservicio gestiona los **movimientos de activos patrimoniales** entre responsables, áreas y ubicaciones dentro de una municipalidad.

## Estructura de Datos

### 1. Crear un Movimiento de Activo

**Endpoint:** `POST /api/v1/asset-movements`

El formulario del frontend está dividido en 4 secciones:

#### Sección 1: Información Básica
- **Activo** (REQUERIDO): Seleccionar el activo a mover
- **Tipo de Movimiento** (REQUERIDO): Tipo de movimiento a realizar
- **Subtipo de Movimiento** (REQUERIDO): Descripción específica del movimiento (ej: "TRANSFERENCIA_POR_ASCENSO")

#### Sección 2: Origen y Destino
- **Responsable Origen** (REQUERIDO): Usuario responsable actual del activo
- **Responsable Destino** (REQUERIDO): Usuario que recibirá el activo
- **Área Origen** (REQUERIDO): Área actual del activo
- **Área Destino** (REQUERIDO): Área a donde se moverá el activo
- **Ubicación Origen** (REQUERIDO): Ubicación física actual
- **Ubicación Destino** (REQUERIDO): Nueva ubicación física

#### Sección 3: Usuarios y Detalles
- **Usuario Solicitante** (REQUERIDO): Usuario que solicita el movimiento
- **Usuario Ejecutor** (OPCIONAL): Usuario que ejecutará el movimiento (se puede asignar después)
- **Motivo** (REQUERIDO): Descripción del motivo del movimiento (mínimo 10 caracteres)
- **Observaciones** (OPCIONAL): Observaciones adicionales (máximo 1000 caracteres)
- **Condiciones Especiales** (OPCIONAL): Condiciones especiales del movimiento (máximo 500 caracteres)

#### Sección 4: Documentación
- **Número de Documento de Soporte** (OPCIONAL): Número del documento que respalda el movimiento
- **Tipo de Documento de Soporte** (OPCIONAL): Tipo de documento (MEMORANDUM, OFICIO, RESOLUCION, etc.)
- **Archivos Adjuntos** (OPCIONAL): Hasta 10 archivos, 10MB cada uno (PDF, DOC, DOCX, JPG, PNG, XLS, XLSX)
- **Requiere Aprobación** (OPCIONAL): Checkbox - por defecto marcado (true)

**Request Body Completo:**
```json
{
  "municipalityId": "uuid",
  "assetId": "uuid",
  "movementType": "INITIAL_ASSIGNMENT | REASSIGNMENT | AREA_TRANSFER | RETURN | MAINTENANCE | REPAIR | TEMPORARY_DISPOSAL | EXTERNAL_TRANSFER | LOAN",
  "movementSubtype": "TRANSFERENCIA_POR_ASCENSO",
  "originResponsibleId": "uuid",
  "destinationResponsibleId": "uuid",
  "originAreaId": "uuid",
  "destinationAreaId": "uuid",
  "originLocationId": "uuid",
  "destinationLocationId": "uuid",
  "requestingUser": "uuid",
  "reason": "Motivo del movimiento (mínimo 10 caracteres)",
  "executingUser": "uuid",
  "observations": "Observaciones adicionales",
  "specialConditions": "Condiciones especiales",
  "supportingDocumentNumber": "DOC-001",
  "supportingDocumentType": "MEMORANDUM",
  "attachedDocuments": "[{\"fileName\":\"documento.pdf\",\"fileUrl\":\"https://storage.example.com/doc.pdf\"}]",
  "requiresApproval": true
}
```

**IMPORTANTE - Campos Obligatorios para el Frontend:**
1. `municipalityId` - ID del municipio (se obtiene del contexto del usuario)
2. `assetId` - ID del activo seleccionado
3. `movementType` - Tipo de movimiento seleccionado
4. `movementSubtype` - Subtipo ingresado (texto libre, máximo 50 caracteres)
5. `originResponsibleId` - Responsable de origen seleccionado
6. `destinationResponsibleId` - Responsable de destino seleccionado
7. `originAreaId` - Área de origen seleccionada
8. `destinationAreaId` - Área de destino seleccionada
9. `originLocationId` - Ubicación de origen seleccionada
10. `destinationLocationId` - Ubicación de destino seleccionada
11. `requestingUser` - Usuario que solicita (usuario actual del sistema)
12. `reason` - Motivo del movimiento (mínimo 10 caracteres, máximo 500)

**Campos Opcionales:**
- `movementNumber` - Se genera automáticamente en el backend (formato: MV-00001, MV-00002, etc.)
- `executingUser` - Usuario ejecutor (puede ser null inicialmente)
- `observations` - Observaciones (máximo 1000 caracteres)
- `specialConditions` - Condiciones especiales (máximo 500 caracteres)
- `supportingDocumentNumber` - Número de documento (máximo 50 caracteres)
- `supportingDocumentType` - Tipo de documento (texto libre, máximo 50 caracteres)
- `attachedDocuments` - Array JSON como string con archivos adjuntos
- `requiresApproval` - Boolean, por defecto true
- `requestDate` - Se asigna automáticamente en el backend con la fecha/hora actual
- `movementStatus` - Se asigna automáticamente como "REQUESTED" en el backend

**Response (201 Created):**
```json
{
  "id": "uuid",
  "municipalityId": "uuid",
  "movementNumber": "MV-00001",
  "assetId": "uuid",
  "movementType": "INITIAL_ASSIGNMENT",
  "movementSubtype": null,
  "originResponsibleId": "uuid",
  "destinationResponsibleId": "uuid",
  "originAreaId": "uuid",
  "destinationAreaId": "uuid",
  "originLocationId": "uuid",
  "destinationLocationId": "uuid",
  "requestDate": "2024-03-10T19:30:00",
  "approvalDate": null,
  "executionDate": null,
  "receptionDate": null,
  "movementStatus": "REQUESTED",
  "requiresApproval": true,
  "approvedBy": null,
  "reason": "Motivo del movimiento",
  "observations": "Observaciones adicionales",
  "specialConditions": null,
  "supportingDocumentNumber": "DOC-001",
  "supportingDocumentType": "MEMORANDUM",
  "attachedDocuments": "[]",
  "requestingUser": "uuid",
  "executingUser": null,
  "createdAt": "2024-03-10T19:30:00",
  "updatedAt": null,
  "active": true,
  "deletedBy": null,
  "deletedAt": null,
  "restoredBy": null,
  "restoredAt": null
}
```

---

## Flujo de Estados de un Movimiento

```
REQUESTED → APPROVED → COMPLETED
    ↓
REJECTED
    ↓
CANCELLED
```

### Estados Disponibles:

1. **REQUESTED**: Movimiento solicitado, pendiente de aprobación
2. **APPROVED**: Movimiento aprobado, listo para ejecutarse
3. **REJECTED**: Movimiento rechazado
4. **COMPLETED**: Movimiento completado exitosamente
5. **CANCELLED**: Movimiento cancelado

---

## Endpoints Principales

### Consultas

#### Obtener todos los movimientos de un municipio
```http
GET /api/v1/asset-movements/municipality/{municipalityId}
```

#### Obtener un movimiento específico
```http
GET /api/v1/asset-movements/{id}/municipality/{municipalityId}
```

#### Obtener movimientos por activo
```http
GET /api/v1/asset-movements/asset/{assetId}/municipality/{municipalityId}
```

#### Obtener movimientos por tipo
```http
GET /api/v1/asset-movements/type/{movementType}/municipality/{municipalityId}
```

#### Obtener movimientos por estado
```http
GET /api/v1/asset-movements/status/{status}/municipality/{municipalityId}
```

#### Obtener movimientos pendientes de aprobación
```http
GET /api/v1/asset-movements/pending-approval/municipality/{municipalityId}
```

---

### Operaciones de Estado

#### Aprobar un movimiento
```http
POST /api/v1/asset-movements/{id}/approve/municipality/{municipalityId}
Content-Type: application/json

{
  "approvedBy": "uuid"
}
```

#### Rechazar un movimiento
```http
POST /api/v1/asset-movements/{id}/reject/municipality/{municipalityId}
Content-Type: application/json

{
  "approvedBy": "uuid",
  "rejectionReason": "Motivo del rechazo"
}
```

#### Completar un movimiento
```http
POST /api/v1/asset-movements/{id}/complete/municipality/{municipalityId}
```

#### Cancelar un movimiento
```http
POST /api/v1/asset-movements/{id}/cancel/municipality/{municipalityId}
Content-Type: application/json

{
  "cancellationReason": "Motivo de cancelación"
}
```

---

### Operaciones CRUD

#### Actualizar un movimiento
```http
PUT /api/v1/asset-movements/{id}/municipality/{municipalityId}
Content-Type: application/json

{
  // Misma estructura que POST, todos los campos son opcionales
  // Solo se actualizan los campos enviados
}
```

#### Eliminar (soft delete) un movimiento
```http
DELETE /api/v1/asset-movements/{id}/municipality/{municipalityId}
Content-Type: application/json

{
  "deletedBy": "uuid"
}
```

#### Restaurar un movimiento eliminado
```http
POST /api/v1/asset-movements/{id}/restore/municipality/{municipalityId}
Content-Type: application/json

{
  "restoredBy": "uuid"
}
```

---

## Integración con Otros Microservicios

### Servicio de Patrimonio (MS-04)

Cuando un movimiento es **APROBADO** o **COMPLETADO**, este microservicio notifica automáticamente al servicio de patrimonio para actualizar:
- Estado del activo
- Responsable actual
- Área actual
- Ubicación actual


### Servicio de Usuarios (MS-02)

Se consulta para obtener información de usuarios (responsables, testigos) en las actas de entrega.


## Ejemplo de Flujo Completo para Transacción Distribuida

### Escenario: Transferir un activo entre áreas

```javascript
// 1. Crear el movimiento
POST /api/v1/asset-movements
{
  "municipalityId": "123e4567-e89b-12d3-a456-426614174000",
  "assetId": "987fcdeb-51a2-43d7-9876-543210fedcba",
  "movementType": "AREA_TRANSFER",
  "movementSubtype": "REORGANIZACION_ADMINISTRATIVA",
  "originResponsibleId": "111e4567-e89b-12d3-a456-426614174111",
  "destinationResponsibleId": "222e4567-e89b-12d3-a456-426614174222",
  "originAreaId": "333e4567-e89b-12d3-a456-426614174333",
  "destinationAreaId": "444e4567-e89b-12d3-a456-426614174444",
  "originLocationId": "aaa-4567-e89b-12d3-a456-426614174aaa",
  "destinationLocationId": "bbb-4567-e89b-12d3-a456-426614174bbb",
  "reason": "Transferencia por reorganización administrativa",
  "requiresApproval": true,
  "requestingUser": "555e4567-e89b-12d3-a456-426614174555"
}

// 2. Aprobar el movimiento
POST /api/v1/asset-movements/{movementId}/approve/municipality/{municipalityId}
{
  "approvedBy": "666e4567-e89b-12d3-a456-426614174666"
}

// 3. Completar el movimiento
POST /api/v1/asset-movements/{movementId}/complete/municipality/{municipalityId}
```

---

## Tipos de Movimiento y su Impacto

| Tipo de Movimiento | Descripción | Estado del Activo Resultante |
|-------------------|-------------|------------------------------|
| `INITIAL_ASSIGNMENT` | Asignación inicial del activo | `ASSIGNED` |
| `REASSIGNMENT` | Reasignación a otro responsable | `ASSIGNED` |
| `AREA_TRANSFER` | Transferencia entre áreas | `ASSIGNED` |
| `RETURN` | Devolución del activo | `AVAILABLE` |
| `MAINTENANCE` | Envío a mantenimiento | `MAINTENANCE` |
| `REPAIR` | Envío a reparación | `REPAIR` |
| `TEMPORARY_DISPOSAL` | Baja temporal | `BAJA_TEMPORAL` |
| `EXTERNAL_TRANSFER` | Transferencia externa | `ASSIGNED` |
| `LOAN` | Préstamo del activo | `IN_USE` |

---
