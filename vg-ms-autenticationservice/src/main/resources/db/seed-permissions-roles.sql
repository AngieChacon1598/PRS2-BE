-- ================================================================
-- SEED: permissions y roles_permissions
-- ================================================================
-- NOTA: Este script asume que los roles ya están creados en la tabla 'roles'.
-- Como 'municipal_code' y 'created_by' ahora permiten NULL en tu esquema, 
-- no los estamos insertando asumiendo que estos son permisos y asignaciones a nivel sistema global.
-- ================================================================

-- 1. Insertar Permisos
-- Usamos ON CONFLICT para evitar errores si lo ejecutas más de una vez.
-- Sin embargo, asegúrate de que tus permisos actuales que mostraste en la imagen 
-- no tengan conflictos o prefieras limpiar la tabla de permisos antes de correr esto.

INSERT INTO permissions (module, action, resource, display_name, description) VALUES
-- Administración Global
('sidebar', 'view', 'admin',          'Ver Administración Global',    'Acceso al módulo de administración de la plataforma'),

-- Gestión de Activos
('sidebar', 'view', 'bienes',         'Ver Bienes',                   'Acceso al catálogo de bienes patrimoniales'),
('sidebar', 'view', 'categorias',     'Ver Categorías',               'Acceso al módulo de categorías de bienes'),
('sidebar', 'view', 'proveedores',    'Ver Proveedores',              'Acceso al módulo de proveedores'),
('sidebar', 'view', 'ubicaciones',    'Ver Ubicaciones Físicas',      'Acceso al módulo de ubicaciones físicas'),

-- Operaciones
('sidebar', 'view', 'movimientos',    'Ver Movimientos',              'Acceso al módulo de movimientos de bienes'),
('sidebar', 'view', 'actas',          'Ver Actas de Entrega',         'Acceso al módulo de actas de entrega-recepción'),
('sidebar', 'view', 'inventarios',    'Ver Inventarios',              'Acceso al módulo de inventarios físicos'),
('sidebar', 'view', 'mantenimientos', 'Ver Mantenimientos',           'Acceso al módulo de mantenimiento de bienes'),

-- Usuarios y Seguridad
('sidebar', 'view', 'usuarios',       'Ver Usuarios',                 'Acceso al módulo de gestión de usuarios'),
('sidebar', 'view', 'personas',       'Ver Personas',                 'Acceso al módulo de personas'),
('sidebar', 'view', 'roles',          'Ver Roles',                    'Acceso al módulo de roles del sistema'),
('sidebar', 'view', 'permisos',       'Ver Permisos',                 'Acceso al módulo de permisos del sistema'),
('sidebar', 'view', 'areas',          'Ver Áreas',                    'Acceso al módulo de áreas organizacionales'),
('sidebar', 'view', 'cargos',         'Ver Cargos',                   'Acceso al módulo de cargos'),

-- Contabilidad
('sidebar', 'view', 'bajas',          'Ver Bajas de Activos',         'Acceso al módulo de bajas de bienes patrimoniales'),
('sidebar', 'view', 'valores',        'Ver Historial de Valores',     'Acceso al historial de valorización de bienes'),

-- Reportes y Auditoría
('sidebar', 'view', 'reportes',       'Ver Reportes',                 'Acceso al módulo de reportes regulatorios'),
('sidebar', 'view', 'auditoria',      'Ver Auditoría',                'Acceso al módulo de auditoría y trazabilidad'),
('sidebar', 'view', 'notificaciones', 'Ver Notificaciones',           'Acceso al módulo de alertas y notificaciones'),

-- Configuración
('sidebar', 'view', 'sistema',        'Ver Configuración del Sistema','Acceso a la configuración general del tenant'),

