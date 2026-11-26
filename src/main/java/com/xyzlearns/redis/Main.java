
package com.xyzlearns.redis;

import com.xyzlearns.redis.commands.CommandHandler;
import com.xyzlearns.redis.storage.RedisStore;
import com.xyzlearns.redis.utils.RespParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("Redis Java Server Started!");

        int port = 6379;

        RedisStore store = new RedisStore();
        CommandHandler commandHandler = new CommandHandler(store);

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
}
