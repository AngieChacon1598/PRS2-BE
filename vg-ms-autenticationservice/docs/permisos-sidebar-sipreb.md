# 🔒 Sistema de Permisos y Sidebar — SIPREB
## Control de Acceso Multi-Capa para el Frontend

> **Basado en:** schema.sql · roles-sipreb.md · sidebar_structure.md · endpoints.md  
> **Normativa:** Ley N° 29151 · Directiva N° 001-2015/SBN · Ley N° 27972  
> **Última actualización:** 2025

---

## 📋 Índice

1. [Arquitectura de 3 capas](#1-arquitectura-de-3-capas)
2. [Evaluación de endpoints](#2-evaluación-de-endpoints)
3. [Problema con el sidebar actual](#3-problema-con-el-sidebar-actual)
4. [Solución — permisos de tipo sidebar](#4-solución-permisos-tipo-sidebar)
5. [Flujo completo de login a sidebar](#5-flujo-completo)
6. [Quién ve qué — mapeo de roles a módulos](#6-quién-ve-qué)
7. [Visibilidad vs Operación — diferencia clave](#7-visibilidad-vs-operación)
8. [Mapeo endpoints → permisos operativos](#8-mapeo-endpoints-permisos)
9. [SQL — seed de permissions](#9-sql-seed-permissions)
10. [SQL — seed de roles_permissions](#10-sql-seed-roles-permissions)
11. [SQL — seed de position_allowed_roles](#11-sql-seed-position-allowed-roles)
12. [Implementación en React](#12-implementación-en-react)

---

## 1. Arquitectura de 3 capas

El control de acceso en SIPREB funciona en tres niveles independientes. Cada capa tiene su propia fuente de verdad y su propio momento de evaluación.

```
┌─────────────────────────────────────────────────────────────────────┐
│  CAPA 1 — Frontend (Sidebar)                                        │
│  Endpoint real: GET /api/v1/assignments/users/{userId}/effective-   │
│                     permissions  (vg-ms-autenticationservice)       │
│  Evalúa: ¿el usuario VE este módulo en el menú?                     │
│  Permiso: sidebar:view:{resource}                                   │
└─────────────────────────┬───────────────────────────────────────────┘
                          │ request HTTP
┌─────────────────────────▼───────────────────────────────────────────┐
│  CAPA 2 — API Gateway                                               │
│  Endpoint: GET /health  (vg-ms-apigateway)                          │
│  Fuente: roles[] del JWT                                            │
│  Evalúa: ¿el usuario puede LLAMAR este endpoint?                    │
│  Ejemplo: PATRIMONIO_GESTOR → accede a /api/v1/assets/**            │
└─────────────────────────┬───────────────────────────────────────────┘
                          │ lógica interna
┌─────────────────────────▼───────────────────────────────────────────┐
│  CAPA 3 — Microservicio (operación puntual)                         │
│  Fuente: permissions[] del cache Redis (TTL 60s)                    │
│  Evalúa: ¿el usuario puede EJECUTAR esta acción?                    │
│  Ejemplo: patrimonio:baja → puede llamar PATCH /api/v1/assets/{id}  │
└─────────────────────────────────────────────────────────────────────┘
```

| Capa | Fuente real | Ejemplo concreto |
|---|---|---|
| Sidebar | `GET /api/v1/assignments/users/{userId}/effective-permissions` | `sidebar:view:inventarios` → mostrar módulo Inventarios |
| Rutas API | `roles[]` del JWT validado por el Gateway | `INVENTARIO_COORDINADOR` → accede a `/api/v1/inventories/**` |
| Operación | `permissions[]` cache Redis, resueltos por `GET /api/v1/permissions/user/{userId}` | `inventario:close` → puede llamar `PUT /inventories/{id}/complete` |

---

## 2. Evaluación de endpoints

### ✅ Lo que está bien

La separación de microservicios es correcta. Cada servicio es dueño de su dominio. El versionado `/api/v1/` es consistente. El endpoint `GET /api/v1/assignments/users/{userId}/effective-permissions` en `AssignmentController` es **exactamente** el que el frontend necesita para construir el sidebar — no hay que crear nada nuevo.

El `PositionAllowedRoleController` en `vg-ms-configurationservice` tiene todos los endpoints necesarios para el flujo de asignación de roles:

```
GET /api/v1/position-allowed-roles/position/{positionId}/municipality/{municipalityId}
    → filtra roles permitidos al crear un usuario en el frontend

GET /api/v1/position-allowed-roles/municipality/{municipalityId}/defaults
    → obtiene defaults para el seed de onboarding
```

### ⚠️ Observaciones y recomendaciones

**1. Inconsistencia de naming en Configuration Service**

Existen rutas duplicadas con verbos en la URL en `areas`, `physical-locations` y `categories-assets`:

```
✅ POST /api/v1/areas          ← REST estándar
❌ POST /api/v1/areas/create   ← verbo en la ruta (duplicado)

✅ PUT  /api/v1/areas/{id}     ← REST estándar
❌ PUT  /api/v1/areas/update/{id} ← verbo en la ruta (duplicado)

✅ GET  /api/v1/areas          ← lista todos
❌ GET  /api/v1/areas/GetAll   ← PascalCase + redundante
```

**Recomendación**: unificar al estándar REST. El frontend debe usar solo las rutas sin verbo para evitar confusión.

**2. `municipalityId` en la URL de Movimientos**

```
GET /api/v1/asset-movements/municipality/{municipalityId}
```

El `municipalityId` ya viene en el JWT como `municipal_code`. Recibirlo también en la URL permite que un usuario pase un ID diferente al suyo — aunque el Gateway filtre por JWT, es mejor que el microservicio extraiga el `municipal_code` del token directamente, sin aceptarlo en la URL.

**3. Endpoint de debug en producción**

```
POST /api/v1/users/{id}/validate-update   ← descripción: "Debug"
```

Si está expuesto en producción debe eliminarse o protegerse con rol `SUPER_ADMIN`. Los endpoints de debug son vectores de exposición de información interna.

**4. Endpoint de permisos — dos opciones disponibles**

Tienes dos endpoints que sirven para resolver permisos:

| Endpoint | Uso recomendado |
|---|---|
| `GET /api/v1/assignments/users/{userId}/effective-permissions` | Frontend post-login para construir el sidebar |
| `GET /api/v1/permissions/user/{userId}` | Consulta granular desde microservicios para validar operaciones |

El primero devuelve **permisos efectivos** (resueltos a través de los roles asignados). El segundo lista permisos directamente. Para el sidebar usar siempre el primero.

---

El sidebar actual usa solo dos roles: `SUPER_ADMIN` y `ADMIN`. Esto es demasiado grueso para el contexto municipal.

```javascript
// ❌ Actual — todos los que tienen ADMIN ven lo mismo
{ label: 'Bienes',        requiredRole: 'ADMIN' }
{ label: 'Inventarios',   requiredRole: 'ADMIN' }
{ label: 'Mantenimientos',requiredRole: 'ADMIN' }
```

Con esta lógica, un **Almacenero** de la UAL ve el módulo de Auditoría. Un **Técnico de Inventario** ve el módulo de Usuarios y Permisos. Un **Asesor Legal** ve el módulo de Mantenimientos. Ninguno de esos accesos tiene sentido operativo.

La solución no es agregar más roles al sidebar — es hacer que el sidebar consulte los **permisos reales** del usuario.

---

## 3. Problema con el sidebar actual

El sidebar actual usa solo dos roles: `SUPER_ADMIN` y `ADMIN`. Esto es demasiado grueso para el contexto municipal.

```javascript
// ❌ Actual — todos los que tienen ADMIN ven lo mismo
{ label: 'Bienes',         requiredRole: 'ADMIN' }
{ label: 'Inventarios',    requiredRole: 'ADMIN' }
{ label: 'Mantenimientos', requiredRole: 'ADMIN' }
```

Con esta lógica, un **Almacenero** de la UAL ve el módulo de Auditoría. Un **Técnico de Inventario** ve el módulo de Usuarios y Permisos. Un **Asesor Legal** ve el módulo de Mantenimientos. Ninguno de esos accesos tiene sentido operativo.

La solución no es agregar más roles al sidebar — es hacer que el sidebar consulte los **permisos reales** del usuario usando el endpoint ya existente: `GET /api/v1/assignments/users/{userId}/effective-permissions`.

---

## 4. Solución — permisos tipo `sidebar`

Se agrega un tipo de permiso especial en la tabla `permissions` usando `module = 'sidebar'` y `action = 'view'`. El `resource` es el identificador del módulo del sidebar.

```
module    action    resource         → significado
────────────────────────────────────────────────────
sidebar   view      bienes           → ver módulo Bienes
sidebar   view      inventarios      → ver módulo Inventarios
sidebar   view      auditoria        → ver módulo Auditoría
sidebar   view      admin            → ver módulo Administración Global
```

Estos permisos se asignan a los roles en `roles_permissions`, igual que cualquier otro permiso del sistema. El frontend consulta una sola vez qué permisos tiene el usuario y construye el sidebar dinámicamente.

### Separación visibilidad / operación

Un mismo módulo puede tener dos tipos de permisos asociados:

```
sidebar:view:movimientos     → VE el módulo en el menú
movimientos:read             → LISTA movimientos
movimientos:create           → CREA solicitudes
movimientos:approve          → APRUEBA (solo MOVIMIENTOS_APROBADOR)
```

Un **Técnico Patrimonial** tiene `sidebar:view:movimientos` + `movimientos:read` + `movimientos:create` pero **no** tiene `movimientos:approve`. Ve el módulo, puede crear solicitudes, pero el botón "Aprobar" no aparece en su interfaz.

---

## 5. Flujo completo

```
Usuario hace login
        ↓
POST /api/v1/auth/login { username, password, municipal_code }
  (vg-ms-autenticationservice → AuthController)
        ↓
JWT emitido: { user_id, municipal_code, roles: ["PATRIMONIO_GESTOR"], ... }
        ↓
GET /api/v1/assignments/users/{user_id}/effective-permissions
  (vg-ms-autenticationservice → AssignmentController)
        ↓ Auth Service consulta Redis
        ↓ (si no hay cache → consulta BD → guarda en Redis TTL 60s)
        ↓
{
  permissions: [
    "sidebar:view:bienes",
    "sidebar:view:movimientos",
    "sidebar:view:inventarios",
    "patrimonio:create",
    "patrimonio:read",
    "patrimonio:update",
    "movimientos:create",
    "movimientos:read:own",
    "inventario:read:active",
    "inventario:verify:item",
    ...
  ]
}
        ↓
Frontend guarda permissions[] en el store (Zustand / Redux / Context)
        ↓
sidebarConfig.js filtra módulos con canView('bienes'), canView('admin'), etc.
        ↓
Sidebar renderizado solo con los módulos permitidos para ese usuario
```

---

## 6. Quién ve qué

Mapeo completo entre los módulos del sidebar y los roles que tienen permiso de verlos.

### Módulo 1 — Dashboard `/`
Visible para **todos** los usuarios autenticados. No requiere permiso específico de sidebar.

### Módulo 2 — Administración Global
Solo `SUPER_ADMIN`. No visible para ningún usuario de municipalidad.

| Sección | Permiso requerido | Roles |
|---|---|---|
| Municipalidades | `sidebar:view:admin` | `SUPER_ADMIN` |
| Suscripciones | `sidebar:view:admin` | `SUPER_ADMIN` |
| Facturación | `sidebar:view:admin` | `SUPER_ADMIN` |
| Onboarding | `sidebar:view:admin` | `SUPER_ADMIN` |

### Módulo 3 — Gestión de Activos

| Sección | Permiso requerido | Roles que lo tienen |
|---|---|---|
| Bienes | `sidebar:view:bienes` | `PATRIMONIO_GESTOR`, `PATRIMONIO_OPERARIO`, `PATRIMONIO_VIEWER`, `TENANT_ADMIN` |
| Categorías | `sidebar:view:categorias` | `PATRIMONIO_GESTOR`, `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| Proveedores | `sidebar:view:proveedores` | `PATRIMONIO_GESTOR`, `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| Ubicaciones Físicas | `sidebar:view:ubicaciones` | `PATRIMONIO_GESTOR`, `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |

### Módulo 4 — Operaciones

| Sección | Permiso requerido | Roles que lo tienen |
|---|---|---|
| Movimientos | `sidebar:view:movimientos` | `MOVIMIENTOS_SOLICITANTE`, `MOVIMIENTOS_APROBADOR`, `MOVIMIENTOS_VIEWER`, `PATRIMONIO_GESTOR`, `TENANT_ADMIN` |
| Actas de Entrega | `sidebar:view:actas` | `MOVIMIENTOS_APROBADOR`, `TENANT_ADMIN` |
| Inventarios | `sidebar:view:inventarios` | `INVENTARIO_COORDINADOR`, `INVENTARIO_VERIFICADOR`, `PATRIMONIO_GESTOR`, `TENANT_ADMIN` |
| Mantenimientos | `sidebar:view:mantenimientos` | `MANTENIMIENTO_GESTOR`, `MANTENIMIENTO_VIEWER`, `TENANT_ADMIN` |

### Módulo 5 — Usuarios y Seguridad

| Sección | Permiso requerido | Roles que lo tienen |
|---|---|---|
| Usuarios | `sidebar:view:usuarios` | `TENANT_ADMIN` |
| Personas | `sidebar:view:personas` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| Roles | `sidebar:view:roles` | `TENANT_ADMIN` |
| Permisos | `sidebar:view:permisos` | `TENANT_ADMIN` |
| Áreas | `sidebar:view:areas` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| Cargos | `sidebar:view:cargos` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |

### Módulo 6 — Contabilidad

| Sección | Permiso requerido | Roles que lo tienen |
|---|---|---|
| Bajas de Activos | `sidebar:view:bajas` | `PATRIMONIO_GESTOR`, `TENANT_ADMIN` |
| Historial de Valores | `sidebar:view:valores` | `PATRIMONIO_GESTOR`, `AUDITORIA_VIEWER`, `TENANT_ADMIN` |

### Módulo 7 — Reportes y Auditoría

| Sección | Permiso requerido | Roles que lo tienen |
|---|---|---|
| Reportes | `sidebar:view:reportes` | `REPORTES_VIEWER`, `REPORTES_SCHEDULER`, `TENANT_ADMIN` |
| Auditoría | `sidebar:view:auditoria` | `AUDITORIA_VIEWER`, `TENANT_ADMIN` |
| Notificaciones | `sidebar:view:notificaciones` | Todos los roles autenticados de municipalidad |

### Módulo 8 — Configuración

| Sección | Permiso requerido | Roles que lo tienen |
|---|---|---|
| Sistema | `sidebar:view:sistema` | `TENANT_ADMIN` |

---

## 7. Visibilidad vs Operación — diferencia clave

El permiso `sidebar:view:*` solo controla si aparece el módulo en el menú. Dentro de cada pantalla, los botones y acciones se controlan con los permisos operativos.

### Ejemplo — módulo Movimientos

```
Usuario: Técnico Patrimonial (PATRIMONIO_GESTOR + MOVIMIENTOS_SOLICITANTE)

Sidebar: ✅ Ve el módulo Movimientos      ← sidebar:view:movimientos
Lista:   ✅ Ve todos los movimientos      ← movimientos:read
Crear:   ✅ Puede crear solicitud         ← movimientos:create
Aprobar: ❌ Botón deshabilitado/oculto    ← NO tiene movimientos:approve
Actas:   ❌ No ve el módulo Actas         ← NO tiene sidebar:view:actas
```

### Ejemplo — módulo Inventarios

```
Usuario: Miembro Verificador CI (INVENTARIO_VERIFICADOR)
— asignado temporalmente con expiration_date

Sidebar: ✅ Ve el módulo Inventarios      ← sidebar:view:inventarios
Ver activos: ✅ Solo inventarios activos  ← inventario:read:active
Verificar:   ✅ Puede marcar ítems        ← inventario:verify:item
Crear:       ❌ No puede crear inventario ← NO tiene inventario:create
Cerrar:      ❌ No puede cerrar           ← NO tiene inventario:close
```

### Ejemplo — módulo Usuarios y Seguridad

```
Usuario: TENANT_CONFIG_MANAGER

Sidebar: ✅ Ve Personas, Áreas, Cargos    ← sidebar:view:personas/areas/cargos
         ❌ NO ve Usuarios, Roles, Permisos ← NO tiene sidebar:view:usuarios/roles/permisos
Editar:  ✅ Puede editar áreas y cargos   ← config:areas:manage / config:categories:manage
Crear usuario: ❌ No puede               ← NO tiene users:manage
```

---

## 8. Mapeo endpoints → permisos operativos

Relación entre cada endpoint real y el permiso del sistema que lo protege. El API Gateway valida el rol del JWT (capa 2); el microservicio valida el permiso puntual (capa 3).

### vg-ms-autenticationservice

| Método | Endpoint | Permiso requerido | Rol mínimo |
|---|---|---|---|
| POST | `/api/v1/auth/login` | público | — |
| POST | `/api/v1/auth/refresh` | público | — |
| POST | `/api/v1/auth/logout` | autenticado | cualquiera |
| POST | `/api/v1/auth/validate` | autenticado | cualquiera |
| GET | `/api/v1/assignments/users/{userId}/effective-permissions` | autenticado | cualquiera (solo propio userId) |
| GET | `/api/v1/assignments/users/{userId}/roles` | `users:manage` | `TENANT_ADMIN` |
| POST | `/api/v1/assignments/users/{userId}/roles/{roleId}` | `roles:assign` | `TENANT_ADMIN` |
| DELETE | `/api/v1/assignments/users/{userId}/roles/{roleId}` | `roles:assign` | `TENANT_ADMIN` |
| GET | `/api/v1/assignments/roles/{roleId}/permissions` | `roles:manage` | `TENANT_ADMIN` |
| POST | `/api/v1/assignments/roles/{roleId}/permissions/{permissionId}` | `roles:manage` | `TENANT_ADMIN` |
| DELETE | `/api/v1/assignments/roles/{roleId}/permissions/{permissionId}` | `roles:manage` | `TENANT_ADMIN` |
| POST | `/api/v1/permissions` | `roles:manage` | `TENANT_ADMIN` |
| GET | `/api/v1/permissions` | `roles:manage` | `TENANT_ADMIN` |
| GET | `/api/v1/permissions/user/{userId}` | autenticado | cualquiera (solo propio userId) |
| PUT | `/api/v1/permissions/{id}` | `roles:manage` | `TENANT_ADMIN` |
| DELETE | `/api/v1/permissions/{id}` | `roles:manage` | `TENANT_ADMIN` |
| POST | `/api/v1/roles` | `roles:manage` | `TENANT_ADMIN` |
| GET | `/api/v1/roles` | `roles:manage` | `TENANT_ADMIN` |
| PUT | `/api/v1/roles/{id}` | `roles:manage` | `TENANT_ADMIN` |
| DELETE | `/api/v1/roles/{id}` | `roles:manage` | `TENANT_ADMIN` |
| POST | `/api/v1/persons` | `users:manage` | `TENANT_ADMIN` |
| GET | `/api/v1/persons` | `users:manage` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| PUT | `/api/v1/persons/{id}` | `users:manage` | `TENANT_ADMIN` |
| DELETE | `/api/v1/persons/{id}` | `users:manage` | `TENANT_ADMIN` |
| POST | `/api/v1/users` | `users:manage` | `TENANT_ADMIN` |
| GET | `/api/v1/users` | `users:manage` | `TENANT_ADMIN` |
| PUT | `/api/v1/users/{id}` | `users:manage` | `TENANT_ADMIN` |
| DELETE | `/api/v1/users/{id}` | `users:manage` | `TENANT_ADMIN` |
| PATCH | `/api/v1/users/{id}/block` | `users:manage` | `TENANT_ADMIN` |
| PATCH | `/api/v1/users/{id}/suspend` | `users:manage` | `TENANT_ADMIN` |
| PATCH | `/api/v1/users/{id}/unblock` | `users:manage` | `TENANT_ADMIN` |
| POST | `/api/v1/users/sync` | `users:manage` | `SUPER_ADMIN` |
| POST | `/api/v1/users/onboarding` | `users:manage` | `SUPER_ADMIN`, `ONBOARDING_MANAGER` |
| POST | `/api/v1/users/{id}/validate-update` | ⚠️ **Eliminar o proteger** | `SUPER_ADMIN` |

### vg-ms-configurationservice

| Método | Endpoint | Permiso requerido | Rol mínimo |
|---|---|---|---|
| GET | `/api/v1/areas` | `config:read` | `TENANT_CONFIG_MANAGER`, `TENANT_ADMIN` |
| POST | `/api/v1/areas` ✅ | `config:areas:manage` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| ~~POST~~ | ~~`/api/v1/areas/create`~~ ❌ | duplicado — usar `POST /areas` | — |
| PUT | `/api/v1/areas/{id}` ✅ | `config:areas:manage` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| ~~PUT~~ | ~~`/api/v1/areas/update/{id}`~~ ❌ | duplicado — usar `PUT /areas/{id}` | — |
| DELETE | `/api/v1/areas/{id}` | `config:areas:manage` | `TENANT_ADMIN` |
| GET | `/api/v1/positions` | `config:read` | `TENANT_CONFIG_MANAGER`, `TENANT_ADMIN` |
| POST | `/api/v1/positions` | `config:areas:manage` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| PUT | `/api/v1/positions/{id}` | `config:areas:manage` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| GET | `/api/v1/suppliers` | `config:read` | `PATRIMONIO_GESTOR`, `TENANT_ADMIN` |
| POST | `/api/v1/suppliers` | `config:areas:manage` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| GET | `/api/v1/physical-locations` | `config:read` | `PATRIMONIO_GESTOR`, `TENANT_ADMIN` |
| POST | `/api/v1/physical-locations` ✅ | `config:locations:manage` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| ~~POST~~ | ~~`/api/v1/physical-locations/create`~~ ❌ | duplicado | — |
| GET | `/api/v1/categories-assets` | `config:read` | `PATRIMONIO_GESTOR`, `TENANT_ADMIN` |
| POST | `/api/v1/categories-assets/create` | `config:categories:manage` | `TENANT_ADMIN`, `TENANT_CONFIG_MANAGER` |
| GET | `/api/v1/position-allowed-roles/position/{positionId}/municipality/{municipalityId}` | autenticado | cualquiera |
| POST | `/api/v1/position-allowed-roles` | `roles:manage` | `TENANT_ADMIN` |

### vg-ms-patrimonioservice

| Método | Endpoint | Permiso requerido | Rol mínimo |
|---|---|---|---|
| POST | `/api/v1/assets` | `patrimonio:create` | `PATRIMONIO_GESTOR`, `PATRIMONIO_OPERARIO` |
| GET | `/api/v1/assets` | `patrimonio:read` | todos los roles de patrimonio |
| PUT | `/api/v1/assets/{id}` | `patrimonio:update` | `PATRIMONIO_GESTOR` |
| DELETE | `/api/v1/assets/{id}` | `patrimonio:delete` | `PATRIMONIO_GESTOR` |
| PATCH | `/api/v1/assets/{id}/status` | `patrimonio:update:status` | `PATRIMONIO_GESTOR`, `PATRIMONIO_OPERARIO` |
| POST | `/api/v1/depreciations` | `patrimonio:depreciation` | `PATRIMONIO_GESTOR` |
| PATCH | `/api/v1/depreciations/{id}/approve` | `patrimonio:depreciation` | `PATRIMONIO_GESTOR` |
| POST | `/api/v1/asset-disposals` | `patrimonio:baja` | `PATRIMONIO_GESTOR` |
| GET | `/api/v1/asset-disposals` | `patrimonio:read` | `PATRIMONIO_GESTOR`, `AUDITORIA_VIEWER` |
| PATCH | `/api/v1/asset-disposals/{id}/finalize` | `patrimonio:baja` | `PATRIMONIO_GESTOR` |
| PUT | `/api/v1/asset-disposal-details/{id}/technical-opinion` | `patrimonio:baja` | `PATRIMONIO_GESTOR` |

### vg-ms-movementservice

| Método | Endpoint | Permiso requerido | Rol mínimo |
|---|---|---|---|
| POST | `/api/v1/asset-movements` | `movimientos:create` | `MOVIMIENTOS_SOLICITANTE` |
| GET | `/api/v1/asset-movements/municipality/{municipalityId}` | `movimientos:read` | `MOVIMIENTOS_APROBADOR`, `MOVIMIENTOS_VIEWER` |
| GET | `/api/v1/asset-movements/pending-approval/municipality/{municipalityId}` | `movimientos:read` | `MOVIMIENTOS_APROBADOR` |
| POST | `/api/v1/asset-movements/{id}/approve/municipality/{municipalityId}` | `movimientos:approve` | `MOVIMIENTOS_APROBADOR` |
| POST | `/api/v1/asset-movements/{id}/reject/municipality/{municipalityId}` | `movimientos:reject` | `MOVIMIENTOS_APROBADOR` |
| POST | `/api/v1/asset-movements/{id}/complete/municipality/{municipalityId}` | `movimientos:approve` | `MOVIMIENTOS_APROBADOR` |
| POST | `/api/v1/handover-receipts/municipality/{municipalityId}` | `movimientos:acta:generate` | `MOVIMIENTOS_APROBADOR` |
| POST | `/api/v1/handover-receipts/{id}/sign/municipality/{municipalityId}` | `movimientos:acta:generate` | `MOVIMIENTOS_APROBADOR` |

### vg-ms-inventarioservice

| Método | Endpoint | Permiso requerido | Rol mínimo |
|---|---|---|---|
| POST | `/api/v1/inventories` | `inventario:create` | `INVENTARIO_COORDINADOR` |
| GET | `/api/v1/inventories` | `inventario:read` | `INVENTARIO_COORDINADOR` |
| GET | `/api/v1/inventories/with-details` | `inventario:read` | `INVENTARIO_COORDINADOR`, `INVENTARIO_VERIFICADOR` |
| PUT | `/api/v1/inventories/{id}/start` | `inventario:update` | `INVENTARIO_COORDINADOR` |
| PUT | `/api/v1/inventories/{id}/complete` | `inventario:close` | `INVENTARIO_COORDINADOR` |
| POST | `/api/v1/inventory-details` | `inventario:verify:item` | `INVENTARIO_VERIFICADOR` |
| PUT | `/api/v1/inventory-details/{id}` | `inventario:verify:item` | `INVENTARIO_VERIFICADOR` |

### vg-ms-mantenimiento-service

| Método | Endpoint | Permiso requerido | Rol mínimo |
|---|---|---|---|
| POST | `/api/v1/maintenances` | `mantenimiento:create` | `MANTENIMIENTO_GESTOR` |
| GET | `/api/v1/maintenances` | `mantenimiento:read` | `MANTENIMIENTO_GESTOR`, `MANTENIMIENTO_VIEWER` |
| PUT | `/api/v1/maintenances/{id}` | `mantenimiento:update` | `MANTENIMIENTO_GESTOR` |
| POST | `/api/v1/maintenances/{id}/start` | `mantenimiento:update` | `MANTENIMIENTO_GESTOR` |
| POST | `/api/v1/maintenances/{id}/complete` | `mantenimiento:close` | `MANTENIMIENTO_GESTOR` |
| POST | `/api/v1/maintenances/{id}/cancel` | `mantenimiento:update` | `MANTENIMIENTO_GESTOR` |
| POST | `/api/v1/maintenances/{id}/reschedule` | `mantenimiento:alert:configure` | `MANTENIMIENTO_GESTOR` |

### vg-ms-tenantmanagmentservice

| Método | Endpoint | Permiso requerido | Rol mínimo |
|---|---|---|---|
| GET | `/api/v1/municipalities` | autenticado | `SUPER_ADMIN`, `ONBOARDING_MANAGER` |
| POST | `/api/v1/municipalities/register` | `users:manage` | `SUPER_ADMIN`, `ONBOARDING_MANAGER` |
| PUT | `/api/v1/municipalities/{id}` | `users:manage` | `SUPER_ADMIN` |
| DELETE | `/api/v1/municipalities/{id}` | `users:manage` | `SUPER_ADMIN` |

---

## 9. SQL — seed de permissions

Script para insertar todos los permisos en la tabla `permissions`. Los `municipal_code` y `created_by` usan valores de ejemplo — reemplazar con los UUIDs reales en el seed de onboarding.

```sql
-- ================================================================
-- SEED: permissions
-- Ejecutar durante el onboarding de cada nuevo tenant
-- ================================================================

-- Variables de referencia (reemplazar con UUIDs reales)
-- :municipal_code = UUID del tenant
-- :system_user_id = UUID del usuario sistema que hace el seed

-- ────────────────────────────────────────────────────────────────
-- PERMISOS DE SIDEBAR (visibilidad del menú lateral)
-- ────────────────────────────────────────────────────────────────
INSERT INTO permissions (module, action, resource, display_name, description, municipal_code, created_by) VALUES

-- Administración Global (solo SUPER_ADMIN — se inserta en master_tenant)
('sidebar', 'view', 'admin',          'Ver Administración Global',    'Acceso al módulo de administración de la plataforma',        :municipal_code, :system_user_id),

-- Gestión de Activos
('sidebar', 'view', 'bienes',         'Ver Bienes',                   'Acceso al catálogo de bienes patrimoniales',                  :municipal_code, :system_user_id),
('sidebar', 'view', 'categorias',     'Ver Categorías',               'Acceso al módulo de categorías de bienes',                    :municipal_code, :system_user_id),
('sidebar', 'view', 'proveedores',    'Ver Proveedores',              'Acceso al módulo de proveedores',                             :municipal_code, :system_user_id),
('sidebar', 'view', 'ubicaciones',    'Ver Ubicaciones Físicas',      'Acceso al módulo de ubicaciones físicas',                     :municipal_code, :system_user_id),

-- Operaciones
('sidebar', 'view', 'movimientos',    'Ver Movimientos',              'Acceso al módulo de movimientos de bienes',                   :municipal_code, :system_user_id),
('sidebar', 'view', 'actas',          'Ver Actas de Entrega',         'Acceso al módulo de actas de entrega-recepción',              :municipal_code, :system_user_id),
('sidebar', 'view', 'inventarios',    'Ver Inventarios',              'Acceso al módulo de inventarios físicos',                     :municipal_code, :system_user_id),
('sidebar', 'view', 'mantenimientos', 'Ver Mantenimientos',           'Acceso al módulo de mantenimiento de bienes',                 :municipal_code, :system_user_id),

-- Usuarios y Seguridad
('sidebar', 'view', 'usuarios',       'Ver Usuarios',                 'Acceso al módulo de gestión de usuarios',                     :municipal_code, :system_user_id),
('sidebar', 'view', 'personas',       'Ver Personas',                 'Acceso al módulo de personas',                                :municipal_code, :system_user_id),
('sidebar', 'view', 'roles',          'Ver Roles',                    'Acceso al módulo de roles del sistema',                       :municipal_code, :system_user_id),
('sidebar', 'view', 'permisos',       'Ver Permisos',                 'Acceso al módulo de permisos del sistema',                    :municipal_code, :system_user_id),
('sidebar', 'view', 'areas',          'Ver Áreas',                    'Acceso al módulo de áreas organizacionales',                  :municipal_code, :system_user_id),
('sidebar', 'view', 'cargos',         'Ver Cargos',                   'Acceso al módulo de cargos',                                  :municipal_code, :system_user_id),

-- Contabilidad
('sidebar', 'view', 'bajas',          'Ver Bajas de Activos',         'Acceso al módulo de bajas de bienes patrimoniales',           :municipal_code, :system_user_id),
('sidebar', 'view', 'valores',        'Ver Historial de Valores',     'Acceso al historial de valorización de bienes',               :municipal_code, :system_user_id),

-- Reportes y Auditoría
('sidebar', 'view', 'reportes',       'Ver Reportes',                 'Acceso al módulo de reportes regulatorios',                   :municipal_code, :system_user_id),
('sidebar', 'view', 'auditoria',      'Ver Auditoría',                'Acceso al módulo de auditoría y trazabilidad',                :municipal_code, :system_user_id),
('sidebar', 'view', 'notificaciones', 'Ver Notificaciones',           'Acceso al módulo de alertas y notificaciones',                :municipal_code, :system_user_id),

-- Configuración
('sidebar', 'view', 'sistema',        'Ver Configuración del Sistema','Acceso a la configuración general del tenant',                :municipal_code, :system_user_id),

-- ────────────────────────────────────────────────────────────────
-- PERMISOS OPERATIVOS — PATRIMONIO
-- ────────────────────────────────────────────────────────────────
('patrimonio', 'create',      NULL,     'Registrar bien',             'Crear nuevos bienes patrimoniales',                           :municipal_code, :system_user_id),
('patrimonio', 'read',        NULL,     'Ver bienes',                 'Listar y consultar el catálogo de bienes',                    :municipal_code, :system_user_id),
('patrimonio', 'update',      NULL,     'Editar bien',                'Actualizar datos de bienes patrimoniales',                    :municipal_code, :system_user_id),
('patrimonio', 'update',      'status', 'Actualizar estado del bien', 'Cambiar el estado de un bien (en uso, en almacén, etc.)',     :municipal_code, :system_user_id),
('patrimonio', 'delete',      NULL,     'Eliminar bien',              'Eliminar bienes del catálogo (solo casos excepcionales)',      :municipal_code, :system_user_id),
('patrimonio', 'depreciation',NULL,     'Calcular depreciación',      'Ejecutar cálculo de depreciación de bienes',                  :municipal_code, :system_user_id),
('patrimonio', 'baja',        NULL,     'Tramitar baja',              'Iniciar expediente de baja de un bien patrimonial',           :municipal_code, :system_user_id),

-- ────────────────────────────────────────────────────────────────
-- PERMISOS OPERATIVOS — MOVIMIENTOS
-- ────────────────────────────────────────────────────────────────
('movimientos', 'create',  NULL,  'Crear solicitud de movimiento',  'Iniciar solicitud de traslado, asignación o transferencia',  :municipal_code, :system_user_id),
('movimientos', 'read',    NULL,  'Ver todos los movimientos',      'Consultar historial completo de movimientos',                 :municipal_code, :system_user_id),
('movimientos', 'read',    'own', 'Ver mis movimientos',            'Consultar solo los movimientos propios o del área',           :municipal_code, :system_user_id),
('movimientos', 'approve', NULL,  'Aprobar movimiento',             'Aprobar solicitudes de movimiento de bienes',                 :municipal_code, :system_user_id),
('movimientos', 'reject',  NULL,  'Rechazar movimiento',            'Rechazar solicitudes de movimiento con observación',          :municipal_code, :system_user_id),
('movimientos', 'acta',    'generate', 'Generar acta',              'Generar acta de entrega-recepción de bienes',                 :municipal_code, :system_user_id),

-- ────────────────────────────────────────────────────────────────
-- PERMISOS OPERATIVOS — INVENTARIO
-- ────────────────────────────────────────────────────────────────
('inventario', 'create',      NULL,     'Crear inventario',          'Programar nuevo inventario físico',                          :municipal_code, :system_user_id),
('inventario', 'read',        NULL,     'Ver inventarios',           'Consultar todos los inventarios',                            :municipal_code, :system_user_id),
('inventario', 'read',        'active', 'Ver inventario activo',     'Consultar solo el inventario en curso',                      :municipal_code, :system_user_id),
('inventario', 'update',      NULL,     'Editar inventario',         'Modificar datos del inventario',                             :municipal_code, :system_user_id),
('inventario', 'verify',      'item',   'Verificar ítem',            'Marcar bienes como verificados durante el inventario',       :municipal_code, :system_user_id),
('inventario', 'conciliate',  NULL,     'Conciliar inventario',      'Conciliar diferencias entre inventario físico y contable',   :municipal_code, :system_user_id),
('inventario', 'close',       NULL,     'Cerrar inventario',         'Cerrar y firmar el inventario físico',                       :municipal_code, :system_user_id),

-- ────────────────────────────────────────────────────────────────
-- PERMISOS OPERATIVOS — MANTENIMIENTO
-- ────────────────────────────────────────────────────────────────
('mantenimiento', 'create',  NULL,       'Programar mantenimiento',  'Crear órdenes de mantenimiento preventivo o correctivo',     :municipal_code, :system_user_id),
('mantenimiento', 'read',    NULL,       'Ver mantenimientos',       'Consultar historial y calendario de mantenimientos',         :municipal_code, :system_user_id),
('mantenimiento', 'update',  NULL,       'Editar mantenimiento',     'Actualizar datos de una orden de mantenimiento',             :municipal_code, :system_user_id),
('mantenimiento', 'close',   NULL,       'Cerrar mantenimiento',     'Marcar una orden de mantenimiento como ejecutada',           :municipal_code, :system_user_id),
('mantenimiento', 'alert',   'configure','Configurar alertas',       'Configurar alertas de vencimiento de mantenimiento',         :municipal_code, :system_user_id),

-- ────────────────────────────────────────────────────────────────
-- PERMISOS OPERATIVOS — REPORTES
-- ────────────────────────────────────────────────────────────────
('reportes', 'read',     NULL, 'Ver reportes',                      'Acceder a dashboards y reportes regulatorios',               :municipal_code, :system_user_id),
('reportes', 'generate', NULL, 'Generar reporte',                   'Ejecutar generación de reportes bajo demanda',               :municipal_code, :system_user_id),
('reportes', 'export',   NULL, 'Exportar reporte',                  'Exportar reportes en PDF, Excel u otros formatos',           :municipal_code, :system_user_id),
('reportes', 'schedule', NULL, 'Programar reporte',                 'Configurar reportes automáticos programados',                :municipal_code, :system_user_id),

-- ────────────────────────────────────────────────────────────────
-- PERMISOS OPERATIVOS — AUDITORÍA
-- ────────────────────────────────────────────────────────────────
('auditoria', 'read', NULL, 'Ver auditoría',                        'Consultar logs de auditoria_cambios y auditoria_accesos',    :municipal_code, :system_user_id),

-- ────────────────────────────────────────────────────────────────
-- PERMISOS OPERATIVOS — CONFIGURACIÓN
-- ────────────────────────────────────────────────────────────────
('config', 'read',              NULL, 'Ver configuración',           'Consultar configuración del tenant',                         :municipal_code, :system_user_id),
('config', 'update',            NULL, 'Editar configuración',        'Modificar parámetros de configuración del tenant',           :municipal_code, :system_user_id),
('config', 'areas',             'manage', 'Gestionar áreas',         'Crear, editar y desactivar áreas organizacionales',          :municipal_code, :system_user_id),
('config', 'categories',        'manage', 'Gestionar categorías',    'Crear y editar categorías de bienes',                        :municipal_code, :system_user_id),
('config', 'locations',         'manage', 'Gestionar ubicaciones',   'Crear y editar ubicaciones físicas',                         :municipal_code, :system_user_id),

-- ────────────────────────────────────────────────────────────────
-- PERMISOS OPERATIVOS — USUARIOS Y SEGURIDAD
-- ────────────────────────────────────────────────────────────────
('users', 'manage',  NULL, 'Gestionar usuarios',                    'Crear, editar, suspender y desactivar usuarios',             :municipal_code, :system_user_id),
('roles', 'manage',  NULL, 'Gestionar roles',                       'Crear y editar roles custom del tenant',                     :municipal_code, :system_user_id),
('roles', 'assign',  NULL, 'Asignar roles',                         'Asignar y revocar roles a usuarios',                         :municipal_code, :system_user_id);
```

---

## 10. SQL — seed de roles_permissions

Asignación de permisos a cada rol del sistema. Usar subconsultas para obtener los IDs por nombre, evitando hardcodear UUIDs.

```sql
-- ================================================================
-- SEED: roles_permissions
-- ================================================================
-- Notación: p(module, action, resource) → permiso
--           r(name)                     → rol

-- ────────────────────────────────────────────────────────────────
-- PATRIMONIO_VIEWER
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'PATRIMONIO_VIEWER'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view',  'bienes'),
    ('patrimonio', 'read',  '')
  );

-- ────────────────────────────────────────────────────────────────
-- PATRIMONIO_OPERARIO
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'PATRIMONIO_OPERARIO'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view',   'bienes'),
    ('sidebar',    'view',   'movimientos'),
    ('patrimonio', 'create', ''),
    ('patrimonio', 'read',   ''),
    ('patrimonio', 'update', 'status'),
    ('movimientos','create', ''),
    ('movimientos','read',   'own')
  );

-- ────────────────────────────────────────────────────────────────
-- PATRIMONIO_GESTOR
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'PATRIMONIO_GESTOR'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view',         'bienes'),
    ('sidebar',    'view',         'categorias'),
    ('sidebar',    'view',         'proveedores'),
    ('sidebar',    'view',         'ubicaciones'),
    ('sidebar',    'view',         'movimientos'),
    ('sidebar',    'view',         'inventarios'),
    ('sidebar',    'view',         'bajas'),
    ('sidebar',    'view',         'reportes'),
    ('patrimonio', 'create',       ''),
    ('patrimonio', 'read',         ''),
    ('patrimonio', 'update',       ''),
    ('patrimonio', 'update',       'status'),
    ('patrimonio', 'delete',       ''),
    ('patrimonio', 'depreciation', ''),
    ('patrimonio', 'baja',         ''),
    ('movimientos','create',       ''),
    ('movimientos','read',         ''),
    ('movimientos','read',         'own'),
    ('inventario', 'read',         ''),
    ('inventario', 'read',         'active'),
    ('reportes',   'read',         ''),
    ('reportes',   'generate',     ''),
    ('reportes',   'export',       '')
  );

-- ────────────────────────────────────────────────────────────────
-- MOVIMIENTOS_SOLICITANTE
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'MOVIMIENTOS_SOLICITANTE'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view',   'movimientos'),
    ('sidebar',    'view',   'bienes'),
    ('movimientos','create', ''),
    ('movimientos','read',   'own'),
    ('patrimonio', 'read',   '')
  );

-- ────────────────────────────────────────────────────────────────
-- MOVIMIENTOS_VIEWER
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'MOVIMIENTOS_VIEWER'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view', 'movimientos'),
    ('movimientos','read', '')
  );

-- ────────────────────────────────────────────────────────────────
-- MOVIMIENTOS_APROBADOR
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'MOVIMIENTOS_APROBADOR'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view',      'movimientos'),
    ('sidebar',    'view',      'actas'),
    ('sidebar',    'view',      'bienes'),
    ('sidebar',    'view',      'reportes'),
    ('movimientos','create',    ''),
    ('movimientos','read',      ''),
    ('movimientos','read',      'own'),
    ('movimientos','approve',   ''),
    ('movimientos','reject',    ''),
    ('movimientos','acta',      'generate'),
    ('patrimonio', 'read',      ''),
    ('reportes',   'read',      ''),
    ('reportes',   'generate',  ''),
    ('reportes',   'export',    '')
  );

-- ────────────────────────────────────────────────────────────────
-- INVENTARIO_VERIFICADOR
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'INVENTARIO_VERIFICADOR'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',   'view',   'inventarios'),
    ('sidebar',   'view',   'bienes'),
    ('inventario','read',   'active'),
    ('inventario','verify', 'item'),
    ('patrimonio','read',   '')
  );

-- ────────────────────────────────────────────────────────────────
-- INVENTARIO_COORDINADOR
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'INVENTARIO_COORDINADOR'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',   'view',       'inventarios'),
    ('sidebar',   'view',       'bienes'),
    ('sidebar',   'view',       'reportes'),
    ('inventario','create',     ''),
    ('inventario','read',       ''),
    ('inventario','read',       'active'),
    ('inventario','update',     ''),
    ('inventario','verify',     'item'),
    ('inventario','conciliate', ''),
    ('inventario','close',      ''),
    ('patrimonio','read',       ''),
    ('reportes',  'read',       ''),
    ('reportes',  'generate',   ''),
    ('reportes',  'export',     '')
  );

-- ────────────────────────────────────────────────────────────────
-- MANTENIMIENTO_VIEWER
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'MANTENIMIENTO_VIEWER'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',       'view',  'mantenimientos'),
    ('mantenimiento', 'read',  '')
  );

-- ────────────────────────────────────────────────────────────────
-- MANTENIMIENTO_GESTOR
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'MANTENIMIENTO_GESTOR'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',       'view',      'mantenimientos'),
    ('sidebar',       'view',      'bienes'),
    ('sidebar',       'view',      'movimientos'),
    ('mantenimiento', 'create',    ''),
    ('mantenimiento', 'read',      ''),
    ('mantenimiento', 'update',    ''),
    ('mantenimiento', 'close',     ''),
    ('mantenimiento', 'alert',     'configure'),
    ('movimientos',   'create',    ''),
    ('movimientos',   'read',      'own'),
    ('patrimonio',    'read',      '')
  );

-- ────────────────────────────────────────────────────────────────
-- REPORTES_VIEWER
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'REPORTES_VIEWER'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar', 'view',     'reportes'),
    ('sidebar', 'view',     'notificaciones'),
    ('reportes','read',     ''),
    ('reportes','generate', ''),
    ('reportes','export',   '')
  );

-- ────────────────────────────────────────────────────────────────
-- REPORTES_SCHEDULER
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'REPORTES_SCHEDULER'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar', 'view',     'reportes'),
    ('sidebar', 'view',     'notificaciones'),
    ('reportes','read',     ''),
    ('reportes','generate', ''),
    ('reportes','export',   ''),
    ('reportes','schedule', '')
  );

