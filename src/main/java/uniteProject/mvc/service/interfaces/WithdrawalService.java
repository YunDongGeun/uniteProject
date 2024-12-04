package uniteProject.mvc.service.interfaces;

import uniteProject.persistence.dto.req_res.Response;

public interface WithdrawalService {
    Response submitWithdrawal(byte[] data);
    Response checkRefundStatus(byte[] data);
    Response getWithdrawalList(byte[] data);
    Response processRefund(byte[] data);

    byte[] handleWithdrawalRequest(byte code, byte[] data);
}