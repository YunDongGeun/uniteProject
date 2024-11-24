package uniteProject.server.message.login;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LoginServer {
    private ServerSocket listenSocket = null;
    private int port = 5000;

    public void start() {
        try {
            listenSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // 새로운 클라이언트 연결마다 새 쓰레드 생성
                LoginHandler handler = new LoginHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        } finally {
            try {
                if (listenSocket != null) listenSocket.close();
            } catch (IOException e) {
                System.err.println("Server Close Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new LoginServer().start();
    }
}