-- ===============================================
-- USERS
-- ===============================================
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username            VARCHAR(50) UNIQUE NOT NULL,
    password_hash       VARCHAR(500) NOT NULL,
    person_id           UUID,
    area_id             UUID,  -- Opcional: referencia a microservicio externo
    position_id         UUID,  -- Opcional: referencia a microservicio externo
    direct_manager_id   UUID,
    municipal_code      UUID,
    status              VARCHAR(20) DEFAULT 'ACTIVE',
    last_login          TIMESTAMP,
    login_attempts      INTEGER DEFAULT 0,
    blocked_until       TIMESTAMP,
    block_reason        TEXT,
    block_start         TIMESTAMP,
    suspension_reason   TEXT,
    suspension_start    TIMESTAMP,
    suspension_end      TIMESTAMP,
    suspended_by        UUID,
    preferences         TEXT DEFAULT '{}',
    created_by          UUID,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_by          UUID,
    updated_at          TIMESTAMP DEFAULT NOW(),
    version             INTEGER DEFAULT 1,
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- Relaciones principales de USERS
ALTER TABLE users ADD CONSTRAINT fk_users_person 
    FOREIGN KEY (person_id) REFERENCES persons(id);

ALTER TABLE users ADD CONSTRAINT fk_users_manager 
    FOREIGN KEY (direct_manager_id) REFERENCES users(id);

ALTER TABLE users ADD CONSTRAINT fk_users_created_by
    FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE users ADD CONSTRAINT fk_users_updated_by
    FOREIGN KEY (updated_by) REFERENCES users(id);

-- ===============================================
-- PERSONS
-- ===============================================
CREATE TABLE persons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_type_id INTEGER NOT NULL,  -- viene de otro microservicio
    document_number VARCHAR(20) NOT NULL,
    person_type CHAR(1) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    gender CHAR(1),
    personal_phone VARCHAR(20),
    work_phone VARCHAR(20),
    personal_email VARCHAR(200),
    address TEXT,
    created_by UUID,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_by UUID,
    updated_at TIMESTAMP DEFAULT NOW(),
    status BOOLEAN DEFAULT true NOT NULL,
    municipal_code UUID,
    CONSTRAINT chk_person_type CHECK (person_type IN ('N', 'J')),
    CONSTRAINT chk_gender CHECK (gender IN ('M', 'F')),
    CONSTRAINT uk_person_document UNIQUE (document_type_id, document_number)
);

-- Relaciones de auditoría para PERSONS
ALTER TABLE persons ADD CONSTRAINT fk_persons_created_by
    FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE persons ADD CONSTRAINT fk_persons_updated_by
    FOREIGN KEY (updated_by) REFERENCES users(id);

-- NOTA: Ya no se crea ni se referencia la tabla document_types aquí.

-- ===============================================
-- ROLES
-- ===============================================
CREATE TABLE roles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(50) UNIQUE NOT NULL,
    description     TEXT,
    is_system       BOOLEAN DEFAULT false,
    active          BOOLEAN DEFAULT true,
    created_at      TIMESTAMP DEFAULT NOW(),
    created_by      UUID,
    municipal_code  UUID,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ===============================================
-- PERMISSIONS
-- ===============================================
CREATE TABLE permissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module          VARCHAR(50) NOT NULL,
    action          VARCHAR(50) NOT NULL,
    resource        VARCHAR(100),
    display_name    VARCHAR(100),
    description     TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    created_by      UUID,
    status          BOOLEAN DEFAULT TRUE,
    municipal_code  UUID,
    CONSTRAINT uk_permission UNIQUE (module, action, resource),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ===============================================
-- ROLES_PERMISSIONS
-- ===============================================
CREATE TABLE roles_permissions (
    role_id         UUID NOT NULL,
    permission_id   UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW(),
    municipal_code  UUID,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- ===============================================
-- USERS_ROLES
-- ===============================================
CREATE TABLE users_roles (
    user_id         UUID NOT NULL,
    role_id         UUID NOT NULL,
    assigned_by     UUID NOT NULL,
    assigned_at     TIMESTAMP DEFAULT NOW(),
    expiration_date DATE,
    active          BOOLEAN DEFAULT true,
    municipal_code  UUID,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id)
);
