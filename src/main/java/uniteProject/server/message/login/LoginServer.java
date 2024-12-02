package uniteProject.server.message.login;

import uniteProject.server.message.Protocol;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class LoginServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private boolean running;

    // 임시 사용자 데이터 (실제로는 DB 사용)
    private Map<String, String> userDatabase;

    public LoginServer() {
        userDatabase = new HashMap<>();
        userDatabase.put("user1", "pass1");
        userDatabase.put("admin", "admin123");
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("서버가 포트 " + PORT + "에서 시작되었습니다.");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 연결됨: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private String currentId = null;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // ID 요청
                Protocol protocol = new Protocol(Protocol.TYPE_AUTH, Protocol.CODE_AUTH_ID_REQ);
                sendProtocol(protocol);

                while (true) {
                    protocol = receiveProtocol();

                    if (protocol.getType() == Protocol.TYPE_AUTH) {
                        if (currentId == null) {
                            // ID 처리
                            String id = new String(protocol.getData()).trim();
                            if (userDatabase.containsKey(id)) {
                                currentId = id;
                                // 비밀번호 요청
                                protocol = new Protocol(Protocol.TYPE_AUTH, Protocol.CODE_AUTH_PW_REQ);
                                sendProtocol(protocol);
                            } else {
                                // ID 실패
                                protocol = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_FAIL);
                                sendProtocol(protocol);
                                break;
                            }
                        } else {
                            // 비밀번호 처리
                            String password = new String(protocol.getData()).trim();
                            if (userDatabase.get(currentId).equals(password)) {
                                // 로그인 성공
                                protocol = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
                                sendProtocol(protocol);
                            } else {
                                // 비밀번호 실패
                                protocol = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_FAIL);
                                sendProtocol(protocol);
                            }
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendProtocol(Protocol protocol) throws IOException {
            out.write(protocol.getType());
            out.write(protocol.getCode());
            out.writeShort(protocol.getLength());
            if (protocol.getData() != null) {
                out.write(protocol.getData());
            }
            out.flush();
        }

        private Protocol receiveProtocol() throws IOException {
            Protocol protocol = new Protocol();

            protocol.setType(in.readByte());
            protocol.setCode(in.readByte());
            protocol.setLength(in.readShort());

            if (protocol.getLength() > 0) {
                byte[] data = new byte[protocol.getLength()];
                in.readFully(data);
                protocol.setData(data);
            }

            return protocol;
        }
    }

    public static void main(String[] args) {
        LoginServer server = new LoginServer();
        server.start();
    }

}