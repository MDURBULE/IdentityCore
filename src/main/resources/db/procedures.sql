-- =============================================================================
-- IdentityCore Stored Procedures & Functions (PostgreSQL PL/pgSQL)
-- =============================================================================

-- 1. Register User SP (V1) with user profile support
CREATE OR REPLACE FUNCTION sp_register_user_v1(
    p_email VARCHAR,
    p_normalized_email VARCHAR,
    p_password_hash VARCHAR,
    p_first_name VARCHAR DEFAULT NULL,
    p_last_name VARCHAR DEFAULT NULL,
    p_phone_number VARCHAR DEFAULT NULL,
    OUT p_user_id BIGINT,
    OUT p_public_id UUID,
    OUT p_status VARCHAR
)
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM users WHERE normalized_email = p_normalized_email) THEN
        RAISE EXCEPTION 'EMAIL_ALREADY_EXISTS' USING ERRCODE = '23505';
    END IF;

    INSERT INTO users (
        email,
        normalized_email,
        first_name,
        last_name,
        phone_number,
        status,
        email_verified
    )
    VALUES (
        p_email,
        p_normalized_email,
        p_first_name,
        p_last_name,
        p_phone_number,
        'PENDING_VERIFICATION',
        FALSE
    )
    RETURNING id, public_id, status INTO p_user_id, p_public_id, p_status;

    INSERT INTO credentials (user_id, credential_type, secret_hash, status)
    VALUES (p_user_id, 'PASSWORD', p_password_hash, 'ACTIVE');
END;
$$;

-- 2. User Last Login Update SP
CREATE OR REPLACE FUNCTION sp_update_user_last_login_v1(p_user_id BIGINT)
RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE users
    SET last_login_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_user_id;
END;
$$;

-- 3. Update User Profile SP
CREATE OR REPLACE FUNCTION sp_update_user_profile_v1(
    p_user_id BIGINT,
    p_first_name VARCHAR,
    p_last_name VARCHAR,
    p_phone_number VARCHAR,
    p_avatar_url VARCHAR
)
RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE users
    SET first_name = COALESCE(p_first_name, first_name),
        last_name = COALESCE(p_last_name, last_name),
        phone_number = COALESCE(p_phone_number, phone_number),
        avatar_url = COALESCE(p_avatar_url, avatar_url),
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_user_id;
END;
$$;

-- 4. Verify User Email SP
CREATE OR REPLACE FUNCTION sp_verify_user_email_v1(p_user_id BIGINT)
RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE users
    SET email_verified = TRUE,
        status = CASE WHEN status = 'PENDING_VERIFICATION' THEN 'ACTIVE' ELSE status END,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_user_id;
END;
$$;

-- 5. Update Password Hash SP
CREATE OR REPLACE FUNCTION sp_update_user_password_v1(
    p_user_id BIGINT,
    p_new_password_hash VARCHAR
)
RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE credentials
    SET secret_hash = p_new_password_hash,
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id AND credential_type = 'PASSWORD';
END;
$$;

-- 6. Record Security Event SP
CREATE OR REPLACE FUNCTION sp_record_security_event_v1(
    p_user_id BIGINT,
    p_event_type VARCHAR,
    p_ip_address VARCHAR,
    p_user_agent TEXT,
    p_client_id VARCHAR,
    p_details JSONB
)
RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO security_events (user_id, event_type, ip_address, user_agent, client_id, details)
    VALUES (p_user_id, p_event_type, p_ip_address, p_user_agent, p_client_id, p_details);
END;
$$;

-- 7. Issue Initial Refresh Token SP
CREATE OR REPLACE FUNCTION sp_issue_refresh_token_v1(
    p_user_id BIGINT,
    p_token_hash VARCHAR,
    p_family_id UUID,
    p_device_name VARCHAR,
    p_ip_address VARCHAR,
    p_user_agent TEXT,
    p_expires_at TIMESTAMPTZ,
    OUT p_token_id BIGINT
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO refresh_tokens (
        user_id, token_hash, family_id, device_name, ip_address, user_agent, expires_at
    )
    VALUES (
        p_user_id, p_token_hash, p_family_id, p_device_name, p_ip_address, p_user_agent, p_expires_at
    )
    RETURNING id INTO p_token_id;
END;
$$;

-- 8. Rotate Refresh Token SP
CREATE OR REPLACE FUNCTION sp_rotate_refresh_token_v1(
    p_old_token_hash VARCHAR,
    p_new_token_hash VARCHAR,
    p_new_expires_at TIMESTAMPTZ,
    p_ip_address VARCHAR,
    p_user_agent TEXT,
    OUT p_success BOOLEAN,
    OUT p_user_id BIGINT,
    OUT p_family_id UUID,
    OUT p_new_token_id BIGINT,
    OUT p_error_code VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_token RECORD;
BEGIN
    SELECT * INTO v_token
    FROM refresh_tokens
    WHERE token_hash = p_old_token_hash;

    IF NOT FOUND THEN
        p_success := FALSE;
        p_error_code := 'TOKEN_NOT_FOUND';
        RETURN;
    END IF;

    -- Token reuse detection: if revoked or already used, compromise detected! Revoke entire family!
    IF v_token.revoked_at IS NOT NULL OR v_token.used_at IS NOT NULL THEN
        UPDATE refresh_tokens
        SET revoked_at = CURRENT_TIMESTAMP
        WHERE family_id = v_token.family_id AND revoked_at IS NULL;

        p_success := FALSE;
        p_error_code := 'TOKEN_REUSE_DETECTED';
        RETURN;
    END IF;

    -- Check expiration
    IF v_token.expires_at < CURRENT_TIMESTAMP THEN
        p_success := FALSE;
        p_error_code := 'TOKEN_EXPIRED';
        RETURN;
    END IF;

    -- Mark current token as used
    UPDATE refresh_tokens
    SET used_at = CURRENT_TIMESTAMP
    WHERE id = v_token.id;

    -- Insert new token in same family
    INSERT INTO refresh_tokens (
        user_id, token_hash, family_id, device_name, ip_address, user_agent, expires_at
    )
    VALUES (
        v_token.user_id, p_new_token_hash, v_token.family_id, v_token.device_name, p_ip_address, p_user_agent, p_new_expires_at
    )
    RETURNING id INTO p_new_token_id;

    p_success := TRUE;
    p_user_id := v_token.user_id;
    p_family_id := v_token.family_id;
    p_error_code := NULL;
END;
$$;

-- 9. Revoke Session SP
CREATE OR REPLACE FUNCTION sp_revoke_session_v1(
    p_session_id BIGINT,
    p_user_id BIGINT
)
RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE refresh_tokens
    SET revoked_at = CURRENT_TIMESTAMP
    WHERE id = p_session_id AND user_id = p_user_id AND revoked_at IS NULL;
END;
$$;

-- 10. Revoke All User Sessions SP
CREATE OR REPLACE FUNCTION sp_revoke_all_user_sessions_v1(
    p_user_id BIGINT,
    OUT p_revoked_count INT
)
LANGUAGE plpgsql AS $$
BEGIN
    WITH updated AS (
        UPDATE refresh_tokens
        SET revoked_at = CURRENT_TIMESTAMP
        WHERE user_id = p_user_id AND revoked_at IS NULL
        RETURNING id
    )
    SELECT COUNT(*)::INT INTO p_revoked_count FROM updated;
END;
$$;
