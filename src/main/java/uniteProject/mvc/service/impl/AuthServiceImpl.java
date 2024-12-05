package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.exception.ServerException;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.Member;
import uniteProject.mvc.model.Student;
import uniteProject.mvc.repository.MemberRepository;
import uniteProject.mvc.repository.StudentRepository;
import uniteProject.mvc.service.interfaces.AuthService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MemberRepository memberRepository;
    private final StudentRepository studentRepository;

    @Override
    public Protocol validateId(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);

        try {
            if (data == null || data.length > Protocol.LEN_MAX) {
                response.setCode(Protocol.CODE_INVALID_REQ);
                response.setData(Protocol.MSG_INVALID_REQ);
                return response;
            }

            String username = new String(data, StandardCharsets.UTF_8);

            // ID 형식 검증
            if (!isValidUsername(username)) {
                response.setCode(Protocol.CODE_FAIL);
                response.setData("유효하지 않은 ID 형식입니다.");
                return response;
            }

            // ID 존재 여부 확인
            boolean exists = memberRepository.existsByUsername(username);
            if (exists) {
                response.setCode(Protocol.CODE_FAIL);
                response.setData("이미 존재하는 ID 입니다.");
            } else {
                response.setData("사용 가능한 ID 입니다.");
            }

            return response;

        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData("ID 검증 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Protocol validatePassword(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);

        try {
            if (data == null || data.length > Protocol.LEN_MAX) {
                response.setCode(Protocol.CODE_INVALID_REQ);
                response.setData(Protocol.MSG_INVALID_REQ);
                return response;
            }

            // data format: "username,password"
            String[] credentials = new String(data, StandardCharsets.UTF_8).split(",");
            if (credentials.length != 2) {
                response.setCode(Protocol.CODE_INVALID_REQ);
                response.setData("잘못된 인증 데이터 형식입니다.");
                return response;
            }

            String username = credentials[0];
            String password = credentials[1];

            // 회원 정보 조회
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다."));

            // 비밀번호 직접 비교
            if (password.equals(member.getPassword())) {
                response.setData(Protocol.MSG_AUTH_SUCCESS);
            } else {
                response.setCode(Protocol.CODE_FAIL);
                response.setData("비밀번호가 일치하지 않습니다.");
            }

            return response;

        } catch (NoSuchElementException e) {
            response.setCode(Protocol.CODE_NO_AUTH);
            response.setData(e.getMessage());
            return response;
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData("비밀번호 검증 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Protocol register(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);

        try {
            if (data == null || data.length > Protocol.LEN_MAX) {
                response.setCode(Protocol.CODE_INVALID_REQ);
                response.setData(Protocol.MSG_INVALID_REQ);
                return response;
            }

            // data format: "username password role name studentNumber major"
            String[] registerData = new String(data, StandardCharsets.UTF_8).split(" ");
            if (registerData.length < 6) {
                response.setCode(Protocol.CODE_INVALID_REQ);
                response.setData("필수 회원가입 정보가 부족합니다.");
                return response;
            }

            String username = registerData[0];
            String password = registerData[1];
            String role = registerData[2];
            String name = registerData[3];
            String studentNumber = registerData[4];
            String major = registerData[5];

            // 중복 ID 검사
            if (memberRepository.existsByUsername(username)) {
                response.setCode(Protocol.CODE_FAIL);
                response.setData(Protocol.MSG_DUPLICATE_USER);
                return response;
            }

            // 회원 정보 생성
            Member member = Member.builder()
                    .username(username)
                    .password(password)  // 직접 저장
                    .role(role)
                    .createdAt(LocalDateTime.now())
                    .build();

            Member savedMember = memberRepository.save(member);

            // 학생 정보 생성 (role이 STUDENT인 경우)
            if ("STUDENT".equals(role)) {
                Student student = Student.builder()
                        .memberId(savedMember.getId())
                        .name(name)
                        .studentNumber(studentNumber)
                        .major(major)
                        .submitDocument(false)
                        .build();

                studentRepository.save(student);
            }

            response.setData(Protocol.MSG_REGISTER_SUCCESS);
            return response;

        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData("회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    // ID 형식 검증 메소드
    private boolean isValidUsername(String username) {
        // 영문자, 숫자만 허용, 4~20자
        return username != null &&
                username.matches("^[a-zA-Z0-9]{4,20}$");
    }
}