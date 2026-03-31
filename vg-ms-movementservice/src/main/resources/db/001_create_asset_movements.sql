-- =================================================================
-- TABLE: asset_movements
-- Microservice: MS-05 Movements Service
-- Port: 5005
-- Database: tenant_specific
-- =================================================================
-- Script to create the asset_movements table for asset movement management
-- This table manages complete traceability of asset movements by municipality
-- Includes soft delete functionality (active field)
-- =================================================================

-- Enable UUID extension if not already enabled
-- For PostgreSQL 13+: gen_random_uuid() is built-in, pgcrypto provides it for older versions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =================================================================
-- CREATE TABLE: asset_movements
-- =================================================================

CREATE TABLE IF NOT EXISTS asset_movements (
    -- Primary key
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Municipality/tenant identification
    municipality_id         UUID NOT NULL,
    
    -- Movement identification
    movement_number         VARCHAR(50) UNIQUE NOT NULL,
    
    -- Asset and movement type
    asset_id                UUID NOT NULL,
    movement_type           VARCHAR(50) NOT NULL,
    movement_subtype        VARCHAR(50),
    
    -- Origin and destination
    origin_responsible_id   UUID,
    destination_responsible_id UUID,
    origin_area_id          UUID,
    destination_area_id     UUID,
    origin_location_id      UUID,
    destination_location_id UUID,
    
    -- Process dates
    request_date            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approval_date           TIMESTAMP,
    execution_date          TIMESTAMP,
    reception_date          TIMESTAMP,
    
    -- Flow control
    movement_status         VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    requires_approval       BOOLEAN NOT NULL DEFAULT true,
    approved_by             UUID,
    
    -- Movement information
    reason                  TEXT NOT NULL,
    observations            TEXT,
    special_conditions      TEXT,
    
    -- Documents and evidences
    supporting_document_number VARCHAR(100),
    supporting_document_type VARCHAR(50),
    attached_documents      JSONB DEFAULT '[]'::jsonb,
    
    -- Audit fields
    requesting_user         UUID NOT NULL,
    executing_user          UUID,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP,
    
    -- Soft delete fields
    active                  BOOLEAN NOT NULL DEFAULT true,
    deleted_by              UUID,
    deleted_at              TIMESTAMP,
    restored_by             UUID,
    restored_at             TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_movement_type CHECK (movement_type IN (
        'INITIAL_ASSIGNMENT', 'REASSIGNMENT', 'AREA_TRANSFER',
        'EXTERNAL_TRANSFER', 'RETURN', 'LOAN',
        'MAINTENANCE', 'REPAIR', 'TEMPORARY_DISPOSAL'
    )),
    CONSTRAINT chk_movement_status CHECK (movement_status IN (
        'REQUESTED', 'APPROVED', 'REJECTED', 'IN_PROCESS',
        'COMPLETED', 'CANCELLED', 'PARTIAL'
    ))
    
    -- Foreign keys (commented out - uncomment when related tables exist)
    -- FOREIGN KEY (asset_id) REFERENCES assets(id),
    -- FOREIGN KEY (origin_responsible_id) REFERENCES users(id),
    -- FOREIGN KEY (destination_responsible_id) REFERENCES users(id),
    -- FOREIGN KEY (origin_area_id) REFERENCES areas(id),
    -- FOREIGN KEY (destination_area_id) REFERENCES areas(id),
    -- FOREIGN KEY (origin_location_id) REFERENCES physical_locations(id),
    -- FOREIGN KEY (destination_location_id) REFERENCES physical_locations(id),
    -- FOREIGN KEY (approved_by) REFERENCES users(id),
    -- FOREIGN KEY (requesting_user) REFERENCES users(id),
    -- FOREIGN KEY (executing_user) REFERENCES users(id),
    -- FOREIGN KEY (deleted_by) REFERENCES users(id),
    -- FOREIGN KEY (restored_by) REFERENCES users(id)
);

-- =================================================================
-- INDEXES FOR OPTIMIZATION
-- =================================================================

