
-- =============================================
-- TABLA PRINCIPAL: physical_inventories
-- =============================================
CREATE TABLE physical_inventories (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    municipality_id         UUID NOT NULL,
    inventory_number        VARCHAR(50) UNIQUE NOT NULL,
    inventory_type          VARCHAR(30) NOT NULL, -- GENERAL, SELECTIVE, SPECIAL

    -- Alcance del inventario
    description             TEXT NOT NULL,
    area_id                 UUID NULL, -- Puede estar vacío al inicio
    category_id             UUID NULL, -- Puede estar vacío
    location_id             UUID NULL, -- Puede estar vacío

    -- Fechas del proceso
    planned_start_date      DATE NOT NULL,
    planned_end_date        DATE NOT NULL,
    actual_start_date       DATE NULL,
    actual_end_date         DATE NULL,

    -- Estado del inventario
    inventory_status        VARCHAR(30) DEFAULT 'PLANNED',
    progress_percentage     DECIMAL(5,2) DEFAULT 0,

    -- Equipo responsable
    general_responsible_id  UUID NOT NULL, -- Debe existir un responsable general
    inventory_team          JSONB DEFAULT '[]'::jsonb,

    -- Configuración
    includes_missing        BOOLEAN DEFAULT true,
    includes_surplus        BOOLEAN DEFAULT true,
    requires_photos         BOOLEAN DEFAULT false,

    -- Observaciones y documentos
    observations            TEXT NULL,
    attached_documents      JSONB DEFAULT '[]'::jsonb,

    -- Auditoría
    created_by              UUID NOT NULL,
    created_at              TIMESTAMP DEFAULT NOW(),
    updated_by              UUID NULL,
    updated_at              TIMESTAMP DEFAULT NOW(),

    -- Validaciones
    CONSTRAINT chk_inventory_type CHECK (inventory_type IN (
        'GENERAL', 'SELECTIVE', 'SPECIAL', 'RECONCILIATION'
    )),
    CONSTRAINT chk_inventory_status CHECK (inventory_status IN (
        'PLANNED', 'IN_PROCESS', 'COMPLETED', 'SUSPENDED', 'CANCELLED'
    )),

    UNIQUE (municipality_id, inventory_number)
);

-- =============================================
-- TABLA DETALLE: physical_inventory_detail
-- =============================================
CREATE TABLE physical_inventory_detail (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    municipality_id         UUID NOT NULL,
    inventory_id            UUID NOT NULL, -- referencia lógica
    asset_id                UUID NULL,     -- puede estar vacío al inicio

    -- Estado del bien en el inventario
    found_status            VARCHAR(30) NOT NULL, -- FOUND, MISSING, SURPLUS
    actual_conservation_status VARCHAR(30) NULL,
    actual_location_id      UUID NULL,  -- puede estar vacío
    actual_responsible_id   UUID NULL,  -- puede estar vacío

    -- Verificación
    verified_by             UUID NULL, -- se llena cuando se verifica
    verification_date       TIMESTAMP NULL,
    observations            TEXT NULL,
    requires_action         BOOLEAN DEFAULT false,
    required_action         TEXT NULL,

    -- Fotos y evidencias
    photographs             JSONB DEFAULT '[]'::jsonb,
    additional_evidence     JSONB DEFAULT '[]'::jsonb,

    -- Diferencias encontradas
    physical_differences    TEXT NULL,
    document_differences    TEXT NULL,

    -- Auditoría
    created_at              TIMESTAMP DEFAULT NOW(),
    updated_at              TIMESTAMP DEFAULT NOW(),

    CONSTRAINT chk_found_status CHECK (found_status IN (
        'FOUND', 'MISSING', 'SURPLUS', 'DAMAGED'
    ))
);

