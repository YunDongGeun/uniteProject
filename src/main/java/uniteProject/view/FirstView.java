package uniteProject.view;

import java.util.Scanner;

public class FirstView {
    Scanner sc = new Scanner(System.in);

    LoginView loginView = new LoginView();

    public void firstView() {
        System.out.println("안녕하세요. 기숙사 관리 시스템입니다.");
        System.out.println("수행할 일을 아래 번호로 입력해주세요.");
        System.out.println("1. 로그인\t2. 회원가입");

        switch (sc.nextInt()) {
            case 1: signInView(); break;
            case 2: signUpView(); break;
        }
    }

    public void signInView() {
        loginView.printSignIn();
    }

    public void signUpView() {
        loginView.printSignUp();
    }
}
