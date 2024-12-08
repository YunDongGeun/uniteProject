package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;

public interface WithdrawalService {

    /**
     * 퇴사 신청
     * @param data 퇴사 신청 후 안내메시지
     * @return 퇴사 신청 후 안내메시지를 포함한 Protocol 객체
     */
    Protocol submitWithdrawal(byte[] data);

    /**
     * 환불 조회
     * @param data 환불 조회 후 안내메시지
     * @return 환불 조회 후 안내메시지를 포함한 Protocol 객체
     */
    Protocol checkRefundStatus(byte[] data);

    /**
     * 환불자 조회
     * @param data 환불자 조회 리스트 데이터
     * @return 환불자 조회 리스트 데이터를 포함한 Protocol 객체
     */
    Protocol getWithdrawalList(byte[] data);

    /**
     * 환불 진행
     * @param data 환불 진행 후 안내메시지
     * @return 환불 진행 후 안내메시지를 포함한 Protocol 객체
     */
    Protocol processRefund(byte[] data);

}