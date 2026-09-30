package com.IdentityCore.dbhandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.IdentityCore.config.Config;
import com.IdentityCore.model.constant.CredentialType;
import com.IdentityCore.model.entity.Credential;

@Service
public class CredentialRepository {

    public Optional<Credential> findByUserIdAndType(Long userId, CredentialType type) {
        String sql = "SELECT * FROM credentials WHERE user_id = ? AND credential_type = ?";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setLong(1, userId);
            pt.setString(2, type.name());
            try (ResultSet rs = pt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCredential(rs));
                }
            }
        } catch (Exception e) {
            Config.getLgr().error("Failed to find credential for userId: {} and type: {}", userId, type, e);
        }
        return Optional.empty();
    }

    public boolean updatePasswordHash(Long userId, String newHash) {
        String sql = "SELECT sp_update_user_password_v1(?, ?)";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setLong(1, userId);
            pt.setString(2, newHash);
            pt.execute();
            return true;
        } catch (Exception e) {
            Config.getLgr().error("Failed to update password hash for userId: {}", userId, e);
            return false;
        }
    }

    private Credential mapRowToCredential(ResultSet rs) throws SQLException {
        Credential cred = new Credential();
        cred.setId(rs.getLong("id"));
        cred.setUserId(rs.getLong("user_id"));
        String typeStr = rs.getString("credential_type");
        if (typeStr != null) {
            cred.setCredentialType(CredentialType.valueOf(typeStr));
        }
        cred.setSecretHash(rs.getString("secret_hash"));
        cred.setProviderReference(rs.getString("provider_reference"));
        cred.setStatus(rs.getString("status"));
        if (rs.getTimestamp("created_at") != null) {
            cred.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        }
        if (rs.getTimestamp("updated_at") != null) {
            cred.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        }
        return cred;
    }
}