-- ────────────────────────────────────────────────────────────────
-- AUDITORIA_VIEWER
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'AUDITORIA_VIEWER'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',  'view', 'auditoria'),
    ('sidebar',  'view', 'reportes'),
    ('sidebar',  'view', 'notificaciones'),
    ('auditoria','read', ''),
    ('reportes', 'read', ''),
    ('reportes', 'export','')
  );

-- ────────────────────────────────────────────────────────────────
-- TENANT_CONFIG_MANAGER
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'TENANT_CONFIG_MANAGER'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar', 'view',      'personas'),
    ('sidebar', 'view',      'areas'),
    ('sidebar', 'view',      'cargos'),
    ('sidebar', 'view',      'categorias'),
    ('sidebar', 'view',      'proveedores'),
    ('sidebar', 'view',      'ubicaciones'),
    ('config',  'read',      ''),
    ('config',  'update',    ''),
    ('config',  'areas',     'manage'),
    ('config',  'categories','manage'),
    ('config',  'locations', 'manage')
  );

-- ────────────────────────────────────────────────────────────────
-- TENANT_ADMIN — todos los permisos anteriores + gestión total
-- ────────────────────────────────────────────────────────────────
INSERT INTO roles_permissions (role_id, permission_id, municipal_code)
SELECT r.id, p.id, :municipal_code
FROM roles r, permissions p
WHERE r.name = 'TENANT_ADMIN'
  AND r.municipal_code = :municipal_code
  AND p.municipal_code = :municipal_code
  AND p.status = true;
