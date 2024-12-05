package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;

public interface WithdrawalService {
    Protocol submitWithdrawal(byte[] data);
    Protocol checkRefundStatus(byte[] data);
    Protocol getWithdrawalList(byte[] data);
    Protocol processRefund(byte[] data);

}