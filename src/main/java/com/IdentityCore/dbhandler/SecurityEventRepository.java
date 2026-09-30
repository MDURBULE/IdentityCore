package com.IdentityCore.dbhandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

import org.springframework.stereotype.Service;

import com.IdentityCore.config.Config;

@Service
public class SecurityEventRepository {

    public void recordSecurityEvent(Long userId, String eventType, String ipAddress, String userAgent, String clientId,
            String details) {
        String sql = "SELECT sp_record_security_event_v1(?, ?, ?, ?, ?, ?::jsonb)";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            if (userId != null) {
                pt.setLong(1, userId);
            } else {
                pt.setNull(1, Types.BIGINT);
            }
            pt.setString(2, eventType);
            pt.setString(3, ipAddress);
            pt.setString(4, userAgent);
            pt.setString(5, clientId);
            pt.setString(6, details != null ? details : "{}");
            pt.execute();
        } catch (Exception e) {
            Config.getLgr().error("Failed to record security event: {} for userId: {}", eventType, userId, e);
        }
    }
}
