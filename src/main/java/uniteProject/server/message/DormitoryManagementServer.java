package uniteProject.server.message;

import uniteProject.global.jdbc.ConfigReader;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Properties;

public class DormitoryManagementServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private boolean running = false;
    ConfigReader configReader = new ConfigReader();


    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Server started on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create a new thread for each client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Process client request
                    processRequest(inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }

        private void processRequest(String request) {
            try {
                // Parse the request (you might want to use JSON or a custom protocol)
                String[] parts = request.split("\\|");
                String command = parts[0];

                switch (command) {
                    case "LOGIN":
                        handleLogin(parts[1], parts[2]);
                        break;
                    case "APPLY":
                        handleApplication(parts);
                        break;
                    // Add more commands as needed
                    default:
                        out.println("Unknown command");
                }
            } catch (Exception e) {
                out.println("Error: " + e.getMessage());
            }
        }

        private void handleLogin(String username, String password) {
            try (Connection conn = DriverManager.getConnection(
                    configReader.getProperty("DB_CONNECTION_URL"),
                    configReader.getProperty("DB_USER"),
                    configReader.getProperty("DB_PASSWORD")))
            {
                String sql = "SELECT * FROM members WHERE username = ? AND password = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);

                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        out.println("LOGIN_SUCCESS|" + rs.getInt("id") + "|" + rs.getString("role"));
                    } else {
                        out.println("LOGIN_FAILED");
                    }
                }
            } catch (SQLException e) {
                out.println("ERROR|" + e.getMessage());
            }
        }

        private void handleApplication(String[] parts) {
            // Implementation for handling dormitory applications
            // This would involve inserting into the application table
            // and handling related business logic
        }

        private void cleanup() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}