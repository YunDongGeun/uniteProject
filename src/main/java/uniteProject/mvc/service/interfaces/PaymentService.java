package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;

public interface PaymentService {
    /**
     * 납부 금액 조회
     * @param data 납부 금액
     * @return 납부 금액를 포함한 Protocol 객체
     */
    Protocol getPaymentAmount(byte[] data);

    /**
     * 납부 처리
     * @param data 납부 완료 메시지
     * @return 납부 완료 메시지를 포함한 Protocol 객체
     */
    Protocol processPayment(byte[] data);

    /**
     * 납부자 조회
     * @param data 납부자 리스트 데이터
     * @return 납부자 리스트 데이터를 포함한 Protocol 객체
     */
    Protocol getPaidList(byte[] data);

    /**
     * 미납부자 조회
     * @param data 미납부자 리스트 데이터
     * @return 미납부자 리스트 데이터를 포함한 Protocol 객체
     */
    Protocol getUnpaidList(byte[] data);
}