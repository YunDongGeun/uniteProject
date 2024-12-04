package uniteProject.global;

import lombok.Getter;
import lombok.Setter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Getter
public class Protocol {
    // Message Type
    public static final byte TYPE_UNDEFINED = -1;    // 초기값
    public static final byte TYPE_AUTH = 0x01;       // 인증 관련
    public static final byte TYPE_SCHEDULE = 0x02;   // 선발 일정 및 비용 관리
    public static final byte TYPE_APPLICATION = 0x03; // 입사 신청 관련
    public static final byte TYPE_ROOM = 0x04;       // 합격/호실 관련
    public static final byte TYPE_PAYMENT = 0x05;    // 비용 관련
    public static final byte TYPE_DOCUMENT = 0x06;   // 서류 관련
    public static final byte TYPE_WITHDRAWAL = 0x07; // 퇴사/환불 관련
    public static final byte TYPE_REGISTER = 0x08;   // 회원가입 관련
    public static final byte TYPE_RESPONSE = (byte)0xFF; // 시스템 응답

    // Auth Codes (0x01)
    public static final byte CODE_AUTH_ID_REQ = 0x01;  // ID 요청
    public static final byte CODE_AUTH_PW_REQ = 0x02;  // PW 요청

    // Schedule Codes (0x02)
    public static final byte CODE_SCHEDULE_VIEW = 0x01;     // 일정 조회
    public static final byte CODE_SCHEDULE_FEE_VIEW = 0x02; // 비용 조회
    public static final byte CODE_SCHEDULE_REG = 0x11;      // 일정 등록
    public static final byte CODE_SCHEDULE_FEE_REG = 0x12;  // 비용 등록

    // Application Codes (0x03)
    public static final byte CODE_APPLICATION_SUBMIT = 0x01; // 신청서 제출
    public static final byte CODE_APPLICATION_STATUS = 0x02; // 신청 상태 조회
    public static final byte CODE_APPLICATION_LIST = 0x11;   // 신청자 목록 조회

    // Room Assignment Codes (0x04)
    public static final byte CODE_ROOM_PASS_CHECK = 0x01;   // 합격 여부 조회
    public static final byte CODE_ROOM_INFO = 0x02;         // 호실 정보 조회
    public static final byte CODE_ROOM_SELECT = 0x11;       // 합격자 선발
    public static final byte CODE_ROOM_ASSIGN = 0x12;       // 호실 배정

    // Payment Codes (0x05)
    public static final byte CODE_PAYMENT_AMOUNT = 0x01;    // 납부 금액 조회
    public static final byte CODE_PAYMENT_PAY = 0x02;       // 납부 처리
    public static final byte CODE_PAYMENT_PAID_LIST = 0x11; // 납부자 명단
    public static final byte CODE_PAYMENT_UNPAID_LIST = 0x12; // 미납자 명단

    // Document Codes (0x06)
    public static final byte CODE_DOCUMENT_SUBMIT = 0x01;   // 결핵진단서 제출
    public static final byte CODE_DOCUMENT_STATUS = 0x02;   // 제출현황 조회

    // Withdrawal Codes (0x07)
    public static final byte CODE_WITHDRAWAL_REQ = 0x01;    // 퇴사 신청
    public static final byte CODE_WITHDRAWAL_STATUS = 0x02; // 환불 상태 조회
    public static final byte CODE_WITHDRAWAL_LIST = 0x11;   // 퇴사자 명단
    public static final byte CODE_WITHDRAWAL_REFUND = 0x12; // 환불 처리

    // Response Code
    public static final byte CODE_SUCCESS = 0x01;        // 성공
    public static final byte CODE_FAIL = 0x02;          // 실패
    public static final byte CODE_NO_AUTH = 0x03;       // 권한 없음
    public static final byte CODE_INVALID_REQ = 0x04;   // 잘못된 요청

    // Protocol 메시지 구조체 정의
    public static final int LEN_MAX = 1024;        // 최대 데이터 길이
    public static final int LEN_TYPE = 1;          // 타입 길이
    public static final int LEN_CODE = 1;          // 코드 길이
    public static final int LEN_LENGTH = 2;        // 길이 필드의 길이
    public static final int LEN_HEADER = 4;        // 헤더의 길이 (타입 + 코드 + 길이)

    // 회원가입 관련
    public static final byte CODE_REGISTER_REQUEST = 0x01;  // 회원가입 요청

    // 응답 메시지
    public static final String MSG_REGISTER_SUCCESS = "회원가입이 완료되었습니다.";
    public static final String MSG_REGISTER_FAIL = "회원가입에 실패했습니다.";
    public static final String MSG_DUPLICATE_USER = "이미 존재하는 사용자입니다.";
    public static final String MSG_AUTH_SUCCESS = "인증에 성공했습니다.";
    public static final String MSG_AUTH_FAIL = "인증에 실패했습니다.";
    public static final String MSG_NO_AUTH = "권한이 없습니다.";
    public static final String MSG_INVALID_REQ = "잘못된 요청입니다.";

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

    // 문자열을 byte[]로 변환하여 데이터 설정
    public void setData(String message) {
        setData(message.getBytes(StandardCharsets.UTF_8));
    }

    // 전체 메시지를 byte[]로 변환
    public byte[] getPacket() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(type);
        outputStream.write(code);
        outputStream.write(ByteBuffer.allocate(2).putShort(length).array());
        if (data != null) {
            outputStream.write(data);
        }
        return outputStream.toByteArray();
    }

    // byte[]로부터 Protocol 객체 생성
    public static Protocol parsePacket(byte[] packet) throws IOException {
        if (packet.length < LEN_HEADER) {
            throw new IOException("Invalid packet length");
        }

        Protocol protocol = new Protocol();
        protocol.setType(packet[0]);
        protocol.setCode(packet[1]);

        ByteBuffer buffer = ByteBuffer.wrap(packet, 2, 2);
        short length = buffer.getShort();
        protocol.setLength(length);

        if (length > 0) {
            byte[] data = new byte[length];
            System.arraycopy(packet, LEN_HEADER, data, 0, length);
            protocol.setData(data);
        }

        return protocol;
    }

    // 데이터를 문자열로 변환
    public String getDataAsString() {
        return data == null ? "" : new String(data, StandardCharsets.UTF_8);
    }

    // Protocol 객체의 문자열 표현
    @Override
    public String toString() {
        return String.format("Protocol{type=0x%02X, code=0x%02X, length=%d, data=%s}",
                type, code, length, getDataAsString());
    }

    // 타입과 코드가 유효한지 검사
    public boolean isValid() {
        return type != TYPE_UNDEFINED && length <= LEN_MAX;
    }
}