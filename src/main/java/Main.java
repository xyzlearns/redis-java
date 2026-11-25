import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

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

    private static void handleMultipleClients(Socket clientSocket){
        try(Socket socket = clientSocket) {
            byte[] buffer = new byte[1024];
            int read = socket.getInputStream().read(buffer);
            while (read != -1) {
                socket.getOutputStream().write("+PONG\r\n".getBytes());
                socket.getOutputStream().flush();
                read = socket.getInputStream().read(buffer);
            }
        } catch (IOException e){
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
