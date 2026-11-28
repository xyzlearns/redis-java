package com.xyzlearns.redis.aof;

import com.xyzlearns.redis.config.RedisConfig;
import com.xyzlearns.redis.utils.RespSerializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class AofManager {
    private final RedisConfig redisConfig;

    public AofManager(RedisConfig redisConfig){
        this.redisConfig = redisConfig;
    }
    public void aofWrite(List<String> command) throws IOException{
        if (!"yes".equalsIgnoreCase(redisConfig.getConfig("appendonly"))) {
            return;
        }

        RespSerializer respSerializer = new RespSerializer();
        String resp = respSerializer.serialize(command);

        String appenddirname = redisConfig.getConfig("appenddirname");
        String appendfilename = redisConfig.getConfig("appendfilename");
        String currentDir = redisConfig.getConfig("dir");

        Path aofDirectory = Path.of(currentDir).resolve(appenddirname);

        String manifestFilename = appendfilename + ".manifest";
        Path manifestDir = aofDirectory.resolve(manifestFilename);
        String activeAofFile = activeAofFile(manifestDir);

        Path aofFile = aofDirectory.resolve(activeAofFile);
        appendToAof(aofFile, resp);

    }

    public void appendToAof(Path file, String resp) throws IOException {

        try (FileChannel channel = FileChannel.open(
                file,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND
        )) {

            ByteBuffer buffer = StandardCharsets.UTF_8.encode(resp);

            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            channel.force(true);
        }
    }

    public String activeAofFile(Path manifest) throws IOException{

        List<String> lines = Files.readAllLines(manifest);

        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 6 && parts[0].equals("file") && parts[5].equals("i")) {
                return parts[1];
            }
        }

        throw new IOException("No incremental AOF file found");
    }
}
