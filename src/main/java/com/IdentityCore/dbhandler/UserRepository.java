package com.IdentityCore.dbhandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.IdentityCore.config.Config;
import com.IdentityCore.model.constant.UserStatus;
import com.IdentityCore.model.entity.User;

@Service
public class UserRepository {

    public Map<String, Object> registerUser(String rawEmail, String email, String password,
            String firstName, String lastName, String phoneNumber, String ipaddress, String userAgent) {
        Map<String, Object> result = new HashMap<>();
        String procedure = "SELECT * FROM sp_register_user_v1(?, ?, ?, ?, ?, ?)";

        try (Connection con = Config.getDbHandler().getConnection()) {
            PreparedStatement pt = con.prepareStatement(procedure);
            pt.setString(1, rawEmail);
            pt.setString(2, email);
            pt.setString(3, password);
            if (firstName != null) {
                pt.setString(4, firstName);
            } else {
                pt.setNull(4, Types.VARCHAR);
            }
            if (lastName != null) {
                pt.setString(5, lastName);
            } else {
                pt.setNull(5, Types.VARCHAR);
            }
            if (phoneNumber != null) {
                pt.setString(6, phoneNumber);
            } else {
                pt.setNull(6, Types.VARCHAR);
            }

            try (ResultSet rs = pt.executeQuery()) {
                if (rs.next()) {
                    long userId = rs.getLong("p_user_id");
                    UUID publicId = (UUID) rs.getObject("p_public_id");
                    String status = rs.getString("p_status");

                    result.put("success", true);
                    result.put("userId", userId);
                    result.put("p_user_id", userId);
                    result.put("publicId", publicId);
                    result.put("p_public_id", publicId);
                    result.put("status", status);
                    result.put("p_status", status);
                }
            }
            return result;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("EMAIL_ALREADY_EXISTS"))) {
                result.put("success", false);
                result.put("error", "EMAIL_ALREADY_EXISTS");
                return result;
            }
            Config.getLgr().error("User registration failed", e);
            throw new RuntimeException("User registration failed", e);
        }
    }

    public boolean updateLastLogin(Long userId) {
        String sql = "SELECT sp_update_user_last_login_v1(?)";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setLong(1, userId);
            pt.execute();
            return true;
        } catch (Exception e) {
            Config.getLgr().error("Failed to update last login for userId: {}", userId, e);
            return false;
        }
    }

    public boolean updateStatusAndVerification(Long userId, UserStatus status, boolean emailVerified) {
        String sql = "UPDATE users SET status = ?, email_verified = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setString(1, status.name());
            pt.setBoolean(2, emailVerified);
            pt.setLong(3, userId);
            return pt.executeUpdate() > 0;
        } catch (Exception e) {
            Config.getLgr().error("Failed to update status and verification for userId: {}", userId, e);
            return false;
        }
    }

    public boolean verifyEmail(Long userId) {
        String sql = "SELECT sp_verify_user_email_v1(?)";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setLong(1, userId);
            pt.execute();
            return true;
        } catch (Exception e) {
            Config.getLgr().error("Failed to verify email for userId: {}", userId, e);
            return false;
        }
    }

    public boolean updateProfile(Long userId, String firstName, String lastName, String phoneNumber, String avatarUrl) {
        String sql = "SELECT sp_update_user_profile_v1(?, ?, ?, ?, ?)";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setLong(1, userId);
            if (firstName != null) pt.setString(2, firstName); else pt.setNull(2, Types.VARCHAR);
            if (lastName != null) pt.setString(3, lastName); else pt.setNull(3, Types.VARCHAR);
            if (phoneNumber != null) pt.setString(4, phoneNumber); else pt.setNull(4, Types.VARCHAR);
            if (avatarUrl != null) pt.setString(5, avatarUrl); else pt.setNull(5, Types.VARCHAR);
            pt.execute();
            return true;
        } catch (Exception e) {
            Config.getLgr().error("Failed to update profile for userId: {}", userId, e);
            return false;
        }
    }

    public Optional<User> findByNormalizedEmail(String normalizedEmail) {
        String sql = "SELECT * FROM users WHERE normalized_email = ?";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setString(1, normalizedEmail);
            try (ResultSet rs = pt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (Exception e) {
            Config.getLgr().error("Failed to find user by normalized email: {}", normalizedEmail, e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setLong(1, id);
            try (ResultSet rs = pt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (Exception e) {
            Config.getLgr().error("Failed to find user by id: {}", id, e);
        }
        return Optional.empty();
    }

    public Optional<User> findByPublicId(UUID publicId) {
        String sql = "SELECT * FROM users WHERE public_id = ?";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setObject(1, publicId);
            try (ResultSet rs = pt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (Exception e) {
            Config.getLgr().error("Failed to find user by public id: {}", publicId, e);
        }
        return Optional.empty();
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setPublicId((UUID) rs.getObject("public_id"));
        user.setEmail(rs.getString("email"));
        user.setNormalizedEmail(rs.getString("normalized_email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setAvatarUrl(rs.getString("avatar_url"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            user.setStatus(UserStatus.valueOf(statusStr));
        }
        user.setEmailVerified(rs.getBoolean("email_verified"));
        if (rs.getTimestamp("created_at") != null) {
            user.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        }
        if (rs.getTimestamp("updated_at") != null) {
            user.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        }
        if (rs.getTimestamp("last_login_at") != null) {
            user.setLastLoginAt(rs.getTimestamp("last_login_at").toInstant());
        }
        return user;
    }
}
