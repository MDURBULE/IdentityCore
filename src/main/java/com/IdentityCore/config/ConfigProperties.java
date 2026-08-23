package com.IdentityCore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigProperties {
    private final String dbfilepath;
    private final String cachefilepath;
    private final String appName;

    public ConfigProperties(@Value("${dbfilepath}")String dbfilepath,
                    @Value("${cachefilepath}")String cachefilepath,
                    @Value("${appname}")String appName){
        this.dbfilepath = dbfilepath;
        this.cachefilepath = cachefilepath;
        this.appName = appName;
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
}
