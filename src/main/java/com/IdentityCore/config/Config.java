package com.IdentityCore.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.master.DBHandler.Chache.CacheHandler;
import com.master.DBHandler.SQL.DBHandler;

import redis.clients.jedis.Jedis;

public final class Config {
    private static Logger lgr;
    private static ConfigProperties cpx;
    private static DBHandler dbHandler;
    private static CacheHandler cacheHandler;

    private Config(){
        
    }

    public static final void initializeConfig(ConfigProperties cp){
        cpx = cp;
        lgr = LogManager.getLogger(cp.getAppName());
        initializeDB(cp.getDbFilePath());
        initializeCache(cp.getCacheFilePath());
    }

    private static void initializeDB(String path){
        try {
            dbHandler = new DBHandler(path);
            if(dbHandler!=null&&dbHandler.isHealty()){
                lgr.info("Database initialize successfully ");
            }else{
                lgr.info("Database initialization Failed");
            }
        } catch (Exception e) {
            lgr.info("Database initialization Failed");
        }
    }
    private static void initializeCache(String path){
        try {
            cacheHandler = new CacheHandler(path);
            if(cacheHandler!=null&&cacheHandler.isHealty()){
                lgr.info("cache initialization successfully ");
            }else{
                lgr.info("cache initialization Failed");
            }
        } catch (Exception e) {
            lgr.info("cache initialization Failed");
        }
    }

    public static Logger getLgr(){
        return lgr;
    }
    public static DBHandler getDbHandler(){
        return dbHandler;
    }
    public static ConfigProperties getCpx(){
        return cpx;
    }

    public static String getKeyFromRedis(String key,boolean del){
        String data=null;
        try(Jedis chache = cacheHandler.getResource()){
            if(chache.exists(key)){
                data = chache.get(key);
                if(del){
                    chache.del(key);
                }
            }
        }catch (Exception e) {
            return null;
        }
        return data;
    }

    public static boolean setRedisKey(String key,String data){
        return setRedisKeyWithTtl(key, data, 60 * 60);
    }

    public static boolean setRedisKeyWithTtl(String key, String data, long seconds) {
        boolean flag = false;
        try (Jedis chache = cacheHandler.getResource()) {
            chache.setex(key, seconds, data);
            flag = true;
        } catch (Exception e) {
            return false;
        }
        return flag;
    }

    public static boolean deleteRedisKey(String key) {
        try (Jedis chache = cacheHandler.getResource()) {
            return chache.del(key) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isRedisKeyPresent(String key){
        try(Jedis chache=cacheHandler.getResource()) {
            if(chache.exists(key)){
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
