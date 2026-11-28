
package com.xyzlearns.redis;

import com.xyzlearns.redis.aof.AofManager;
import com.xyzlearns.redis.commands.CommandHandler;
import com.xyzlearns.redis.config.RedisConfig;
import com.xyzlearns.redis.storage.RedisStore;
import com.xyzlearns.redis.utils.RespParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("Redis Java Server Started!");

        int port = 6379;

        RedisStore store = new RedisStore();
        RedisConfig redisConfig = new RedisConfig();
        CommandHandler commandHandler = new CommandHandler(store,redisConfig);

        loadArgs(args,redisConfig);

        try {
            AofManager aofManager = new AofManager(redisConfig);
            aofManager.executeCommands(commandHandler);
        } catch (IOException e) {
            System.out.println("AOF Recovery Error: " + e.getMessage());
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket()) {

            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));

            while (true) {
                Socket clientSocket = serverSocket.accept();

                Thread thread = new Thread(
                        () -> handleMultipleClients(clientSocket, commandHandler)
                );

                thread.start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleMultipleClients(
            Socket clientSocket,
            CommandHandler commandHandler) {

        try (Socket socket = clientSocket) {

            InputStream in = socket.getInputStream();
            RespParser parser = new RespParser();

            List<String> commands;

            while ((commands = parser.respParser(in)) != null) {
                commandHandler.execute(commands, socket.getOutputStream());
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void loadArgs(String[] args,RedisConfig redisConfig){
        for (int i=0;i< args.length-1;i+=2){
            if (!args[i].startsWith("--")) {
                throw new IllegalArgumentException("Invalid flag: " + args[i]);
            }

            String key = args[i];
            key = key.substring(2);

            if (!redisConfig.hasConfig(key)) {
                throw new IllegalArgumentException("Unknown configuration: " + key);
            }

            String value = args[i+1];
            redisConfig.setConfig(key,value);
        }
        redisConfig.reload();
    }
}
