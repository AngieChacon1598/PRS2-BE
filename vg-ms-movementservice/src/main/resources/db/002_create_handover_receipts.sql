CREATE TABLE handover_receipts (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    municipality_id         UUID NOT NULL,
    receipt_number          VARCHAR(50) UNIQUE NOT NULL,
    movement_id             UUID NOT NULL,
    -- Receipt participants
    delivering_responsible_id UUID NOT NULL,
    receiving_responsible_id UUID NOT NULL,
    witness_1_id            UUID,
    witness_2_id            UUID,
    -- Dates
    receipt_date            DATE NOT NULL DEFAULT CURRENT_DATE,
    delivery_signature_date TIMESTAMP,
    reception_signature_date TIMESTAMP,
    -- Status and documents
    receipt_status          VARCHAR(30) DEFAULT 'GENERATED',
    delivery_observations   TEXT,
    reception_observations  TEXT,
    special_conditions      TEXT,
    -- Files
    pdf_document_path       VARCHAR(500),
    digital_signatures      JSONB DEFAULT '{}'::jsonb,
    -- Audit
    generated_by            UUID NOT NULL,
    created_at              TIMESTAMP DEFAULT NOW(),
    updated_at              TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_receipt_status CHECK (receipt_status IN ('GENERATED', 'PARTIALLY_SIGNED', 'FULLY_SIGNED', 'VOIDED')),
    FOREIGN KEY (movement_id) REFERENCES asset_movements(id),
    FOREIGN KEY (delivering_responsible_id) REFERENCES users(id),
    FOREIGN KEY (receiving_responsible_id) REFERENCES users(id),
    FOREIGN KEY (witness_1_id) REFERENCES users(id),
    FOREIGN KEY (witness_2_id) REFERENCES users(id),
    FOREIGN KEY (generated_by) REFERENCES users(id)
);