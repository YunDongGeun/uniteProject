package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;

public interface PaymentService {
    Protocol getPaymentAmount(byte[] data);
    Protocol processPayment(byte[] data);
    Protocol getPaidList(byte[] data);
    Protocol getUnpaidList(byte[] data);
}