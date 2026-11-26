package com.xyzlearns.redis;

import com.xyzlearns.redis.utils.RespParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Main {
    public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

        ServerSocket serverSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread =new Thread(()->handleMultipleClients(clientSocket));
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }


    private static void handleMultipleClients(Socket clientSocket) {
        try (Socket socket = clientSocket) {

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            RespParser parser = new RespParser();

            List<String> commands;

            while ((commands = parser.respParser(in)) != null) {

                if (commands.isEmpty()) continue;

                String command = commands.get(0).toLowerCase();

                if (command.equals("ping")) {

                    out.write("+PONG\r\n".getBytes());

                } else if (command.equals("echo")) {

                    String msg = commands.get(1);

                    msg = "$" + msg.length() + "\r\n" + msg + "\r\n";

                    out.write(msg.getBytes());

                } else {

                    out.write("-ERR unknown command\r\n".getBytes());

                }

                out.flush();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

}
