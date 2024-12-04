package uniteProject.mvc.service.interfaces;

import uniteProject.persistence.dto.req_res.Response;

public interface PaymentService {
    Response getPaymentAmount(byte[] data);
    Response processPayment(byte[] data);
    Response getPaidList(byte[] data);
    Response getUnpaidList(byte[] data);

    byte[] handlePaymentRequest(byte code, byte[] data);
}