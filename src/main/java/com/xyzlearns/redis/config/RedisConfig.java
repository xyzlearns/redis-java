package com.xyzlearns.redis.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

    public void reload() {
        try {
            if ("yes".equalsIgnoreCase(config.get("appendonly"))) {

                String currentDirectory = config.get("dir");
                String appenddirname = config.get("appenddirname");
                String appendfilename = config.get("appendfilename");

                Path dir = Path.of(currentDirectory).resolve(appenddirname);
                Files.createDirectories(dir);

                Path aofFile = dir.resolve(appendfilename + ".1.incr.aof");
                Path aofManifestFile = dir.resolve(appendfilename+".manifest");

                if (Files.notExists(aofFile)) {
                    Files.createFile(aofFile);
                }

                if (Files.notExists(aofManifestFile)) {
                    Files.createFile(aofManifestFile);
                    Files.writeString(aofManifestFile,"file"+appendfilename+".1.incr.aof"+" seq 1 type i\n", StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
