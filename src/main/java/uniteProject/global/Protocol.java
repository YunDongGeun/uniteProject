package uniteProject.global;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Protocol {
    // Message Type
    public static final byte TYPE_UNDEFINED = -1;    // 초기값
    public static final byte TYPE_AUTH = 0x01;      // 인증 관련
    public static final byte TYPE_RESPONSE = (byte)0xFF; // 시스템 응답

    // Message Code
    public static final byte CODE_AUTH_ID_REQ = 0x01;  // ID 요청
    public static final byte CODE_AUTH_PW_REQ = 0x02;  // PW 요청

    // Response Code
    public static final byte CODE_SUCCESS = 0x01;
    public static final byte CODE_FAIL = 0x02;
    public static final byte CODE_NO_AUTH = 0x03;
    public static final byte CODE_INVALID_REQ = 0x04;

    public static final int LEN_MAX = 1024;          // 최대 데이터 길이
    public static final int LEN_TYPE = 1;            // 타입 길이
    public static final int LEN_CODE = 1;            // 코드 길이
    public static final int LEN_LENGTH = 2;          // 길이 필드의 길이
    public static final int LEN_HEADER = 4;          // 헤더의 길이 (타입 + 코드 + 길이)

    // 회원가입 관련
    public static final byte TYPE_REGISTER = 0x08;
    public static final byte CODE_REGISTER_REQUEST = 0x01;

    // 응답 메시지를 위한 상수
    public static final String MSG_REGISTER_SUCCESS = "회원가입이 완료되었습니다.";
    public static final String MSG_REGISTER_FAIL = "회원가입에 실패했습니다.";
    public static final String MSG_DUPLICATE_USER = "이미 존재하는 사용자입니다.";

    @Setter
    protected byte type;
    @Setter
    protected byte code;
    @Setter
    protected short length;
    protected byte[] data;

    public Protocol() {
        this(TYPE_UNDEFINED, (byte)0);
    }

    public Protocol(byte type, byte code) {
        this.type = type;
        this.code = code;
        this.length = 0;
        this.data = null;
    }

    public void setData(byte[] data) {
        this.data = data;
        this.length = (short) (data == null ? 0 : data.length);
    }
}