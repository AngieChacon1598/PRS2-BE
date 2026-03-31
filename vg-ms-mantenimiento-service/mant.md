## Flujo completo según la SBN
```
┌─────────────────────────────────────────────────────────────────────┐
│  FLUJO DE MANTENIMIENTO — SBN / Ley 29151                           │
└─────────────────────────────────────────────────────────────────────┘

ORIGEN DEL MANTENIMIENTO
  ├── PREVENTIVE  → Scheduller automático por next_date
  ├── CORRECTIVE  → Solicitud del responsable del bien
  └── EMERGENCY   → Reporte urgente, priority = CRITICAL

SCHEDULED
  │  PatrimonioService: validar asset activo y no en baja
  │  AuthService: validar que technical_responsible existe
  │  ConfigService: validar proveedor si es servicio externo
  ↓
IN_PROCESS  (start_date registrado)
  │  Se registran maintenance_parts conforme se usan
  │  maintenance_status_history: entrada por cada cambio
  │  Posible: → SUSPENDED (con reason y estimated_resume_date)
  │            → CANCELLED (con justificación formal)
  ↓
COMPLETED   (técnico registra applied_solution, end_date)
  │  Se calcula parts_cost sumando maintenance_parts.subtotal
  ↓
PENDING_CONFORMITY   ← NUEVO ESTADO
  │  Se notifica al responsable/custodio del bien
  │  (no al técnico que hizo el trabajo)
  ↓
CONFIRMED   (responsable firma maintenance_conformity)
  │  PatrimonioService: actualizar historial del bien
  │  → si asset_condition_after = REQUIRES_FOLLOWUP:
  │    crear nuevo mantenimiento CORRECTIVE automáticamente
  │  → si has_warranty: registrar warranty_expiration_date
  ↓
FIN DEL CICLO
```

---

## ¿Por qué es una transacción distribuida?

Porque al confirmar un mantenimiento (`CONFIRMED`) necesitas garantizar que **todo esto pasa o nada pasa**:
```
1. MantenimientoService → actualizar status a CONFIRMED
2. PatrimonioService    → actualizar asset.last_maintenance_date
                       → actualizar asset.condition
3. PatrimonioService    → si requiere seguimiento: crear nuevo maintenance
4. MantenimientoService → registrar maintenance_conformity
5. (futuro) Notificaciones → avisar si warranty está por vencer