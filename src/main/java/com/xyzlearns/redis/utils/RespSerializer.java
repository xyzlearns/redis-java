package com.xyzlearns.redis.utils;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class RespSerializer {
    public String serialize(List<String> command) {
        StringBuilder resp = new StringBuilder();

        resp.append("*").append(command.size()).append("\r\n");

        for (String arg : command) {
            resp.append("$")
                    .append(arg.getBytes(StandardCharsets.UTF_8).length)
                    .append("\r\n");

            resp.append(arg).append("\r\n");
        }

        return resp.toString();
    }
}
