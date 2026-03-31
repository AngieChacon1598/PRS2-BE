# 🚚 Movements Service

Microservicio de movimientos de activos patrimoniales disponible como imagen Docker.

**🔗 [angie14/vg-ms-movementservice](https://hub.docker.com/r/angie14/vg-ms-movementservice)**

## 📦 Descargar la imagen

```bash
docker pull angie14/movementservice:latest
```

## 🚀 Ejecutar el contenedor

```bash
docker run -d -p 5005:5005 --name movementservice angie14/movementservice:latest
```

- El servicio estará disponible en `http://localhost:5005` después de iniciar el contenedor




# 🚚 Movements Service - Asset Movement Management Microservice

## 📡 Endpoints API

**Base URL:** `http://localhost:5005/api/v1`

**Swagger UI:** `http://localhost:5005/swagger-ui.html`

**API Docs:** `http://localhost:5005/api-docs`

**Health Check:** `http://localhost:5005/actuator/health`

### 📦 Asset Movements (Movimientos de Activos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/asset-movements` | Crear nuevo movimiento |
| GET | `/api/v1/asset-movements/municipality/{municipalityId}` | Listar movimientos por municipio |
| GET | `/api/v1/asset-movements/{id}/municipality/{municipalityId}` | Obtener movimiento por ID |
| PUT | `/api/v1/asset-movements/{id}/municipality/{municipalityId}` | Actualizar movimiento |
| DELETE | `/api/v1/asset-movements/{id}/municipality/{municipalityId}` | Eliminar movimiento (soft delete) |
| GET | `/api/v1/asset-movements/asset/{assetId}/municipality/{municipalityId}` | Movimientos por activo |
| GET | `/api/v1/asset-movements/type/{movementType}/municipality/{municipalityId}` | Filtrar por tipo |
| GET | `/api/v1/asset-movements/status/{status}/municipality/{municipalityId}` | Filtrar por estado |
| GET | `/api/v1/asset-movements/pending-approval/municipality/{municipalityId}` | Movimientos pendientes de aprobación |
| POST | `/api/v1/asset-movements/{id}/approve/municipality/{municipalityId}` | Aprobar movimiento |
| POST | `/api/v1/asset-movements/{id}/reject/municipality/{municipalityId}` | Rechazar movimiento |
| POST | `/api/v1/asset-movements/{id}/in-process/municipality/{municipalityId}` | Marcar como en proceso |
| POST | `/api/v1/asset-movements/{id}/complete/municipality/{municipalityId}` | Completar movimiento |
| POST | `/api/v1/asset-movements/{id}/cancel/municipality/{municipalityId}` | Cancelar movimiento |
| GET | `/api/v1/asset-movements/destination-responsible/{userId}/municipality/{municipalityId}` | Por responsable destino |
| GET | `/api/v1/asset-movements/origin-responsible/{userId}/municipality/{municipalityId}` | Por responsable origen |
| GET | `/api/v1/asset-movements/count/municipality/{municipalityId}` | Contar movimientos |
| GET | `/api/v1/asset-movements/deleted/municipality/{municipalityId}` | Movimientos eliminados |
| POST | `/api/v1/asset-movements/{id}/restore/municipality/{municipalityId}` | Restaurar movimiento |

### 📄 Handover Receipts (Recibos de Entrega)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/handover-receipts` | Crear recibo de entrega |
| GET | `/api/v1/handover-receipts/municipality/{municipalityId}` | Listar recibos por municipio |
| GET | `/api/v1/handover-receipts/{id}/municipality/{municipalityId}` | Obtener recibo por ID |
| PUT | `/api/v1/handover-receipts/{id}/municipality/{municipalityId}` | Actualizar recibo |
| DELETE | `/api/v1/handover-receipts/{id}/municipality/{municipalityId}` | Eliminar recibo |
| POST | `/api/v1/handover-receipts/{id}/sign/municipality/{municipalityId}` | Firmar recibo |

### 👥 Users (Usuarios)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/users/municipality/{municipalityId}` | Listar usuarios por municipio |
| GET | `/api/v1/users/{id}/municipality/{municipalityId}` | Obtener usuario por ID |

