package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.Application;
import uniteProject.mvc.model.Payment;
import uniteProject.mvc.model.Recruitment;
import uniteProject.mvc.model.Student;
import uniteProject.mvc.repository.*;
import uniteProject.mvc.service.interfaces.ApplicationService;
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
    private final RecruitmentRepository recruitmentRepository;
    private final FeeManagementRepository feeManagementRepository;

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
            if (paymentInfo.length < 2) {
                throw new IllegalArgumentException("결제 정보가 부족합니다.");
            }

            String studentNumber = paymentInfo[0];
            String paid = paymentInfo[1];

            // 1. 학생 정보 확인
            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            // 2. 신청 정보 확인
            Application application = applicationRepository.findByStudentId(student.getId())
                    .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));

            // 승인된 신청인지 확인
            if (!"선발".equals(application.getStatus())) {
                throw new RuntimeException("아직 승인되지 않은 신청입니다. 현재 상태: " + application.getStatus());
            }

            // 3. 기존 결제 내역 확인
            if (paymentRepository.findByApplicationIdAndStatus(application.getId(), "PAID").isPresent()) {
                throw new RuntimeException("이미 납부가 완료된 신청입니다.");
            }

            Payment pay = paymentRepository.findByApplicationId(application.getId())
                    .orElseThrow(() -> new RuntimeException("납부 정보를 찾을 수 없습니다."));

            if (paid.equals("납부")) {
                // 6. 결제 정보 저장
                Payment payment = Payment.builder()
                        .applicationId(application.getId())
                        .amount(pay.getAmount())
                        .paymentStatus(paid)
                        .paymentDate(LocalDateTime.now())
                        .build();

                paymentRepository.save(payment);

                // 7. 신청 상태 업데이트
                application.setIsPaid(true);
                application.setUpdateAt(LocalDateTime.now());
                applicationRepository.save(application);

                response.setData("납부가 완료되었습니다.".getBytes());
            } else response.setData("납부 실패.".getBytes());

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