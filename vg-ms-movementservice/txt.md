# API Handover Receipts - Actas de Entrega

Base URL: `http://localhost:5005/api/v1/handover-receipts`

Municipality ID de ejemplo: `24ad12a5-d9e5-4cdd-91f1-8fd0355c9473`

---

## 1. Crear Acta de Entrega

**POST** `/municipality/{municipalityId}`

```
POST http://localhost:5005/api/v1/handover-receipts/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
    "movementId": "ab4d52af-6330-44b8-83ac-3a715305f379",
    "deliveringResponsibleId": "2718dd7c-7bb8-4cd7-9a11-caf56d003d1f",
    "receivingResponsibleId": "76069df0-bcaa-4188-af6e-6265127413c0",
    "witness1Id": "40d4faa8-c68c-4fcb-a0b7-6ac8a5bee137",
    "witness2Id": "ca26c491-d30e-461c-9e2d-002fd731002c",
    "receiptDate": "2025-12-03",
    "deliveryObservations": "Entrega en buen estado",
    "receptionObservations": "Recibido conforme",
    "specialConditions": "Sin condiciones especiales",
    "generatedBy": "40a5c2fa-ab64-4a6e-8335-2a136ebeed1a"
}
```

**Campos requeridos:** `movementId`, `deliveringResponsibleId`, `receivingResponsibleId`, `generatedBy`

**Campos opcionales:** `witness1Id`, `witness2Id`, `receiptDate`, `deliveryObservations`, `receptionObservations`, `specialConditions`

---

## 2. Listar Todas las Actas

**GET** `/municipality/{municipalityId}`

```
GET http://localhost:5005/api/v1/handover-receipts/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```

---

## 3. Obtener Acta por ID

**GET** `/{id}/municipality/{municipalityId}`

```
GET http://localhost:5005/api/v1/handover-receipts/122d5334-8349-4ff5-809e-69ea4b4244c4/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```

---

## 4. Editar Acta

**PUT** `/{id}/municipality/{municipalityId}`

```
PUT http://localhost:5005/api/v1/handover-receipts/122d5334-8349-4ff5-809e-69ea4b4244c4/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
    "movementId": "ab4d52af-6330-44b8-83ac-3a715305f379",
    "deliveringResponsibleId": "2718dd7c-7bb8-4cd7-9a11-caf56d003d1f",
    "receivingResponsibleId": "76069df0-bcaa-4188-af6e-6265127413c0",
    "witness1Id": "40d4faa8-c68c-4fcb-a0b7-6ac8a5bee137",
    "witness2Id": "ca26c491-d30e-461c-9e2d-002fd731002c",
    "receiptDate": "2025-12-03",
    "deliveryObservations": "Observación actualizada",
    "receptionObservations": "Recepción modificada",
    "specialConditions": "Nuevas condiciones",
    "generatedBy": "40a5c2fa-ab64-4a6e-8335-2a136ebeed1a"
}
```

---

## 5. Firmar Acta

**POST** `/{id}/sign/municipality/{municipalityId}`

```
POST http://localhost:5005/api/v1/handover-receipts/122d5334-8349-4ff5-809e-69ea4b4244c4/sign/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```

**Headers:**
```
Content-Type: application/json
```

**Firma de Entrega:**
```json
{
    "signerId": "2718dd7c-7bb8-4cd7-9a11-caf56d003d1f",
    "signatureType": "delivery",
    "observations": "Entregado sin novedades"
}
```

**Firma de Recepción:**
```json
{
    "signerId": "76069df0-bcaa-4188-af6e-6265127413c0",
    "signatureType": "reception",
    "observations": "Recibido conforme"
}
```

---

## 6. Listar por Estado

**GET** `/status/{status}/municipality/{municipalityId}`

```
GET http://localhost:5005/api/v1/handover-receipts/status/GENERATED/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```

**Estados válidos:** `GENERATED`, `PARTIALLY_SIGNED`, `FULLY_SIGNED`, `VOIDED`

---

## 7. Listar por Responsable

**GET** `/responsible/{responsibleId}/municipality/{municipalityId}`

```
GET http://localhost:5005/api/v1/handover-receipts/responsible/2718dd7c-7bb8-4cd7-9a11-caf56d003d1f/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```

---

## 8. Obtener por Movimiento

**GET** `/movement/{movementId}/municipality/{municipalityId}`

```
GET http://localhost:5005/api/v1/handover-receipts/movement/ab4d52af-6330-44b8-83ac-3a715305f379/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```

---

## 9. Contar Actas

**GET** `/count/municipality/{municipalityId}`

```
GET http://localhost:5005/api/v1/handover-receipts/count/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```

---

## 10. Contar por Estado

**GET** `/count/status/{status}/municipality/{municipalityId}`

```
GET http://localhost:5005/api/v1/handover-receipts/count/status/FULLY_SIGNED/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```

# 11. Eliminar

```
https://docs.google.com/document/d/1JrxmIlx3Ne7YOspfSoK-pxsR_2Qtnjp2ggW6JnCrN1E/edit?usp=sharing
```

**DELETE - Seleccionar ID para Eliminar**

```
DELETE http://localhost:5005/api/v1/handover-receipts/122d5334-8349-4ff5-809e-69ea4b4244c4/municipality/24ad12a5-d9e5-4cdd-91f1-8fd0355c9473
```





