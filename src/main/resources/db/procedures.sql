-- Register User SP : 

CREATE OR REPLACE FUNCTION sp_register_user_v1(
    p_email VARCHAR,
    p_normalized_email VARCHAR,
    p_password_hash VARCHAR,
    OUT p_user_id BIGINT,
    OUT p_public_id UUID,
    OUT p_status VARCHAR
)
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM users WHERE normalized_email = p_normalized_email) THEN
        RAISE EXCEPTION 'EMAIL_ALREADY_EXISTS' USING ERRCODE = '23505';
    END IF;

    INSERT INTO users (email, normalized_email, status, email_verified)
    VALUES (p_email, p_normalized_email, 'PENDING_VERIFICATION', FALSE)
    RETURNING id, public_id, status INTO p_user_id, p_public_id, p_status;

    INSERT INTO credentials (user_id, credential_type, secret_hash, status)
    VALUES (p_user_id, 'PASSWORD', p_password_hash, 'ACTIVE');

END;
$$;

-- User Last Login Update SP: 

CREATE OR REPLACE FUNCTION sp_update_user_last_login_v1(p_user_id BIGINT)
RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE users
    SET last_login_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
    WHERE id = p_user_id;
END;
$$;
