package com.xyzlearns.redis.entity;

public class RedisEntry {

    private final String value;
    private final long expiryTime;

    public RedisEntry(String value, long expiryTime) {
        this.value = value;
        this.expiryTime = expiryTime;
    }

    public String getValue() {
        return value;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        return expiryTime != -1 && System.currentTimeMillis() >= expiryTime;
    }
}
