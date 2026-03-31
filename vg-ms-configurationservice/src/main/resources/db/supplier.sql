-- Habilitar la extensión necesaria para generar UUIDs
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ===============================
-- Tabla: document_types
-- ===============================
CREATE TABLE IF NOT EXISTS document_types (
    id SERIAL PRIMARY KEY,
    code VARCHAR(5) UNIQUE NOT NULL,
    description VARCHAR(100) NOT NULL,
    length INT NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- ===============================
-- Tabla: suppliers
-- ===============================
CREATE TABLE IF NOT EXISTS suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_types_id INT NOT NULL REFERENCES document_types(id) ON DELETE RESTRICT,
    numero_documento VARCHAR(20) NOT NULL,
    legal_name VARCHAR(200) NOT NULL, -- razón social
    trade_name VARCHAR(200),
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(200),
    website VARCHAR(300),
    main_contact VARCHAR(200),
    company_type VARCHAR(50), -- Tipo: S.A., S.A.C., S.R.L., E.I.R.L., S.A.A., PERSONA NATURAL
    is_state_provider BOOLEAN DEFAULT FALSE, -- si es proveedor del Estado
    classification VARCHAR(50), -- Clasificación: MICROEMPRESA, PEQUEÑA EMPRESA, MEDIANA EMPRESA, GRAN EMPRESA
    municipality_id UUID, -- referencia a municipio (será poblado desde otro microservicio)
    active BOOLEAN DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uk_supplier_document UNIQUE (document_types_id, numero_documento),
    CONSTRAINT chk_company_type CHECK (company_type IN ('S.A.', 'S.A.C.', 'S.R.L.', 'E.I.R.L.', 'S.A.A.', 'PERSONA NATURAL', 'ASOCIACION', 'FUNDACION', 'ONG')),
    CONSTRAINT chk_classification CHECK (classification IN ('MICROEMPRESA', 'PEQUEÑA EMPRESA', 'MEDIANA EMPRESA', 'GRAN EMPRESA'))
);

-- Índices recomendados
CREATE INDEX IF NOT EXISTS idx_suppliers_document_type ON suppliers (document_types_id);
CREATE INDEX IF NOT EXISTS idx_suppliers_active ON suppliers (active);
CREATE INDEX IF NOT EXISTS idx_suppliers_classification ON suppliers (classification);
CREATE INDEX IF NOT EXISTS idx_suppliers_municipality ON suppliers (municipality_id);

-- Inserción opcional de tipos de documento base
INSERT INTO document_types (code, description, length, active)
VALUES 
('RUC', 'Registro Único de Contribuyentes', 11, TRUE),
('DNI', 'Documento Nacional de Identidad', 8, TRUE),
('CE', 'Carné de Extranjería', 9, TRUE)
ON CONFLICT (code) DO NOTHING;