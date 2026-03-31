CREATE TABLE position_allowed_roles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    position_id     UUID NOT NULL,
    area_id         UUID,                -- NULL = aplica a cualquier área
    role_id         UUID NOT NULL,
    is_default      BOOLEAN DEFAULT false,
    municipality_id UUID NOT NULL,
    created_by      UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW(),
    CONSTRAINT uk_position_area_role UNIQUE (position_id, area_id, role_id, municipality_id)
);
