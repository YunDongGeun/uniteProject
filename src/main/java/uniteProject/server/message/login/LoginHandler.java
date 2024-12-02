package uniteProject.server.message.login;

import uniteProject.server.message.LoginMessage;

import java.io.*;
import java.net.*;

public class LoginHandler extends Thread {
    private final Socket clientSocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private LoginMessage loginData;

    public LoginHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // 스트림 설정
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                // 클라이언트로부터 메시지 타입 받기
                loginData = (LoginMessage) ois.readObject();
                int messageType = loginData.getType();

                switch (messageType) {
                    case LoginMessage.LOGIN_REQUEST:
                        handleLogin();
                        break;
                    case LoginMessage.SIGNUP_REQUEST:
                        handleSignup();
                        break;
                    case LoginMessage.LOGOUT:
                        return; // 쓰레드 종료
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client Handler Error: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void handleLogin() {
        try {
            String id = loginData.getId();
            String pwd = loginData.getPwd();
            System.out.println("Login attempt - ID: " + id);

            // 실제 환경에서는 데이터베이스 조회로 변경해야 함
            if (id.equals("software")) {
                if (pwd.equals("1234")) {
                    sendLoginResult(LoginMessage.SUCCESS, "로그인 성공");
                } else {
                    sendLoginResult(LoginMessage.FAIL_PWD, "비밀번호가 일치하지 않습니다");
                }
            } else {
                sendLoginResult(LoginMessage.FAIL_ID, "존재하지 않는 아이디입니다");
            }
        } catch (IOException e) {
            System.err.println("Login Handler Error: " + e.getMessage());
        }
    }

    private void handleSignup() {
        try {
            String id = loginData.getId();
            String pwd = loginData.getPwd();
            System.out.println("Signup attempt - ID: " + id);

            // 실제 환경에서는 데이터베이스에 저장하는 로직으로 변경해야 함
            // 여기서는 간단히 성공 응답만 보냄
            LoginMessage response = new LoginMessage();
            response.setType(LoginMessage.SIGNUP_RESULT);
            response.setCode(LoginMessage.SUCCESS);
            response.setMessage("회원가입이 완료되었습니다");
            oos.writeObject(response);

        } catch (IOException e) {
            System.err.println("Signup Handler Error: " + e.getMessage());
        }
    }

    private void sendLoginResult(int code, String message) throws IOException {
        LoginMessage response = new LoginMessage();
        response.setType(LoginMessage.LOGIN_RESULT);
        response.setCode(code);
        response.setMessage(message);
        oos.writeObject(response);
    }

    private void closeConnection() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Close Connection Error: " + e.getMessage());
        }
    }
}