-- TENANT_ADMIN obtiene TODOS los permisos del tenant
```

---

## 11. SQL — seed de position_allowed_roles

Seed completo basado en las áreas y cargos de la SBN. Los `position_id` deben reemplazarse con los UUIDs reales de la tabla `cargos` del Config Service.

```sql
-- ================================================================
-- SEED: position_allowed_roles
-- Ejecutar durante el onboarding de cada nuevo tenant
-- ================================================================
-- Notación de referencia:
-- pos('Nombre del Cargo') → UUID del cargo en Config Service
-- rol('NOMBRE_ROL')       → UUID del rol en la tabla roles

-- ────────────────────────────────────────────────────────────────
-- ÁREA: Oficina General de Administración (OGA)
-- ────────────────────────────────────────────────────────────────

-- Gerente / Jefe OGA
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'MOVIMIENTOS_APROBADOR' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos  WHERE name = 'Gerente OGA'            AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas   WHERE name = 'Oficina General de Administración' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('TENANT_ADMIN','MOVIMIENTOS_APROBADOR','REPORTES_VIEWER','AUDITORIA_VIEWER')
        AND municipal_code = :municipal_code) r;

-- Especialista Administrativo OGA
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'PATRIMONIO_GESTOR' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Especialista Administrativo' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Oficina General de Administración' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('PATRIMONIO_GESTOR','MOVIMIENTOS_APROBADOR','REPORTES_VIEWER')
        AND municipal_code = :municipal_code) r;

