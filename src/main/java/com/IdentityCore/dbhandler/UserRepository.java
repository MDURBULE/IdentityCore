package com.IdentityCore.dbhandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.IdentityCore.config.Config;
import com.IdentityCore.model.constant.UserStatus;
import com.IdentityCore.model.entity.User;

public class UserRepository {

    public Map<String, Object> registerUser(String rawEmail, String email, String password, String ipaddress,
            String userAgent) {
        Map<String, Object> result = new HashMap<>();
        String procedure = "SELECT * FROM sp_register_user_v1(?, ?, ?)";

        try (Connection con = Config.getDbHandler().getConnection()) {
            PreparedStatement pt = con.prepareStatement(procedure);
            pt.setString(1, rawEmail);
            pt.setString(2, email);
            pt.setString(3, password);
            try (ResultSet rs = pt.executeQuery()) {

                if (rs.next()) {
                    result.put("userId", rs.getLong("p_user_id"));
                    result.put("publicId", rs.getObject("p_public_id"));
                    result.put("status", rs.getString("p_status"));
                }
            }
            return result;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                result.put("success", false);
                result.put("error", "EMAIL_ALREADY_EXISTS");
                return result;
            }
            throw new RuntimeException("User registration failed", e);
        }

    }

    public boolean updateLastLogin(Long userId) {
        try (Connection con = Config.getDbHandler().getConnection()) {

        } catch (Exception e) {
        }
        return false;
    }

    public boolean updateStatusAndVerification(Long userId, UserStatus status, boolean emailVerified) {
        try (Connection con = Config.getDbHandler().getConnection()) {

        } catch (Exception e) {

        }
        return false;
    }

    public Optional<User> findByNormalizedEmail(String normalizedEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByNormalizedEmail'");
    }
}
