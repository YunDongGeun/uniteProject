package uniteProject.server.message;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class LoginMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 메시지 타입 상수
    public static final int LOGIN_REQUEST = 1;
    public static final int LOGIN_RESULT = 2;
    public static final int SIGNUP_REQUEST = 3;
    public static final int SIGNUP_RESULT = 4;
    public static final int LOGOUT = 5;

    // 결과 코드 상수
    public static final int SUCCESS = 100;
    public static final int FAIL_ID = 201;
    public static final int FAIL_PWD = 202;

    private int type;
    private int code;
    private String id;
    private String pwd;
    private String message;

    public LoginMessage(){
        this.type = 0;   // not defined yet
        this.code = 0;   // not defined yet
        this.id = null;
        this.pwd = null;
    }

}