-- ────────────────────────────────────────────────────────────────
-- ÁREA: Unidad de Control Patrimonial (UCP)
-- ────────────────────────────────────────────────────────────────

-- Jefe de Control Patrimonial
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'PATRIMONIO_GESTOR'     THEN true
                   WHEN 'MOVIMIENTOS_APROBADOR'  THEN true
                   ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Jefe de Control Patrimonial' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Unidad de Control Patrimonial' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN (
           'PATRIMONIO_GESTOR','INVENTARIO_COORDINADOR',
           'MOVIMIENTOS_APROBADOR','REPORTES_SCHEDULER','AUDITORIA_VIEWER')
        AND municipal_code = :municipal_code) r;

-- Técnico Patrimonial
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'PATRIMONIO_GESTOR' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Técnico Patrimonial' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Unidad de Control Patrimonial' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('PATRIMONIO_GESTOR','INVENTARIO_VERIFICADOR','MOVIMIENTOS_SOLICITANTE','REPORTES_VIEWER')
        AND municipal_code = :municipal_code) r;

-- Técnico de Inventario
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'INVENTARIO_VERIFICADOR' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Técnico de Inventario' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Unidad de Control Patrimonial' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('INVENTARIO_VERIFICADOR','PATRIMONIO_VIEWER')
        AND municipal_code = :municipal_code) r;

