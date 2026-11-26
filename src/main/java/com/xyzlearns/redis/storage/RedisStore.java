package com.xyzlearns.redis.storage;

import com.xyzlearns.redis.entity.RedisEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisStore {
    private final Map<String, RedisEntry> map = new ConcurrentHashMap<>();

    public void set(String key,String value ,long expiryTime){
        RedisEntry redisEntry = new RedisEntry(value,expiryTime);
        map.put(key,redisEntry);
    }

    public RedisEntry get(String key){
        RedisEntry entry = map.get(key);

        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            map.remove(key, entry);
            return null;
        }

        return entry;
    }
}
