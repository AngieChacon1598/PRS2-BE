-- Inserts de ejemplo para tenant_configuration

INSERT INTO tenant_configuration (municipality_id, system_name, logo_url, theme_colors, reports_configuration, business_parameters, default_currency, timezone, active)
VALUES 
(gen_random_uuid(), 'Asset Control System - Lima', 'https://example.com/logos/lima.png', '{"primary": "#1976d2", "secondary": "#dc004e", "accent": "#ff9800"}', '{"format": "PDF", "orientation": "portrait"}', '{"annual_depreciation": 10, "minimum_asset_value": 500}', 'PEN', 'America/Lima', true),

(gen_random_uuid(), 'Asset Control System - Arequipa', 'https://example.com/logos/arequipa.png', '{"primary": "#2196f3", "secondary": "#f50057"}', '{"format": "EXCEL", "orientation": "landscape"}', '{"annual_depreciation": 12, "minimum_asset_value": 300}', 'PEN', 'America/Lima', true),

(gen_random_uuid(), 'Asset Control System - Cusco', 'https://example.com/logos/cusco.png', '{"primary": "#4caf50", "secondary": "#ff5722"}', '{"format": "PDF", "orientation": "portrait", "include_charts": true}', '{"annual_depreciation": 8, "minimum_asset_value": 400, "inventory_period": "biannual"}', 'PEN', 'America/Lima', true),

(gen_random_uuid(), 'Asset Control System - Trujillo', NULL, '{"primary": "#9c27b0", "secondary": "#00bcd4"}', '{}', '{"annual_depreciation": 10}', 'PEN', 'America/Lima', false);