-- Asistente Patrimonial
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id, true, :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Asistente Patrimonial' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Unidad de Control Patrimonial' AND municipal_code = :municipal_code) a,
       (SELECT id FROM roles  WHERE name = 'PATRIMONIO_OPERARIO' AND municipal_code = :municipal_code) r;

-- ────────────────────────────────────────────────────────────────
-- ÁREA: Unidad de Abastecimiento / Logística (UAL)
-- ────────────────────────────────────────────────────────────────

-- Jefe de Abastecimiento
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'MOVIMIENTOS_SOLICITANTE' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Jefe de Abastecimiento' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Unidad de Abastecimiento' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('PATRIMONIO_GESTOR','MOVIMIENTOS_SOLICITANTE','REPORTES_VIEWER')
        AND municipal_code = :municipal_code) r;

-- Almacenero / Técnico de Almacén
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'PATRIMONIO_OPERARIO' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Almacenero' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Unidad de Abastecimiento' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('PATRIMONIO_OPERARIO','MOVIMIENTOS_SOLICITANTE')
        AND municipal_code = :municipal_code) r;

-- Asistente de Logística
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id, true, :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Asistente de Logística' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Unidad de Abastecimiento' AND municipal_code = :municipal_code) a,
       (SELECT id FROM roles  WHERE name = 'PATRIMONIO_OPERARIO' AND municipal_code = :municipal_code) r;

