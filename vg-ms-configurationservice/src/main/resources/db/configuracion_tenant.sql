CREATE TABLE tenant_configuration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    municipality_id UUID NOT NULL,
    system_name VARCHAR(200) DEFAULT 'Asset Control System',
    logo_url VARCHAR(500),
    theme_colors TEXT DEFAULT '{"primary": "#1976d2", "secondary": "#dc004e"}',
    reports_configuration TEXT DEFAULT '{}',
    business_parameters TEXT DEFAULT '{}',
    default_currency VARCHAR(3) DEFAULT 'PEN',
    timezone VARCHAR(50) DEFAULT 'America/Lima',
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
