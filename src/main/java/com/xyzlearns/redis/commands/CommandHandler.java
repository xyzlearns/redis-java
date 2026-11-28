package com.xyzlearns.redis.commands;

import com.xyzlearns.redis.aof.AofManager;
import com.xyzlearns.redis.config.RedisConfig;
import com.xyzlearns.redis.entity.RedisEntry;
import com.xyzlearns.redis.storage.RedisStore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class CommandHandler {
    private final RedisStore store;
    private final RedisConfig redisConfig;

    public CommandHandler(RedisStore redisStore,RedisConfig redisConfig){
        this.store = redisStore;
        this.redisConfig = redisConfig;
    }

    public void execute(List<String> commands, OutputStream out) throws IOException{
        execute(commands,out,true);
    }

    public void execute(List<String> commands, OutputStream out,boolean persist) throws IOException{
        if (commands == null || commands.isEmpty()) {
            return;
        }

        String command = commands.get(0).toLowerCase();

        switch (command) {
            case "ping" -> out.write("+PONG\r\n".getBytes());
            case "echo" -> {

                if (commands.size() != 2) {
                    out.write("-ERR wrong number of arguments\r\n".getBytes());
                    return;
                }

                writeBulkString(commands.get(1), out);
            }
            case "set" -> {

                if (commands.size() != 3 && commands.size() != 5) {
                    out.write("-ERR wrong number of arguments\r\n".getBytes());
                    return;
                }

                String key = commands.get(1);
                String value = commands.get(2);

                long expiryTime = -1;

                if (commands.size() == 5) {

                    String expiryType = commands.get(3).toLowerCase();
                    long expiry = Long.parseLong(commands.get(4));

                    if (expiry <= 0) {
                        out.write("-ERR invalid expire time\r\n".getBytes());
                        return;
                    }

                    if (expiryType.equals("px")) {
                        expiryTime = System.currentTimeMillis() + expiry;

                    } else if (expiryType.equals("ex")) {
                        expiryTime = System.currentTimeMillis() + expiry * 1000;

                    } else {
                        out.write("-ERR syntax error\r\n".getBytes());
                        return;
                    }
                }

                store.set(key, value, expiryTime);
                AofManager manager = new AofManager(redisConfig);

                if (persist) {
                    manager.aofWrite(commands);
                }

                out.write("+OK\r\n".getBytes());
            }
            case "get" -> {

                if (commands.size() != 2) {
                    out.write("-ERR wrong number of arguments\r\n".getBytes());
                    return;
                }

                RedisEntry entry = store.get(commands.get(1));

                if (entry == null) {
                    out.write("$-1\r\n".getBytes());
                } else {
                    writeBulkString(entry.getValue(), out);
                }
            }
            case "config" -> {
                if (commands.size() != 3 ||
                        !commands.get(1).equalsIgnoreCase("get")) {
                    out.write("-ERR wrong number of arguments\r\n".getBytes());
                    return;
                }

                config(commands.get(2), out);

            }
            default -> out.write(("-ERR unknown command '" +
                    commands.get(0) + "'\r\n").getBytes());
        }

        out.flush();
    }

    private void writeBulkString(String value, OutputStream out)
            throws IOException {

        byte[] data = value.getBytes();

        out.write(("$" + data.length + "\r\n").getBytes());
        out.write(data);
        out.write("\r\n".getBytes());
    }

    private void config(String parameter, OutputStream out) throws IOException {

        String value = redisConfig.getConfig(parameter);

        if (value == null) {
            out.write("*0\r\n".getBytes());
            return;
        }

        out.write("*2\r\n".getBytes());
        writeBulkString(parameter, out);
        writeBulkString(value, out);
    }
}

