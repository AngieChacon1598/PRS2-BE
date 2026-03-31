-- =================================================================
-- MS-MANTENIMIENTO: SBN / LEY 29151 COMPLIANCE
-- BASADO EN: Directiva N° 001-2015/SBN · D.S. N° 007-2008-VIVIENDA
-- =================================================================

-- 1. TABLA PRINCIPAL: maintenances
CREATE TABLE maintenances (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    municipality_id          UUID NOT NULL, -- Ref: TenantManagementService
    maintenance_code         VARCHAR(50) UNIQUE NOT NULL, -- MNT-YYYY-XXXXXX
    
    -- Referencias Externas: PatrimonioService (Origen de Datos)
    asset_id                 UUID NOT NULL, -- ID del bien real en Patrimonio
    asset_code               VARCHAR(50),   -- Desnormalizado para reportes sin JOINs externos
    asset_description        VARCHAR(255),  -- Desnormalizado para reportes sin JOINs externos
    
    maintenance_type         VARCHAR(30) NOT NULL, -- PREVENTIVE, CORRECTIVE, PREDICTIVE, EMERGENCY
    is_scheduled             BOOLEAN DEFAULT false,
    priority                 VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    
    scheduled_date           DATE NOT NULL,
    start_date               TIMESTAMP,
    end_date                 TIMESTAMP,
    next_date                DATE,            -- Para mantenimientos cíclicos/preventivos
    estimated_duration_hours DECIMAL(5,1),
    
    work_description         TEXT NOT NULL,
    reported_problem         TEXT,
    applied_solution         TEXT,            -- Se llena al completar el trabajo técnico
    observations             TEXT,
    
    -- Referencias Externas: AuthService (Usuarios)
    technical_responsible_id UUID NOT NULL, -- Técnico asignado
    supervisor_id            UUID,           -- Jefe/Supervisor del servicio
    requested_by             UUID NOT NULL, -- Usuario que reportó la necesidad
    
    -- Referencias Externas: ConfigService (Proveedores)
    service_supplier_id      UUID,           -- RUC/ID del proveedor si es servicio externo
    
    -- Control Económico (Trazabilidad Contable SBN)
    labor_cost               DECIMAL(10,2) DEFAULT 0,
    parts_cost               DECIMAL(10,2) DEFAULT 0, -- Calculado auto desde maintenance_parts
    additional_cost          DECIMAL(10,2) DEFAULT 0,
    total_cost               DECIMAL(10,2) 
        GENERATED ALWAYS AS (labor_cost + parts_cost + additional_cost) STORED,
    
    maintenance_status       VARCHAR(30) DEFAULT 'SCHEDULED',
    -- [SCHEDULED, IN_PROCESS, COMPLETED, PENDING_CONFORMITY, CONFIRMED, CANCELLED, SUSPENDED]
    
    -- Documentación de Respaldo
    work_order               VARCHAR(100),
    purchase_order           VARCHAR(100),
    invoice_number           VARCHAR(100),
    
    -- Garantía Post-Mantenimiento
    has_warranty             BOOLEAN NOT NULL DEFAULT false,
    warranty_expiration_date DATE,
    warranty_description     TEXT,
    
    -- Auditoría Base
    created_at               TIMESTAMP DEFAULT NOW(),
    updated_by               UUID,
    updated_at               TIMESTAMP DEFAULT NOW(),

    -- Restricciones de Dominio
    CONSTRAINT chk_maintenance_type CHECK (maintenance_type IN (
        'PREVENTIVE', 'CORRECTIVE', 'PREDICTIVE', 'EMERGENCY'
    )),
    CONSTRAINT chk_priority CHECK (priority IN (
        'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    )),
    CONSTRAINT chk_status CHECK (maintenance_status IN (
        'SCHEDULED', 'IN_PROCESS', 'COMPLETED', 
        'PENDING_CONFORMITY', 'CONFIRMED', 
        'CANCELLED', 'SUSPENDED'
    ))
);

-- 2. DETALLE DE REPUESTOS / INSUMOS (Trazabilidad de Gasto SBN)
CREATE TABLE maintenance_parts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maintenance_id      UUID NOT NULL REFERENCES maintenances(id) ON DELETE CASCADE,
    municipality_id     UUID NOT NULL,
    
    part_code           VARCHAR(50),         -- Código SIGA/SIAF/SBN
    part_name           VARCHAR(255) NOT NULL,
    description         TEXT,
    part_type           VARCHAR(30) NOT NULL, -- SPARE_PART, CONSUMABLE, TOOL, SERVICE
    
    quantity            DECIMAL(10,3) NOT NULL,
    unit_of_measure     VARCHAR(20) NOT NULL, -- UND, KG, LT, MT
    unit_price          DECIMAL(10,2) NOT NULL DEFAULT 0,
    subtotal            DECIMAL(10,2) 
        GENERATED ALWAYS AS (quantity * unit_price) STORED,
    
    supplier_id         UUID,               -- Ref: ConfigService (Proveedor del repuesto)
    created_at          TIMESTAMP DEFAULT NOW(),

    CONSTRAINT chk_part_type CHECK (part_type IN (
        'SPARE_PART', 'CONSUMABLE', 'TOOL', 'SERVICE', 'OTHER'
    ))
);

-- 3. HISTORIAL DE ESTADOS (Auditoría de Flujo)
CREATE TABLE maintenance_status_history (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maintenance_id      UUID NOT NULL REFERENCES maintenances(id) ON DELETE CASCADE,
    municipality_id     UUID NOT NULL,
    previous_status     VARCHAR(30),
    new_status          VARCHAR(30) NOT NULL,
    reason              TEXT,               -- Obligatorio para CANCELLED/SUSPENDED
    changed_by          UUID NOT NULL,      -- Ref: AuthService
    changed_at          TIMESTAMP DEFAULT NOW(),

    CONSTRAINT chk_hist_status CHECK (new_status IN (
        'SCHEDULED', 'IN_PROCESS', 'COMPLETED', 
        'PENDING_CONFORMITY', 'CONFIRMED', 
        'CANCELLED', 'SUSPENDED'
    ))
);

-- 4. ACTA DE CONFORMIDAD (Cierre Formal SBN)
CREATE TABLE maintenance_conformity (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maintenance_id          UUID NOT NULL UNIQUE REFERENCES maintenances(id) ON DELETE CASCADE,
    municipality_id         UUID NOT NULL,
    conformity_number       VARCHAR(50) UNIQUE NOT NULL, -- CONF-YYYY-XXXXXX
    
    work_quality            VARCHAR(20) NOT NULL, -- EXCELLENT, GOOD, ACCEPTABLE, DEFICIENT
    asset_condition_after   VARCHAR(20) NOT NULL, -- OPERATIONAL, PARTIAL, NON_OPERATIONAL
    observations            TEXT,
    
    confirmed_by            UUID NOT NULL,  -- Ref: AuthService (Responsable/Custodio del Bien)
    confirmed_at            TIMESTAMP DEFAULT NOW(),
    digital_signature       TEXT,
    
    requires_followup       BOOLEAN DEFAULT false,
    followup_description    TEXT,
    
    created_at              TIMESTAMP DEFAULT NOW(),

    CONSTRAINT chk_quality CHECK (work_quality IN (
        'EXCELLENT', 'GOOD', 'ACCEPTABLE', 'DEFICIENT'
    )),
    CONSTRAINT chk_condition CHECK (asset_condition_after IN (
        'OPERATIONAL', 'PARTIAL', 'REQUIRES_FOLLOWUP', 'NON_OPERATIONAL'
    ))
);

-- Índices de Optimización
CREATE INDEX idx_maintenances_asset ON maintenances(asset_id);
CREATE INDEX idx_maintenances_municipality ON maintenances(municipality_id);
CREATE INDEX idx_maintenances_status ON maintenances(maintenance_status);
CREATE INDEX idx_parts_maint ON maintenance_parts(maintenance_id);
CREATE INDEX idx_history_maint ON maintenance_status_history(maintenance_id);
