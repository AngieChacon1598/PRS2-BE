CREATE TABLE IF NOT EXISTS assets (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    municipality_id         UUID NOT NULL,
    asset_code              VARCHAR(30) UNIQUE NOT NULL,
    internal_code           VARCHAR(50),
    sbn_code                VARCHAR(50),
    description             TEXT NOT NULL,
    category_id             UUID,
    subcategory_id          UUID,
    brand                   VARCHAR(100),
    model                   VARCHAR(100),
    serial_number           VARCHAR(100),
    useful_life             INTEGER,                     -- AGREGADO: vidaUtil
    asset_plate             VARCHAR(20),
    qr_code                 VARCHAR(100),                -- AGREGADO: codigoQr
    barcode                 VARCHAR(100),                -- AGREGADO: codigoBarras
    rfid_tag                VARCHAR(100),                -- AGREGADO: etiquetaRfid
    color                   VARCHAR(50),
    dimensions              VARCHAR(200),
    weight                  DECIMAL(10,3),
    material                VARCHAR(100),
    supplier_id             UUID,
    acquisition_date        DATE NOT NULL,
    acquisition_type        VARCHAR(30) DEFAULT 'PURCHASE',
    invoice_number          VARCHAR(100),
    purchase_order_number   VARCHAR(100),
    pecosa_number           VARCHAR(100),
    acquisition_value       DECIMAL(15,2) NOT NULL,
    currency                VARCHAR(3) DEFAULT 'PEN',
    current_value           DECIMAL(15,2),
    residual_value          DECIMAL(15,2) DEFAULT 0,
    accumulated_depreciation DECIMAL(15,2) DEFAULT 0,
    asset_status            VARCHAR(30) DEFAULT 'AVAILABLE',
    conservation_status     VARCHAR(30) DEFAULT 'GOOD',
    current_location_id     UUID,
    current_responsible_id  UUID,
    current_area_id         UUID,
    entry_date              TIMESTAMP NOT NULL DEFAULT NOW(),
    last_inventory_date     DATE,
    next_depreciation_date  DATE,
    warranty_expiration_date DATE,
    observations            TEXT,
    technical_specifications TEXT,
    attached_documents      TEXT,
    custom_fields           TEXT,
    image_url               VARCHAR(500),           -- URL imagen del bien en Supabase Storage
    is_inventoriable        BOOLEAN DEFAULT true,
    requires_maintenance    BOOLEAN DEFAULT false,
    is_depreciable          BOOLEAN DEFAULT true,
    created_by              UUID NOT NULL,
    created_at              TIMESTAMP DEFAULT NOW(),
    updated_by              UUID,
    updated_at              TIMESTAMP DEFAULT NOW(),
    version                 INTEGER DEFAULT 1
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_assets_patrimonial_code ON assets(asset_code);
CREATE INDEX IF NOT EXISTS idx_assets_asset_status ON assets(asset_status);
CREATE INDEX IF NOT EXISTS idx_assets_category_id ON assets(category_id);



CREATE OR REPLACE FUNCTION set_entry_date()
RETURNS TRIGGER AS $$
BEGIN
    -- Si no se proporcionó entry_date, asignamos la fecha y hora actual
    IF NEW.entry_date IS NULL THEN
        NEW.entry_date := NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TABLE IF NOT EXISTS depreciations (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Basic Asset Data
    asset_id                UUID NOT NULL, -- bien_patrimonial_id

    -- Calculation Period
    fiscal_year             INTEGER NOT NULL, -- periodo_fiscal
    calculation_month       INTEGER NOT NULL, -- mes_calculo

    -- Calculation Values
    initial_value           DECIMAL(15,2) NOT NULL, -- valor_inicial
    useful_life_years       INTEGER NOT NULL,       -- vida_util_anos
    residual_value          DECIMAL(15,2) DEFAULT 0, -- valor_residual

    -- Calculated Depreciation
    annual_depreciation     DECIMAL(15,2) NOT NULL, -- depreciacion_anual
    monthly_depreciation    DECIMAL(15,2) NOT NULL, -- depreciacion_mensual
    previous_accumulated_depreciation DECIMAL(15,2) DEFAULT 0, -- depreciacion_acumulada_anterior
    period_depreciation     DECIMAL(15,2) NOT NULL, -- depreciacion_periodo
    current_accumulated_depreciation DECIMAL(15,2) NOT NULL, -- depreciacion_acumulada_actual

    -- Book Value
    previous_book_value     DECIMAL(15,2) NOT NULL, -- valor_neto_anterior (Valor Neto Anterior)
    current_book_value      DECIMAL(15,2) NOT NULL, -- valor_neto_actual (Valor Neto Actual)

    -- Calculation Status
    calculation_status      VARCHAR(30) DEFAULT 'CALCULATED', -- estado_calculo
    depreciation_method     VARCHAR(30) DEFAULT 'STRAIGHT_LINE', -- metodo_depreciacion (LINEAL)

    -- Observations
    observations            TEXT,

    -- Basic Audit
    calculated_by           UUID,
    approved_by             UUID,
    calculation_date        TIMESTAMP DEFAULT NOW(), -- fecha_calculo
    approval_date           TIMESTAMP,             -- fecha_aprobacion

    -- Constraints and Relationships
    -- Foreign Key to the assets table (Relación con 'assets')
    CONSTRAINT fk_depreciation_asset FOREIGN KEY (asset_id)
        REFERENCES assets(id),

    -- Simple Rules
    CONSTRAINT uk_depreciation_period UNIQUE (asset_id, fiscal_year, calculation_month),
    CONSTRAINT chk_month_valid CHECK (calculation_month BETWEEN 1 AND 12),
    CONSTRAINT chk_calculation_status CHECK (calculation_status IN (
        'CALCULATED', 'APPROVED', 'REJECTED', 'ADJUSTED'
    )),
    CONSTRAINT chk_depreciation_method CHECK (depreciation_method IN (
        'STRAIGHT_LINE',         -- LINEAL
        'ACCELERATED',           -- ACELERADA
        'UNITS_OF_PRODUCTION'    -- UNIDADES_PRODUCCION
    ))
);


-- =================================================================
-- TABLE: asset_disposals
-- =================================================================
CREATE TABLE IF NOT EXISTS asset_disposals (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    municipality_id         UUID NOT NULL,
    file_number             VARCHAR(50) UNIQUE NOT NULL,
    
    -- General information
    disposal_type           VARCHAR(30) NOT NULL, -- ADMINISTRATIVE, TECHNICAL, FORTUITOUS
    disposal_reason         VARCHAR(50) NOT NULL,
    reason_description      TEXT NOT NULL,
    
    -- Process dates
    request_date            DATE NOT NULL DEFAULT CURRENT_DATE,
    technical_evaluation_date DATE,
    resolution_date         DATE,
    physical_removal_date   DATE,

    -- Technical report author
    technical_report_author_id UUID NOT NULL, -- Nuevo: quién elabora el informe técnico
    technical_report        TEXT,
    
    -- Approval
    approved_by_id          UUID, -- Nuevo: quién aprueba o rechaza (administrador de finanzas)
    approval_date           DATE,
    file_status             VARCHAR(30) DEFAULT 'INITIATED',
    
    resolution_number       VARCHAR(100),
    supporting_documents    TEXT DEFAULT '[]',
    
    observations            TEXT,
    requires_destruction    BOOLEAN DEFAULT false,
    allows_donation         BOOLEAN DEFAULT false,
    recoverable_value       DECIMAL(15,2) DEFAULT 0,
    
    -- Audit
    requested_by            UUID NOT NULL, -- quién solicitó la baja
    created_at              TIMESTAMP DEFAULT NOW(),
    updated_by              UUID,
    updated_at              TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT chk_disposal_type CHECK (disposal_type IN (
        'ADMINISTRATIVE', 'TECHNICAL', 'FORTUITOUS', 'OBSOLESCENCE'
    )),
    CONSTRAINT chk_file_status CHECK (file_status IN (
        'INITIATED', 'UNDER_EVALUATION', 'APPROVED', 'REJECTED', 
        'EXECUTED', 'CANCELLED'
    ))
);

CREATE TABLE IF NOT EXISTS asset_disposal_detail (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    municipality_id         UUID NOT NULL,
    disposal_id             UUID NOT NULL,
    asset_id                UUID NOT NULL,
    
    conservation_status     VARCHAR(30) NOT NULL,
    book_value              DECIMAL(15,2) NOT NULL,
    recoverable_value       DECIMAL(15,2) DEFAULT 0,
    
    -- Informe técnico individual
    technical_opinion       TEXT,
    recommendation          VARCHAR(50), -- DESTROY, DONATE, SELL, RECYCLE
    
    removal_date            DATE,
    removal_responsible_id  UUID,
    final_destination       VARCHAR(100),
    
    observations            TEXT,
    condition_photographs   JSONB DEFAULT '[]'::jsonb,
    
    created_at              TIMESTAMP DEFAULT NOW(),
    updated_at              TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT chk_recommendation CHECK (recommendation IN (
        'DESTROY', 'DONATE', 'SELL', 'RECYCLE', 'TRANSFER'
    )),
    
    FOREIGN KEY (disposal_id) REFERENCES asset_disposals(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES assets(id)
);

