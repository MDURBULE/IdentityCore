package com.IdentityCore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigProperties {
    private final String dbfilepath;
    private final String cachefilepath;
    private final String appName;
    private final String notificationurl;

    public ConfigProperties(@Value("${dbfilepath}")String dbfilepath,
                    @Value("${cachefilepath}")String cachefilepath,
                    @Value("${appname}")String appName,
                @Value("$notificationurl")String notificationurl){
        this.dbfilepath = dbfilepath;
        this.cachefilepath = cachefilepath;
        this.appName = appName;
        this.notificationurl = notificationurl;
    }

    public String getDbFilePath(){
        return dbfilepath;
    }
    public String getCacheFilePath(){
        return cachefilepath;
    }
    public String getAppName(){
        return appName;
    }
    public String getNotificationurl(){
        return notificationurl;
    }
}
