-- Persona de ejemplo
CREATE TABLE IF NOT EXISTS person (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

-- Extensión para generar UUIDs (requiere permisos en la base de datos)
-- Nota: algunas plataformas gestionadas (Neon) no permiten crear extensiones.
-- Eliminamos la creación de extensión y NO usamos gen_random_uuid() por defecto.
-- La aplicación debe generar el UUID y enviarlo en INSERTs, o usar la función
-- del servidor si tu provider la habilita.
-- Municipalities table (English schema)
CREATE TABLE IF NOT EXISTS municipalities (
    -- Primary key (Identificador único universal)
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- name: Nombre completo de la municipalidad
    name                VARCHAR(255) NOT NULL,

    -- ruc: Registro Único de Contribuyente de la entidad
    ruc                 VARCHAR(11) UNIQUE,

    -- ubigeo_code: Código de Ubicación Geográfica oficial del INEI (6 dígitos)
    ubigeo_code         VARCHAR(6) UNIQUE NOT NULL,

    -- municipality_type: Tipo de municipalidad (PROVINCIAL, DISTRICT o TOWN_CENTER)
    municipality_type   VARCHAR(20) NOT NULL DEFAULT 'DISTRICT',

    -- department: Departamento donde se ubica la municipalidad
    department          VARCHAR(100) NOT NULL,

    -- province: Provincia donde se ubica la municipalidad
    province            VARCHAR(100) NOT NULL,

    -- district: Distrito donde se ubica la municipalidad
    district            VARCHAR(100) NOT NULL,

    -- address: Dirección completa de la sede municipal
    address             TEXT,

    -- phone_number: Número de teléfono fijo de contacto
    phone_number        VARCHAR(50),

    -- mobile_number: Número de celular de contacto (opcional)
    mobile_number       VARCHAR(20),

    -- email: Correo electrónico institucional de la municipalidad
    email               VARCHAR(255),

    -- website: Sitio web oficial de la municipalidad
    website             VARCHAR(300),

    -- mayor_name: Nombre completo del alcalde en gestión
    mayor_name          VARCHAR(255),

    -- is_active: Indica si la municipalidad está activa en el sistema
    is_active           BOOLEAN DEFAULT true,

    -- created_at: Fecha y hora de creación del registro
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- updated_at: Fecha y hora de la última actualización del registro
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- Restricción para asegurar los valores válidos del tipo de municipalidad
    CONSTRAINT chk_municipality_type CHECK (municipality_type IN ('PROVINCIAL', 'DISTRICT', 'TOWN_CENTER'))
);

-- Índices para optimizar búsquedas comunes
CREATE INDEX IF NOT EXISTS idx_municipalities_type ON municipalities(municipality_type);
CREATE INDEX IF NOT EXISTS idx_municipalities_ubigeo ON municipalities(ubigeo_code);
CREATE INDEX IF NOT EXISTS idx_municipalities_province ON municipalities(province);

-- Comentarios sobre las columnas
COMMENT ON COLUMN municipalities.ubigeo_code IS 'Código de Ubicación Geográfica oficial del INEI (6 dígitos).';
COMMENT ON COLUMN municipalities.municipality_type IS 'Tipo de municipalidad según la Ley Orgánica de Municipalidades.';
COMMENT ON COLUMN municipalities.mayor_name IS 'Nombre completo del alcalde en gestión.';

-- Tabla de configuración por tenant
CREATE TABLE IF NOT EXISTS configuracion_tenant (
  id UUID PRIMARY KEY,
  municipality_id UUID NOT NULL,
  nombre_sistema VARCHAR(200) DEFAULT 'Sistema de Control Patrimonial',
  logo_url VARCHAR(500),
  colores_tema JSONB DEFAULT '{"primary": "#1976d2", "secondary": "#dc004e"}'::jsonb,
  configuracion_reportes JSONB DEFAULT '{}'::jsonb,
  parametros_negocio JSONB DEFAULT '{}'::jsonb,
  timezone VARCHAR(50) DEFAULT 'America/Lima',
  moneda_default VARCHAR(3) DEFAULT 'PEN',
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