-- Index for municipality queries (most common)
CREATE INDEX IF NOT EXISTS idx_asset_movements_municipality_id 
    ON asset_movements(municipality_id);

-- Index for active movements filter (critical for soft delete)
CREATE INDEX IF NOT EXISTS idx_asset_movements_municipality_active 
    ON asset_movements(municipality_id, active) 
    WHERE active = true;

-- Index for asset queries
CREATE INDEX IF NOT EXISTS idx_asset_movements_asset_id 
    ON asset_movements(asset_id);

-- Index for movement number lookups (unique but index helps)
CREATE INDEX IF NOT EXISTS idx_asset_movements_movement_number 
    ON asset_movements(movement_number);

-- Index for status queries
CREATE INDEX IF NOT EXISTS idx_asset_movements_status 
    ON asset_movements(movement_status, municipality_id, active);

-- Index for movement type queries
CREATE INDEX IF NOT EXISTS idx_asset_movements_type 
    ON asset_movements(movement_type, municipality_id, active);

-- Index for request date ordering
CREATE INDEX IF NOT EXISTS idx_asset_movements_request_date 
    ON asset_movements(request_date DESC);

-- Index for destination responsible queries
CREATE INDEX IF NOT EXISTS idx_asset_movements_destination_responsible 
    ON asset_movements(destination_responsible_id, municipality_id, active);

-- Index for origin responsible queries
CREATE INDEX IF NOT EXISTS idx_asset_movements_origin_responsible 
    ON asset_movements(origin_responsible_id, municipality_id, active);

-- Index for destination area queries
CREATE INDEX IF NOT EXISTS idx_asset_movements_destination_area 
    ON asset_movements(destination_area_id, municipality_id, active);

-- Index for origin area queries
CREATE INDEX IF NOT EXISTS idx_asset_movements_origin_area 
    ON asset_movements(origin_area_id, municipality_id, active);

-- Index for pending approval queries
CREATE INDEX IF NOT EXISTS idx_asset_movements_pending_approval 
    ON asset_movements(municipality_id, movement_status, requires_approval, active) 
    WHERE movement_status = 'REQUESTED' AND requires_approval = true AND active = true;

-- Index for deleted movements queries
CREATE INDEX IF NOT EXISTS idx_asset_movements_deleted 
    ON asset_movements(municipality_id, active, deleted_at DESC) 
    WHERE active = false;

-- =================================================================
-- TABLE COMMENTS AND DOCUMENTATION
-- =================================================================

COMMENT ON TABLE asset_movements IS 
'Table that manages complete traceability of asset movements by municipality. Supports soft delete functionality.';

COMMENT ON COLUMN asset_movements.id IS 'Primary key - UUID';
COMMENT ON COLUMN asset_movements.municipality_id IS 'Municipality identifier (multi-tenant support)';
COMMENT ON COLUMN asset_movements.movement_number IS 'Unique movement number for identification';
COMMENT ON COLUMN asset_movements.asset_id IS 'Reference to the asset that is being moved';
COMMENT ON COLUMN asset_movements.movement_type IS 'Type of movement: INITIAL_ASSIGNMENT, REASSIGNMENT, AREA_TRANSFER, etc.';
COMMENT ON COLUMN asset_movements.movement_status IS 'Current status: REQUESTED, APPROVED, REJECTED, IN_PROCESS, COMPLETED, CANCELLED, PARTIAL';
COMMENT ON COLUMN asset_movements.active IS 'Soft delete flag: true = active, false = deleted';
COMMENT ON COLUMN asset_movements.deleted_by IS 'User who deleted the movement (soft delete)';
COMMENT ON COLUMN asset_movements.deleted_at IS 'Date and time when the movement was deleted (soft delete)';
COMMENT ON COLUMN asset_movements.restored_by IS 'User who restored the movement';
COMMENT ON COLUMN asset_movements.restored_at IS 'Date and time when the movement was restored';
COMMENT ON COLUMN asset_movements.attached_documents IS 'JSON array of attached documents (JSONB)';

-- =================================================================
-- END OF SCRIPT
-- =================================================================