-- ────────────────────────────────────────────────────────────────
-- ÁREA: Oficina de Contabilidad (OC)
-- ────────────────────────────────────────────────────────────────

-- Contador General
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'PATRIMONIO_VIEWER' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Contador General' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Oficina de Contabilidad' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('REPORTES_VIEWER','AUDITORIA_VIEWER','PATRIMONIO_VIEWER')
        AND municipal_code = :municipal_code) r;

-- Asistente Contable
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'PATRIMONIO_VIEWER' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Asistente Contable' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Oficina de Contabilidad' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('PATRIMONIO_VIEWER','REPORTES_VIEWER')
        AND municipal_code = :municipal_code) r;

-- ────────────────────────────────────────────────────────────────
-- ÁREA: Unidad de Asesoría Jurídica (UAJ)
-- ────────────────────────────────────────────────────────────────

-- Asesor Legal
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'AUDITORIA_VIEWER' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Asesor Legal' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Unidad de Asesoría Jurídica' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('AUDITORIA_VIEWER','PATRIMONIO_VIEWER','REPORTES_VIEWER')
        AND municipal_code = :municipal_code) r;

-- ────────────────────────────────────────────────────────────────
-- ÁREA: Gerencia de Infraestructura y Obras (GIO)
-- ────────────────────────────────────────────────────────────────

