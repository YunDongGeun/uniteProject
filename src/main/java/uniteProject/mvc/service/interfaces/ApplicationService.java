package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;
import uniteProject.persistence.dto.req_res.Response;

public interface ApplicationService {
    /**
     * 입사 신청서 제출
     * @param data 입사 신청 정보가 담긴 데이터
     * @return Protocol 응답
     */
    Protocol submitApplication(byte[] data);

    /**
     * 신청 상태 확인
     * @param data 학번 정보가 담긴 데이터
     * @return Protocol 응답
     */
    Protocol checkApplicationStatus(byte[] data);

    /**
     * 신청자 목록 조회 (관리자용)
     * @param data 조회 조건이 담긴 데이터
     * @return Protocol 응답
     */
    Protocol getApplicationList(byte[] data);

    byte[] handleApplicationRequest(byte code, byte[] data);
}