-- PERMISOS OPERATIVOS — PATRIMONIO
('patrimonio', 'create',      NULL,     'Registrar bien',             'Crear nuevos bienes patrimoniales'),
('patrimonio', 'read',        NULL,     'Ver bienes',                 'Listar y consultar el catálogo de bienes'),
('patrimonio', 'update',      NULL,     'Editar bien',                'Actualizar datos de bienes patrimoniales'),
('patrimonio', 'update',      'status', 'Actualizar estado del bien', 'Cambiar el estado de un bien (en uso, en almacén, etc.)'),
('patrimonio', 'delete',      NULL,     'Eliminar bien',              'Eliminar bienes del catálogo (solo casos excepcionales)'),
('patrimonio', 'depreciation',NULL,     'Calcular depreciación',      'Ejecutar cálculo de depreciación de bienes'),
('patrimonio', 'baja',        NULL,     'Tramitar baja',              'Iniciar expediente de baja de un bien patrimonial'),

-- PERMISOS OPERATIVOS — MOVIMIENTOS
('movimientos', 'create',  NULL,  'Crear solicitud de movimiento',  'Iniciar solicitud de traslado, asignación o transferencia'),
('movimientos', 'read',    NULL,  'Ver todos los movimientos',      'Consultar historial completo de movimientos'),
('movimientos', 'read',    'own', 'Ver mis movimientos',            'Consultar solo los movimientos propios o del área'),
('movimientos', 'approve', NULL,  'Aprobar movimiento',             'Aprobar solicitudes de movimiento de bienes'),
('movimientos', 'reject',  NULL,  'Rechazar movimiento',            'Rechazar solicitudes de movimiento con observación'),
('movimientos', 'acta',    'generate', 'Generar acta',              'Generar acta de entrega-recepción de bienes'),

-- PERMISOS OPERATIVOS — INVENTARIO
('inventario', 'create',      NULL,     'Crear inventario',          'Programar nuevo inventario físico'),
('inventario', 'read',        NULL,     'Ver inventarios',           'Consultar todos los inventarios'),
('inventario', 'read',        'active', 'Ver inventario activo',     'Consultar solo el inventario en curso'),
('inventario', 'update',      NULL,     'Editar inventario',         'Modificar datos del inventario'),
('inventario', 'verify',      'item',   'Verificar ítem',            'Marcar bienes como verificados durante el inventario'),
('inventario', 'conciliate',  NULL,     'Conciliar inventario',      'Conciliar diferencias entre inventario físico y contable'),
('inventario', 'close',       NULL,     'Cerrar inventario',         'Cerrar y firmar el inventario físico'),

-- PERMISOS OPERATIVOS — MANTENIMIENTO
('mantenimiento', 'create',  NULL,       'Programar mantenimiento',  'Crear órdenes de mantenimiento preventivo o correctivo'),
('mantenimiento', 'read',    NULL,       'Ver mantenimientos',       'Consultar historial y calendario de mantenimientos'),
('mantenimiento', 'update',  NULL,       'Editar mantenimiento',     'Actualizar datos de una orden de mantenimiento'),
('mantenimiento', 'close',   NULL,       'Cerrar mantenimiento',     'Marcar una orden de mantenimiento como ejecutada'),
('mantenimiento', 'alert',   'configure','Configurar alertas',       'Configurar alertas de vencimiento de mantenimiento'),

-- PERMISOS OPERATIVOS — REPORTES
('reportes', 'read',     NULL, 'Ver reportes',                      'Acceder a dashboards y reportes regulatorios'),
('reportes', 'generate', NULL, 'Generar reporte',                   'Ejecutar generación de reportes bajo demanda'),
('reportes', 'export',   NULL, 'Exportar reporte',                  'Exportar reportes en PDF, Excel u otros formatos'),
('reportes', 'schedule', NULL, 'Programar reporte',                 'Configurar reportes automáticos programados'),

-- PERMISOS OPERATIVOS — AUDITORÍA
('auditoria', 'read', NULL, 'Ver auditoría',                        'Consultar logs de auditoria_cambios y auditoria_accesos'),