-- Técnico de Mantenimiento
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'MANTENIMIENTO_GESTOR' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Técnico de Mantenimiento' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Gerencia de Infraestructura y Obras' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('MANTENIMIENTO_GESTOR','MOVIMIENTOS_SOLICITANTE','PATRIMONIO_VIEWER')
        AND municipal_code = :municipal_code) r;

-- Gerente de Infraestructura (área usuaria)
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, a.id, r.id,
       CASE r.name WHEN 'MOVIMIENTOS_SOLICITANTE' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Gerente de Infraestructura' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM areas  WHERE name = 'Gerencia de Infraestructura y Obras' AND municipal_code = :municipal_code) a,
       (SELECT id, name FROM roles WHERE name IN ('MOVIMIENTOS_SOLICITANTE','PATRIMONIO_VIEWER','MANTENIMIENTO_VIEWER')
        AND municipal_code = :municipal_code) r;

-- ────────────────────────────────────────────────────────────────
-- ÁREA: Gerencia de Servicios a la Ciudad (GSC)
-- ────────────────────────────────────────────────────────────────

-- Gerente de Servicios / Supervisor de Serenazgo (area_id = NULL → cualquier área usuaria)
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, NULL, r.id,
       CASE r.name WHEN 'MOVIMIENTOS_SOLICITANTE' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name IN ('Gerente de Servicios','Supervisor de Serenazgo')
        AND municipal_code = :municipal_code) pos,
       (SELECT id, name FROM roles WHERE name IN ('MOVIMIENTOS_SOLICITANTE','PATRIMONIO_VIEWER')
        AND municipal_code = :municipal_code) r;

-- ────────────────────────────────────────────────────────────────
-- COMISIÓN DE INVENTARIO (CI) — roles temporales, area_id = NULL
-- Asignar con expiration_date en users_roles
-- ────────────────────────────────────────────────────────────────

-- Presidente de Comisión
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, NULL, r.id, true, :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Presidente de Comisión de Inventario' AND municipal_code = :municipal_code) pos,
       (SELECT id FROM roles  WHERE name = 'INVENTARIO_COORDINADOR' AND municipal_code = :municipal_code) r;

-- Miembro Verificador
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, NULL, r.id,
       CASE r.name WHEN 'INVENTARIO_VERIFICADOR' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Miembro Verificador CI' AND municipal_code = :municipal_code) pos,
       (SELECT id, name FROM roles WHERE name IN ('INVENTARIO_VERIFICADOR','PATRIMONIO_VIEWER')
        AND municipal_code = :municipal_code) r;

-- ────────────────────────────────────────────────────────────────
-- PLANIFICACIÓN / ANALISTA — area_id = NULL → cualquier área
-- ────────────────────────────────────────────────────────────────
INSERT INTO position_allowed_roles (position_id, area_id, role_id, is_default, municipal_code, created_by)
SELECT pos.id, NULL, r.id,
       CASE r.name WHEN 'REPORTES_VIEWER' THEN true ELSE false END,
       :municipal_code, :system_user_id
FROM   (SELECT id FROM cargos WHERE name = 'Analista de Reportes' AND municipal_code = :municipal_code) pos,
       (SELECT id, name FROM roles WHERE name IN ('REPORTES_VIEWER','REPORTES_SCHEDULER','AUDITORIA_VIEWER')
        AND municipal_code = :municipal_code) r;
```

> **Nota de implementación**: los nombres de `cargos` y `areas` en los SELECT deben coincidir exactamente con los registros que el Config Service inserta durante el onboarding. Se recomienda usar constantes centralizadas en el código para evitar typos.

---

## 12. Implementación en React

### Hook de permisos

```javascript
// src/hooks/usePermissions.js
import { useAuthStore } from '@/stores/authStore';

export const usePermissions = () => {
  const { permissions } = useAuthStore();

  const canView = (resource) => permissions.includes(`sidebar:view:${resource}`);
  const canDo   = (module, action, resource = null) => {
    const perm = resource ? `${module}:${action}:${resource}` : `${module}:${action}`;
    return permissions.includes(perm);
  };

  return { canView, canDo };
};
```

### Carga de permisos post-login

```javascript
// src/stores/authStore.js  (Zustand)
import { create } from 'zustand';
import { assignmentApi } from '@/api/authService';

