package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;

public interface AuthService {
    /**
     * 사용자 ID 검증
     * @param data 검증할 사용자 ID 데이터
     * @return 검증 결과를 포함한 Protocol 객체
     */
    Protocol validateId(byte[] data);

    /**
     * 사용자 비밀번호 검증
     * @param data 검증할 비밀번호 데이터
     * @return 검증 결과를 포함한 Protocol 객체
     */
    Protocol validatePassword(byte[] data);

    /**
     * 회원 가입 처리
     * @param data 회원 가입 정보 데이터
     * @return 가입 결과를 포함한 Protocol 객체
     */
    Protocol register(byte[] data);
}