package com.IdentityCore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigProperties {
    private final String dbfilepath;
    private final String cachefilepath;
    private final String appName;
    private final String notificationurl;
    private final String jwtIssuer;
    private final String jwtAudience;
    private final long jwtExpirationSeconds;

    public ConfigProperties(
            @Value("${dbfilepath:config/DBConfig.cfg}") String dbfilepath,
            @Value("${cachefilepath:config/cache.cfg}") String cachefilepath,
            @Value("${appname:IdentityCore}") String appName,
            @Value("${notification.service.url:http://localhost:8080/api/v1/notifications}") String notificationurl,
            @Value("${security.jwt.issuer:https://identity.swiunflow.com}") String jwtIssuer,
            @Value("${security.jwt.audience:swiunflow}") String jwtAudience,
            @Value("${security.jwt.expiration-seconds:900}") long jwtExpirationSeconds) {
        this.dbfilepath = dbfilepath;
        this.cachefilepath = cachefilepath;
        this.appName = appName;
        this.notificationurl = notificationurl;
        this.jwtIssuer = jwtIssuer;
        this.jwtAudience = jwtAudience;
        this.jwtExpirationSeconds = jwtExpirationSeconds;
    }

    public String getDbFilePath() {
        return dbfilepath;
    }

    public String getCacheFilePath() {
        return cachefilepath;
    }

    public String getAppName() {
        return appName;
    }

    public String getNotificationurl() {
        return notificationurl;
    }

    public String getJwtIssuer() {
        return jwtIssuer;
    }

    public String getJwtAudience() {
        return jwtAudience;
    }

    public long getJwtExpirationSeconds() {
        return jwtExpirationSeconds;
    }
}

