package com.util.redis;

public class RedisConfig {
    public static final int CURRENT_DB_INDEX = 0;
    public static final String WALLCATEGORY_KEY = "wallcategory";
    public static final String PROFILECATEGROY_KEY = "profilecategroy";
    public static final String WALLPAPER_KEY_PREFIX = "wallpaper";
    public static final String PROFILEPHOTO_KEY_PREFIX = "profilephoto";
    //所有key的过期时间
    public static Long EXPIRE_TIME = 24l;

}