export const useAuthStore = create((set) => ({
  token: null,
  user: null,
  permissions: [],

  login: async (credentials) => {
    // 1. Login → obtener JWT
    const { data: loginData } = await authApi.login(credentials);
    const { access_token, user_id } = loginData;

    // 2. Obtener permisos efectivos con el endpoint real
    // GET /api/v1/assignments/users/{userId}/effective-permissions
    const { data: permsData } = await assignmentApi.getEffectivePermissions(user_id, {
      headers: { Authorization: `Bearer ${access_token}` }
    });

    set({
      token: access_token,
      user:  { ...loginData },
      permissions: permsData,  // ["sidebar:view:bienes", "patrimonio:create", ...]
    });
  },

  logout: () => set({ token: null, user: null, permissions: [] }),
}));
```

```javascript
// src/api/authService.js
import axios from 'axios';

const BASE = import.meta.env.VITE_AUTH_SERVICE_URL; // http://localhost:5002

export const authApi = {
  login: (body) => axios.post(`${BASE}/api/v1/auth/login`, body),
  refresh: (body) => axios.post(`${BASE}/api/v1/auth/refresh`, body),
  logout: () => axios.post(`${BASE}/api/v1/auth/logout`),
};

export const assignmentApi = {
  // Endpoint real para permisos efectivos del usuario
  getEffectivePermissions: (userId, config) =>
    axios.get(`${BASE}/api/v1/assignments/users/${userId}/effective-permissions`, config),

  // Endpoint para listar roles asignados
  getUserRoles: (userId) =>
    axios.get(`${BASE}/api/v1/assignments/users/${userId}/roles`),

  // Endpoint para asignar rol (TENANT_ADMIN)
  assignRole: (userId, roleId) =>
    axios.post(`${BASE}/api/v1/assignments/users/${userId}/roles/${roleId}`),
};
```

### sidebarConfig.js actualizado

```javascript
// src/layouts/components/sidebarConfig.js
import { usePermissions } from '@/hooks/usePermissions';

export const useSidebarConfig = () => {
  const { canView } = usePermissions();

  return [
    // ── Dashboard ───────────────────────────────────────────
    {
      label: 'Dashboard',
      path: '/',
      icon: 'chart',
      visible: true,
    },

    // ── Administración Global (solo SUPER_ADMIN) ────────────
    canView('admin') && {
      label: 'Administración',
      icon: 'shield',
      color: '#dc2626',
      children: [
        { label: 'Municipalidades', path: '/admin/municipalidades' },
        { label: 'Suscripciones',   path: '/admin/suscripciones',  comingSoon: true },
        { label: 'Facturación',     path: '/admin/facturacion',    comingSoon: true },
        { label: 'Onboarding',      path: '/admin/onboarding',     comingSoon: true },
      ],
    },

    // ── Gestión de Activos ──────────────────────────────────
    (canView('bienes') || canView('categorias') || canView('proveedores') || canView('ubicaciones')) && {
      label: 'Gestión de Activos',
      icon: 'package',
      color: '#34d399',
      children: [
        canView('bienes')      && { label: 'Bienes',             path: '/bienes' },
        canView('categorias')  && { label: 'Categorías',         path: '/categorias' },
        canView('proveedores') && { label: 'Proveedores',        path: '/proveedores' },
        canView('ubicaciones') && { label: 'Ubicaciones Físicas',path: '/ubicaciones' },
      ].filter(Boolean),
    },

    // ── Operaciones ─────────────────────────────────────────
    (canView('movimientos') || canView('actas') || canView('inventarios') || canView('mantenimientos')) && {
      label: 'Operaciones',
      icon: 'refresh',
      color: '#f59e0b',
      children: [
        canView('movimientos')    && { label: 'Movimientos',     path: '/movimientos' },
        canView('actas')          && { label: 'Actas de Entrega',path: '/actas' },
        canView('inventarios')    && { label: 'Inventarios',     path: '/inventarios' },
        canView('mantenimientos') && { label: 'Mantenimientos',  path: '/mantenimientos' },
      ].filter(Boolean),
    },

    // ── Usuarios y Seguridad ────────────────────────────────
    (canView('usuarios') || canView('personas') || canView('roles') ||
     canView('permisos') || canView('areas')    || canView('cargos')) && {
      label: 'Usuarios y Seguridad',
      icon: 'users',
      color: '#f97316',
      children: [
        canView('usuarios') && { label: 'Usuarios', path: '/usuarios' },
        canView('personas') && { label: 'Personas', path: '/personas' },
        canView('roles')    && { label: 'Roles',    path: '/roles'    },
        canView('permisos') && { label: 'Permisos', path: '/permisos' },
        canView('areas')    && { label: 'Áreas',    path: '/areas'    },
        canView('cargos')   && { label: 'Cargos',   path: '/cargos'   },
      ].filter(Boolean),
    },

    // ── Contabilidad ────────────────────────────────────────
    (canView('bajas') || canView('valores')) && {
      label: 'Contabilidad',
      icon: 'calculator',
      color: '#eab308',
      children: [
        canView('bajas')   && { label: 'Bajas de Activos',    path: '/bajas' },
        canView('valores') && { label: 'Historial de Valores',path: '/valores', comingSoon: true },
      ].filter(Boolean),
    },

    // ── Reportes y Auditoría ────────────────────────────────
    (canView('reportes') || canView('auditoria') || canView('notificaciones')) && {
      label: 'Reportes y Auditoría',
      icon: 'chart-bar',
      color: '#8b5cf6',
      children: [
        canView('reportes')       && { label: 'Reportes',      path: '/reportes',      comingSoon: true },
        canView('auditoria')      && { label: 'Auditoría',     path: '/auditoria',     comingSoon: true },
        canView('notificaciones') && { label: 'Notificaciones',path: '/notificaciones',comingSoon: true },
      ].filter(Boolean),
    },

    // ── Configuración ───────────────────────────────────────
    canView('sistema') && {
      label: 'Configuración',
      icon: 'settings',
      color: '#94a3b8',
      children: [
        { label: 'Sistema', path: '/sistema' },
      ],
    },

  ].filter(Boolean);
};
```

### Control de botones por operación

```javascript
// Ejemplo — página de Movimientos
import { usePermissions } from '@/hooks/usePermissions';

const MovimientosPage = () => {
  const { canDo } = usePermissions();

  return (
    <div>
      {/* MOVIMIENTOS_SOLICITANTE y MOVIMIENTOS_APROBADOR */}
      {canDo('movimientos', 'create') && (
        <Button onClick={handleCreate}>Nueva Solicitud</Button>
      )}

      {/* Solo MOVIMIENTOS_APROBADOR */}
      {canDo('movimientos', 'approve') && (
        <Button onClick={handleApprove}>Aprobar</Button>
      )}

      {/* Solo MOVIMIENTOS_APROBADOR */}
      {canDo('movimientos', 'acta', 'generate') && (
        <Button onClick={handleActa}>Generar Acta</Button>
      )}
    </div>
  );
};

// Ejemplo — página de Inventarios
const InventariosPage = () => {
  const { canDo } = usePermissions();

  return (
    <div>
      {/* INVENTARIO_COORDINADOR */}
      {canDo('inventario', 'create') && (
        <Button onClick={handleCreate}>Nuevo Inventario</Button>
      )}

      {/* INVENTARIO_VERIFICADOR — verifica ítems en inventario activo */}
      {canDo('inventario', 'verify', 'item') && (
        <Button onClick={handleVerify}>Verificar Ítem</Button>
      )}

      {/* Solo INVENTARIO_COORDINADOR */}
      {canDo('inventario', 'close') && (
        <Button onClick={handleClose}>Cerrar Inventario</Button>
      )}
    </div>
  );
};
```

---

*Versión 2.0 — SIPREB 2025*  
*Basado en: schema.sql · roles-sipreb.md · sidebar_structure.md · endpoints.md*  
*Normativa: Ley N° 29151 · Directiva N° 001-2015/SBN*