-- PERMISOS OPERATIVOS — CONFIGURACIÓN
('config', 'read',              NULL,     'Ver configuración',           'Consultar configuración del tenant'),
('config', 'update',            NULL,     'Editar configuración',        'Modificar parámetros de configuración del tenant'),
('config', 'areas',             'manage', 'Gestionar áreas',             'Crear, editar y desactivar áreas organizacionales'),
('config', 'categories',        'manage', 'Gestionar categorías',        'Crear y editar categorías de bienes'),
('config', 'locations',         'manage', 'Gestionar ubicaciones',       'Crear y editar ubicaciones físicas'),

-- PERMISOS OPERATIVOS — USUARIOS Y SEGURIDAD
('users', 'manage',  NULL, 'Gestionar usuarios',                    'Crear, editar, suspender y desactivar usuarios'),
('roles', 'manage',  NULL, 'Gestionar roles',                       'Crear y editar roles custom del tenant'),
('roles', 'assign',  NULL, 'Asignar roles',                         'Asignar y revocar roles a usuarios')
ON CONFLICT (module, action, resource) DO NOTHING;


-- 2. Insertar mapeo de Roles y Permisos (roles_permissions)

-- PATRIMONIO_VIEWER
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'PATRIMONIO_VIEWER' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view',  'bienes'),
    ('patrimonio', 'read',  '')
) ON CONFLICT DO NOTHING;

-- PATRIMONIO_OPERARIO
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'PATRIMONIO_OPERARIO' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view',   'bienes'),
    ('sidebar',    'view',   'movimientos'),
    ('patrimonio', 'create', ''),
    ('patrimonio', 'read',   ''),
    ('patrimonio', 'update', 'status'),
    ('movimientos','create', ''),
    ('movimientos','read',   'own')
) ON CONFLICT DO NOTHING;

-- PATRIMONIO_GESTOR
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'PATRIMONIO_GESTOR' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view',         'bienes'),
    ('sidebar',    'view',         'categorias'),
    ('sidebar',    'view',         'proveedores'),
    ('sidebar',    'view',         'ubicaciones'),
    ('sidebar',    'view',         'movimientos'),
    ('sidebar',    'view',         'inventarios'),
    ('sidebar',    'view',         'bajas'),
    ('sidebar',    'view',         'reportes'),
    ('patrimonio', 'create',       ''),
    ('patrimonio', 'read',         ''),
    ('patrimonio', 'update',       ''),
    ('patrimonio', 'update',       'status'),
    ('patrimonio', 'delete',       ''),
    ('patrimonio', 'depreciation', ''),
    ('patrimonio', 'baja',         ''),
    ('movimientos','create',       ''),
    ('movimientos','read',         ''),
    ('movimientos','read',         'own'),
    ('inventario', 'read',         ''),
    ('inventario', 'read',         'active'),
    ('reportes',   'read',         ''),
    ('reportes',   'generate',     ''),
    ('reportes',   'export',       '')
) ON CONFLICT DO NOTHING;

-- MOVIMIENTOS_SOLICITANTE
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'MOVIMIENTOS_SOLICITANTE' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view',   'movimientos'),
    ('sidebar',    'view',   'bienes'),
    ('movimientos','create', ''),
    ('movimientos','read',   'own'),
    ('patrimonio', 'read',   '')
) ON CONFLICT DO NOTHING;

-- MOVIMIENTOS_VIEWER
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'MOVIMIENTOS_VIEWER' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view', 'movimientos'),
    ('movimientos','read', '')
) ON CONFLICT DO NOTHING;

-- MOVIMIENTOS_APROBADOR
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'MOVIMIENTOS_APROBADOR' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',    'view',      'movimientos'),
    ('sidebar',    'view',      'actas'),
    ('sidebar',    'view',      'bienes'),
    ('sidebar',    'view',      'reportes'),
    ('movimientos','create',    ''),
    ('movimientos','read',      ''),
    ('movimientos','read',      'own'),
    ('movimientos','approve',   ''),
    ('movimientos','reject',    ''),
    ('movimientos','acta',      'generate'),
    ('patrimonio', 'read',      ''),
    ('reportes',   'read',      ''),
    ('reportes',   'generate',  ''),
    ('reportes',   'export',    '')
) ON CONFLICT DO NOTHING;

