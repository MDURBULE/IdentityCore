-- User Table : 

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    normalized_email VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMPTZ,
    CONSTRAINT chk_user_status CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'LOCKED', 'DISABLED', 'DELETED'))
);

CREATE INDEX IF NOT EXISTS idx_users_normalized_email ON users(normalized_email);
CREATE INDEX IF NOT EXISTS idx_users_public_id ON users(public_id);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- Credentials Table : 

CREATE TABLE IF NOT EXISTS credentials (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credential_type VARCHAR(32) NOT NULL,
    secret_hash VARCHAR(512),
    provider_reference VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_credential_type UNIQUE (user_id, credential_type),
    CONSTRAINT chk_credential_type CHECK (credential_type IN ('PASSWORD', 'GOOGLE', 'GITHUB', 'APPLE', 'OIDC', 'PASSKEY', 'WEBAUTHN'))
);

CREATE INDEX IF NOT EXISTS idx_credentials_user_id ON credentials(user_id);
