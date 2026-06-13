CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(160) NOT NULL,
    name VARCHAR(160) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(40) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'MANAGER', 'CASHIER'))
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);