-- INVENTARIO_VERIFICADOR
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'INVENTARIO_VERIFICADOR' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',   'view',   'inventarios'),
    ('sidebar',   'view',   'bienes'),
    ('inventario','read',   'active'),
    ('inventario','verify', 'item'),
    ('patrimonio','read',   '')
) ON CONFLICT DO NOTHING;

-- INVENTARIO_COORDINADOR
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'INVENTARIO_COORDINADOR' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',   'view',       'inventarios'),
    ('sidebar',   'view',       'bienes'),
    ('sidebar',   'view',       'reportes'),
    ('inventario','create',     ''),
    ('inventario','read',       ''),
    ('inventario','read',       'active'),
    ('inventario','update',     ''),
    ('inventario','verify',     'item'),
    ('inventario','conciliate', ''),
    ('inventario','close',      ''),
    ('patrimonio','read',       ''),
    ('reportes',  'read',       ''),
    ('reportes',  'generate',   ''),
    ('reportes',  'export',     '')
) ON CONFLICT DO NOTHING;

-- MANTENIMIENTO_VIEWER
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'MANTENIMIENTO_VIEWER' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',       'view',  'mantenimientos'),
    ('mantenimiento', 'read',  '')
) ON CONFLICT DO NOTHING;

-- MANTENIMIENTO_GESTOR
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'MANTENIMIENTO_GESTOR' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',       'view',       'mantenimientos'),
    ('mantenimiento', 'create',     ''),
    ('mantenimiento', 'read',       ''),
    ('mantenimiento', 'update',     ''),
    ('mantenimiento', 'close',      ''),
    ('mantenimiento', 'alert',      'configure')
) ON CONFLICT DO NOTHING;

-- REPORTES_VIEWER
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'REPORTES_VIEWER' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',  'view',       'reportes'),
    ('reportes', 'read',       ''),
    ('reportes', 'generate',   ''),
    ('reportes', 'export',     '')
) ON CONFLICT DO NOTHING;

-- REPORTES_SCHEDULER
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'REPORTES_SCHEDULER' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',  'view',       'reportes'),
    ('reportes', 'read',       ''),
    ('reportes', 'generate',   ''),
    ('reportes', 'export',     ''),
    ('reportes', 'schedule',   '')
) ON CONFLICT DO NOTHING;

-- AUDITORIA_VIEWER
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'AUDITORIA_VIEWER' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar',   'view', 'auditoria'),
    ('auditoria', 'read', '')
) ON CONFLICT DO NOTHING;

-- TENANT_CONFIG_MANAGER
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'TENANT_CONFIG_MANAGER' AND (p.module, p.action, COALESCE(p.resource,'')) IN (
    ('sidebar', 'view', 'areas'),
    ('sidebar', 'view', 'cargos'),
    ('sidebar', 'view', 'categorias'),
    ('sidebar', 'view', 'proveedores'),
    ('sidebar', 'view', 'ubicaciones'),
    ('sidebar', 'view', 'personas'),
    ('config',  'read', ''),
    ('config',  'update', ''),
    ('config',  'areas', 'manage'),
    ('config',  'categories', 'manage'),
    ('config',  'locations', 'manage')
) ON CONFLICT DO NOTHING;

-- TENANT_ADMIN (El Tenant Admin tiene todos los permisos operativos locales de tenant)
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'TENANT_ADMIN' AND p.module != 'sidebar' OR (p.module = 'sidebar' AND p.resource != 'admin')
ON CONFLICT DO NOTHING;

-- SUPER_ADMIN (El Super Admin puede ver la administración global)
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'SUPER_ADMIN' AND p.module = 'sidebar' AND p.resource = 'admin'
ON CONFLICT DO NOTHING;
