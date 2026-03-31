-- =====================================================
-- MIGRACIÓN: Eliminar columna middle_name de persons
-- =====================================================
-- Este script elimina la columna middle_name de la tabla persons
-- Ejecutar solo si ya tienes una base de datos existente

-- Eliminar la columna middle_name
ALTER TABLE persons DROP COLUMN IF EXISTS middle_name;

-- Verificar que la columna fue eliminada
-- SELECT column_name FROM information_schema.columns 
-- WHERE table_name = 'persons' AND column_name = 'middle_name';
