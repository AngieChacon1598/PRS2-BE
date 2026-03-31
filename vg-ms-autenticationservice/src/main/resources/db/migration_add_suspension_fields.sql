-- ===============================================
-- MIGRACIÓN: Agregar campos de suspensión y bloqueo
-- ===============================================

-- Agregar campos de bloqueo
ALTER TABLE users ADD COLUMN IF NOT EXISTS block_reason TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS block_start TIMESTAMP;

-- Agregar campos de suspensión
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspension_reason TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspension_start TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspension_end TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspended_by UUID;

-- Agregar foreign key para suspended_by (opcional)
-- ALTER TABLE users ADD CONSTRAINT fk_users_suspended_by 
--     FOREIGN KEY (suspended_by) REFERENCES users(id);

-- Verificar los cambios
SELECT 
    column_name, 
    data_type,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name IN ('block_reason', 'block_start', 'suspension_reason', 'suspension_start', 'suspension_end', 'suspended_by')
ORDER BY column_name;
