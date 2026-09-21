package com.IdentityCore.utils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.IdentityCore.config.Config;

@Component
public class Notification {

    private final RestTemplate restTemplate;

    public Notification() {
        this.restTemplate = new RestTemplate();
    }

    public void sendEmailVerification(String recipientEmail, String userName, String verificationUrl,
            int expiryMinutes) {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", userName);
        data.put("verificationUrl", verificationUrl);
        data.put("expiryMinutes", expiryMinutes);

        dispatchNotification("EMAIL_VERIFICATION", recipientEmail, data);
    }

    public void sendOtp(String recipientEmail, String userName, String otp, int expiryMinutes) {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", userName);
        data.put("otp", otp);
        data.put("expiryMinutes", expiryMinutes);

        dispatchNotification("SEND_OTP", recipientEmail, data);
    }

    public void sendPasswordReset(String recipientEmail, String userName, String resetUrl, int expiryMinutes) {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", userName);
        data.put("resetUrl", resetUrl);
        data.put("expiryMinutes", expiryMinutes);

        dispatchNotification("PASSWORD_RESET", recipientEmail, data);
    }

    public void sendPasswordChanged(String recipientEmail, String userName, Instant changedAt) {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", userName);
        data.put("changedAt", changedAt.toString());

        dispatchNotification("PASSWORD_CHANGED", recipientEmail, data);
    }

    public void sendWelcomeEmail(String recipientEmail, String userName, String applicationName) {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", userName);
        data.put("applicationName", applicationName);

        dispatchNotification("WELCOME_EMAIL", recipientEmail, data);
    }

    public void sendSecurityAlert(String recipientEmail, String userName, String alertType, String ipAddress,
            String deviceInfo, Instant timestamp) {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", userName);
        data.put("alertType", alertType);
        data.put("ipAddress", ipAddress);
        data.put("deviceInfo", deviceInfo);
        data.put("timestamp", timestamp.toString());

        dispatchNotification("SECURITY_ALERT", recipientEmail, data);
    }

    private void dispatchNotification(String event, String recipientEmail, Map<String, Object> data) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("event", event);
            payload.put("recipient", Map.of("email", recipientEmail));
            payload.put("data", data);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(Config.getCpx().getNotificationurl(), request, Map.class);
            Config.getLgr().info("Successfully dispatched notification event: {} to recipient: {}", event,
                    recipientEmail);
        } catch (Exception e) {
            Config.getLgr().error("Failed to dispatch notification event: {} to recipient: {}", event, recipientEmail,
                    e);
        }
    }
}
