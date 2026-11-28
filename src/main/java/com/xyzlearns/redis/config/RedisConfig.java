package com.xyzlearns.redis.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RedisConfig {
    private final Map<String,String> config = new HashMap<>();

    public RedisConfig(){
        String currentDirectory = System.getProperty("user.dir");
        config.put("dir",currentDirectory);
        config.put("appendonly","no");
        config.put("appenddirname","appendonlydir");
        config.put("appendfilename","appendonly.aof");
        config.put("appendfsync","everysec");
    }

    public String getConfig(String key) {
        return config.get(key);
    }

    public void setConfig(String key,String value) {
        config.put(key,value);
    }

    public boolean hasConfig(String key) {
        return config.containsKey(key);
    }

    public void reload() throws IOException {
        if (config.get("appendonly").equalsIgnoreCase("yes")){
            String currentDirectory = config.get("dir");
            String appendirname = config.get("appendirname");
            Path dir = Path.of(currentDirectory).resolve(appendirname);
            Files.createDirectories(dir);
        }
    }
}
