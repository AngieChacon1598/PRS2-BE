-- =====================================================
-- DATOS INICIALES PARA EL SISTEMA DE AUTENTICACIÓN
-- =====================================================
-- NOTA: municipal_code, area_id, position_id y document_type_id 
-- son datos simulados que vendrán de otros microservicios

-- =====================================================
-- PERSONS (Personas)
-- =====================================================
INSERT INTO persons (id, document_type_id, document_number, person_type, first_name, last_name, birth_date, gender, personal_phone, work_phone, personal_email, address, municipal_code, status, created_at, updated_at) VALUES
('40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', 1, '76451238', 'N', 'Daniel', 'Gonzales', '1985-03-15', 'M', '987654321', '014567890', 'dgonzales@example.com', 'Av. Principal 123, Lima', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW(), NOW()),
('77983d85-9aa9-48b2-ac62-fdabb4e8c155', 1, '48231567', 'N', 'María', 'Quispe', '1990-07-22', 'F', '987654322', '014567891', 'mquispe@example.com', 'Jr. Los Olivos 456, Lima', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW(), NOW()),
('cc891043-8cb7-42ec-b7e9-2c4cf28fa70a', 1, '75643218', 'N', 'Carlos', 'Rojas', '1992-11-10', 'M', '987654323', '014567892', 'crojas@example.com', 'Av. Los Pinos 789, Lima', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW(), NOW()),
('ebf7496e-8521-41dc-aad2-2f4cdbead4f9', 1, '47583920', 'N', 'Lucía', 'Chávez', '1988-05-18', 'F', '987654324', '014567893', 'lchavez@example.com', 'Calle Las Flores 321, Lima', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW(), NOW()),
('2489176a-4078-4674-861e-48265c92f1f3', 1, '40123589', 'N', 'Jorge', 'Ramírez', '1987-09-25', 'M', '987654325', '014567894', 'jramirez@example.com', 'Av. Industrial 654, Lima', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW(), NOW()),
('41ce7552-34b3-4d6c-8b64-44bbafd82fd6', 1, '42319876', 'N', 'Elena', 'Flores', '1991-12-08', 'F', '987654326', '014567895', 'eflores@example.com', 'Jr. Comercio 987, Lima', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW(), NOW());

-- =====================================================
-- USERS (Usuarios)
-- =====================================================
-- Super Admin
INSERT INTO users (id, username, password_hash, person_id, area_id, position_id, direct_manager_id, municipal_code, status, last_login, login_attempts, blocked_until, preferences, created_by, created_at, updated_by, updated_at, version) VALUES
('40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', 'dgonzales', crypt('SuperAdmin', gen_salt('bf')), '40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACTIVE', NOW(), 0, NULL, '{}', NULL, NOW(), NULL, NOW(), 1);

