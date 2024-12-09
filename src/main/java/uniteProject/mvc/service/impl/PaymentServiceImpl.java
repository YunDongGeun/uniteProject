package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.Application;
import uniteProject.mvc.model.Payment;
import uniteProject.mvc.model.Student;
import uniteProject.mvc.repository.ApplicationRepository;
import uniteProject.mvc.repository.PaymentRepository;
import uniteProject.mvc.repository.StudentRepository;
import uniteProject.mvc.service.interfaces.PaymentService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;

    @Override
    public Protocol getPaymentAmount(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            String studentNumber = new String(data, StandardCharsets.UTF_8);
            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            Application application = applicationRepository.findByStudentId(student.getId())
                    .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));

            Payment payment = paymentRepository.findByApplicationId(application.getId())
                    .orElseThrow(() -> new RuntimeException("납부 정보를 찾을 수 없습니다."));

            response.setData(String.valueOf(payment.getAmount()).getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol processPayment(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            String[] paymentInfo = new String(data, StandardCharsets.UTF_8).split(",");
            String studentNumber = paymentInfo[0];
            int amount = Integer.parseInt(paymentInfo[1]);

            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            Application application = applicationRepository.findByStudentId(student.getId())
                    .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));

            Payment payment = Payment.builder()
                    .applicationId(application.getId())
                    .amount(amount)
                    .paymentStatus("PAID")
                    .paymentDate(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);
            response.setData("납부가 완료되었습니다.".getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol getPaidList(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            List<Payment> paidList = paymentRepository.findAllByPaymentStatus("PAID");
            StringBuilder resultBuilder = new StringBuilder();

            for (Payment payment : paidList) {
                Application application = applicationRepository.findById(payment.getApplicationId())
                        .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));
                Student student = studentRepository.findById(application.getStudentId())
                        .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

                resultBuilder.append(String.format("%s,%s,%d,%s\n",
                        student.getStudentNumber(),
                        student.getName(),
                        payment.getAmount(),
                        payment.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ));
            }
            response.setData(resultBuilder.toString().getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol getUnpaidList(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            List<Payment> unpaidList = paymentRepository.findAllByPaymentStatus("0");
            StringBuilder resultBuilder = new StringBuilder();

            for (Payment payment : unpaidList) {
                Application application = applicationRepository.findById(payment.getApplicationId())
                        .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));
                Student student = studentRepository.findById(application.getStudentId())
                        .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

                resultBuilder.append(String.format("%s,%s,%d\n",
                        student.getStudentNumber(),
                        student.getName(),
                        payment.getAmount()
                ));
            }
            response.setData(resultBuilder.toString().getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }
}