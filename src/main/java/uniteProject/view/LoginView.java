package uniteProject.view;

import uniteProject.server.message.LoginMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class LoginView {
    private Socket clientSocket = null;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private LoginMessage loginData;
    Scanner sc = new Scanner(System.in);

    public void printSignIn() {
        try {
            connectToServer();

            System.out.println("=== 로그인 ===");
            System.out.print("아이디: ");
            String id = sc.next();
            System.out.print("비밀번호: ");
            String pwd = sc.next();

            // 로그인 요청 전송
            loginData = new LoginMessage();
            loginData.setType(LoginMessage.LOGIN_REQUEST);
            loginData.setId(id);
            loginData.setPwd(pwd);
            oos.writeObject(loginData);

            // 결과 수신
            loginData = (LoginMessage) ois.readObject();
            System.out.println(loginData.getMessage());

            if (loginData.getCode() == LoginMessage.SUCCESS) {
                // 로그인 성공 처리
                // 여기에 메인 메뉴로 이동하는 로직 추가
            }

        } catch (Exception e) {
            System.out.println("로그인 오류: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    public void printSignUp() {
        try {
            connectToServer();

            System.out.println("=== 회원가입 ===");
            System.out.print("아이디: ");
            String id = sc.next();
            System.out.print("비밀번호: ");
            String pwd = sc.next();

            // 회원가입 요청 전송
            loginData = new LoginMessage();
            loginData.setType(LoginMessage.SIGNUP_REQUEST);
            loginData.setId(id);
            loginData.setPwd(pwd);
            oos.writeObject(loginData);

            // 결과 수신
            loginData = (LoginMessage) ois.readObject();
            System.out.println(loginData.getMessage());

        } catch (Exception e) {
            System.out.println("회원가입 오류: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void connectToServer() throws IOException {
        clientSocket = new Socket("localhost", 5000);
        oos = new ObjectOutputStream(clientSocket.getOutputStream());
        ois = new ObjectInputStream(clientSocket.getInputStream());
    }

    private void closeConnection() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("연결 종료 오류: " + e.getMessage());
        }
    }
}