-- Responsable de Patrimonio
INSERT INTO users (id, username, password_hash, person_id, area_id, position_id, direct_manager_id, municipal_code, status, last_login, login_attempts, blocked_until, preferences, created_by, created_at, updated_by, updated_at, version) VALUES
('77983d85-9aa9-48b2-ac62-fdabb4e8c155', 'mquispe', crypt('SuperAdmin', gen_salt('bf')), '77983d85-9aa9-48b2-ac62-fdabb4e8c155', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACTIVE', NOW(), 0, NULL, '{}', '40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', NOW(), NULL, NOW(), 1);

-- Técnico de Patrimonio
INSERT INTO users (id, username, password_hash, person_id, area_id, position_id, direct_manager_id, municipal_code, status, last_login, login_attempts, blocked_until, preferences, created_by, created_at, updated_by, updated_at, version) VALUES
('cc891043-8cb7-42ec-b7e9-2c4cf28fa70a', 'crojas', crypt('SuperAdmin', gen_salt('bf')), 'cc891043-8cb7-42ec-b7e9-2c4cf28fa70a', '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', '77983d85-9aa9-48b2-ac62-fdabb4e8c155', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACTIVE', NOW(), 0, NULL, '{}', '40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', NOW(), NULL, NOW(), 1);

-- Responsable de Área
INSERT INTO users (id, username, password_hash, person_id, area_id, position_id, direct_manager_id, municipal_code, status, last_login, login_attempts, blocked_until, preferences, created_by, created_at, updated_by, updated_at, version) VALUES
('ebf7496e-8521-41dc-aad2-2f4cdbead4f9', 'lchavez', crypt('SuperAdmin', gen_salt('bf')), 'ebf7496e-8521-41dc-aad2-2f4cdbead4f9', '44444444-4444-4444-4444-444444444444', '55555555-5555-5555-5555-555555555555', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACTIVE', NOW(), 0, NULL, '{}', '40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', NOW(), NULL, NOW(), 1);

-- Jefe de Logística
INSERT INTO users (id, username, password_hash, person_id, area_id, position_id, direct_manager_id, municipal_code, status, last_login, login_attempts, blocked_until, preferences, created_by, created_at, updated_by, updated_at, version) VALUES
('2489176a-4078-4674-861e-48265c92f1f3', 'jramirez', crypt('SuperAdmin', gen_salt('bf')), '2489176a-4078-4674-861e-48265c92f1f3', '66666666-6666-6666-6666-666666666666', '77777777-7777-7777-7777-777777777777', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACTIVE', NOW(), 0, NULL, '{}', '40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', NOW(), NULL, NOW(), 1);

-- Auditor Interno
INSERT INTO users (id, username, password_hash, person_id, area_id, position_id, direct_manager_id, municipal_code, status, last_login, login_attempts, blocked_until, preferences, created_by, created_at, updated_by, updated_at, version) VALUES
('41ce7552-34b3-4d6c-8b64-44bbafd82fd6', 'eflores', crypt('SuperAdmin', gen_salt('bf')), '41ce7552-34b3-4d6c-8b64-44bbafd82fd6', '88888888-8888-8888-8888-888888888888', '99999999-9999-9999-9999-999999999999', '2489176a-4078-4674-861e-48265c92f1f3', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACTIVE', NOW(), 0, NULL, '{}', '40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', NOW(), NULL, NOW(), 1);

-- =====================================================
-- ROLES
-- =====================================================
INSERT INTO roles (id, name, description, is_system, active, created_by, municipal_code, created_at) VALUES
('f089bbfb-7d15-4fe8-8ff3-d4d6c4ad1787', 'SUPER_ADMIN', 'Acceso total a todas las funcionalidades y configuraciones del sistema.', true, true, NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW()),
('197fd233-75c2-48d3-a439-2b2bbe6665d5', 'ADMIN', 'Gestiona usuarios, roles y personas dentro del sistema. Acceso completo a funcionalidades administrativas.', true, true, NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW()),
('1bbe7351-c6d1-4a84-af5e-f588649356c6', 'USER_MANAGER', 'Puede crear, editar y gestionar usuarios sin acceso a configuraciones globales.', true, true, NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW()),
('687d9f86-0e28-40ab-841e-ce538500b529', 'VIEWER', 'Posee acceso solo de lectura a los módulos y datos del sistema.', true, true, NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW()),
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'LOGISTICA_VIEW', 'Acceso solo de lectura para módulos de logística y gestión de inventarios.', true, true, NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW());

-- =====================================================
-- PERMISSIONS
-- =====================================================
INSERT INTO permissions (id, module, action, resource, description, created_by, municipal_code, status, created_at) VALUES
-- Módulo: USERS
(gen_random_uuid(), 'users', 'read', '*', 'Permite visualizar la lista y detalles de usuarios registrados.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'users', 'create', '*', 'Permite crear nuevos usuarios en el sistema.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'users', 'update', '*', 'Permite editar usuarios existentes en el sistema.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'users', 'activate', '*', 'Permite activar usuarios desactivados.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'users', 'deactivate', '*', 'Permite desactivar usuarios activos.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'users', 'block', '*', 'Permite bloquear usuarios.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'users', 'unblock', '*', 'Permite desbloquear usuarios bloqueados.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'users', 'suspend', '*', 'Permite suspender usuarios.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'users', 'unsuspend', '*', 'Permite reactivar usuarios suspendidos.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),

-- Módulo: PERSONS
(gen_random_uuid(), 'persons', 'read', '*', 'Permite consultar datos personales de las personas registradas.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'persons', 'write', '*', 'Permite registrar o actualizar información de personas.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'persons', 'delete', '*', 'Permite eliminar o desactivar registros de personas.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'persons', 'manage', '*', 'Permite gestionar tipos de documentos y relaciones personales.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),

-- Módulo: ROLES
(gen_random_uuid(), 'roles', 'read', '*', 'Permite visualizar los roles existentes en el sistema.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'roles', 'write', '*', 'Permite crear o modificar roles.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'roles', 'delete', '*', 'Permite eliminar roles definidos por el usuario.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'roles', 'manage', '*', 'Permite asignar permisos a los roles.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),

-- Módulo: PERMISSIONS
(gen_random_uuid(), 'permissions', 'read', '*', 'Permite ver los permisos definidos en el sistema.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'permissions', 'write', '*', 'Permite crear o modificar permisos específicos.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'permissions', 'delete', '*', 'Permite eliminar permisos existentes.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'permissions', 'manage', '*', 'Permite asignar permisos a los roles o usuarios.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),

-- Módulo: ASSIGNMENTS
(gen_random_uuid(), 'assignments', 'read', '*', 'Permite ver asignaciones de bienes o responsabilidades.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'assignments', 'write', '*', 'Permite registrar nuevas asignaciones de bienes.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'assignments', 'delete', '*', 'Permite eliminar asignaciones de bienes.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW()),
(gen_random_uuid(), 'assignments', 'manage', '*', 'Permite aprobar, transferir o auditar asignaciones.', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, NOW());

-- =====================================================
-- USERS_ROLES (Asignación de roles a usuarios)
-- =====================================================
INSERT INTO users_roles (user_id, role_id, assigned_by, municipal_code, assigned_at, active) VALUES
-- SUPER_ADMIN
('40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', 'f089bbfb-7d15-4fe8-8ff3-d4d6c4ad1787', '40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW(), true),

-- ADMIN
('77983d85-9aa9-48b2-ac62-fdabb4e8c155', '197fd233-75c2-48d3-a439-2b2bbe6665d5', '40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW(), true),

-- USER_MANAGER
('cc891043-8cb7-42ec-b7e9-2c4cf28fa70a', '1bbe7351-c6d1-4a84-af5e-f588649356c6', '40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW(), true),
('ebf7496e-8521-41dc-aad2-2f4cdbead4f9', '1bbe7351-c6d1-4a84-af5e-f588649356c6', '40a5c2fa-ab64-4a6e-8335-2a136ebeed1a', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW(), true),

-- VIEWER
('2489176a-4078-4674-861e-48265c92f1f3', '687d9f86-0e28-40ab-841e-ce538500b529', '77983d85-9aa9-48b2-ac62-fdabb4e8c155', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW(), true),
('41ce7552-34b3-4d6c-8b64-44bbafd82fd6', '687d9f86-0e28-40ab-841e-ce538500b529', '77983d85-9aa9-48b2-ac62-fdabb4e8c155', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW(), true);

-- =====================================================
-- ROLES_PERMISSIONS (Asignación de permisos a roles)
-- =====================================================
-- SUPER_ADMIN: Acceso total
INSERT INTO roles_permissions (role_id, permission_id, municipal_code, created_at)
SELECT 'f089bbfb-7d15-4fe8-8ff3-d4d6c4ad1787', id, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW() FROM permissions;

-- ADMIN: Gestión de users, persons, roles y permissions
INSERT INTO roles_permissions (role_id, permission_id, municipal_code, created_at)
SELECT '197fd233-75c2-48d3-a439-2b2bbe6665d5', id, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW()
FROM permissions 
WHERE module IN ('users', 'persons', 'roles', 'permissions');

-- USER_MANAGER: Solo permisos del módulo users
INSERT INTO roles_permissions (role_id, permission_id, municipal_code, created_at)
SELECT '1bbe7351-c6d1-4a84-af5e-f588649356c6', id, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW()
FROM permissions 
WHERE module = 'users';

-- VIEWER: Solo permisos de lectura en todos los módulos
INSERT INTO roles_permissions (role_id, permission_id, municipal_code, created_at)
SELECT '687d9f86-0e28-40ab-841e-ce538500b529', id, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW()
FROM permissions
WHERE action = 'read';

-- LOGISTICA_VIEW: Solo permisos de lectura para módulos de logística
-- Nota: Este rol tendrá permisos de lectura solo para módulos relacionados con logística
-- Los permisos específicos deben agregarse cuando se definan los módulos de logística
INSERT INTO roles_permissions (role_id, permission_id, municipal_code, created_at)
SELECT 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', id, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW()
FROM permissions
WHERE action = 'read' AND (module LIKE '%logistica%' OR module LIKE '%logistics%' OR module LIKE '%inventario%' OR module LIKE '%inventory%');
