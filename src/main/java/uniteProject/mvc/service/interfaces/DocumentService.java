package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;

public interface DocumentService {
    /**
     * 결핵진단서 제출
     * @param data 결핵진단서 이미지 데이터
     * @return 검증 결과를 포함한 Protocol 객체
     */
    Protocol submitTBCertificate(byte[] data);

    /**
     * 제출 상태 확인
     * @param data 제출 상태(제출 완료, 미제출), 제출 시간
     * @return 제출 상태를 포함한 Protocol 객체
     */
    Protocol checkSubmissionStatus(byte[] data);

    Protocol submitTBCertificateBeforeId(byte[] data);
}
