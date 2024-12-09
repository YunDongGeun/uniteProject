package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.Student;
import uniteProject.mvc.model.Withdrawal;
import uniteProject.mvc.repository.ApplicationRepository;
import uniteProject.mvc.repository.StudentRepository;
import uniteProject.mvc.repository.WithdrawalRepository;
import uniteProject.mvc.service.interfaces.WithdrawalService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {
    private final WithdrawalRepository withdrawalRepository;
    private final StudentRepository studentRepository;

    @Override
    public Protocol submitWithdrawal(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            // data format: "studentNumber,leaveDate,reason"
            String[] withdrawalData = new String(data, StandardCharsets.UTF_8).split(",");
            if (withdrawalData.length < 2) {
                throw new IllegalArgumentException("필수 퇴사 정보가 부족합니다.");
            }

            String studentNumber = withdrawalData[0];
            LocalDateTime leaveDate = LocalDateTime.parse(withdrawalData[1]);

            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            Withdrawal withdrawal = Withdrawal.builder()
                    .studentId(student.getStudentNumber())
                    .leaveDate(leaveDate)
                    .status("PENDING")
                    .refundAmount(calculateRefundAmount(student.getId(), leaveDate))
                    .build();

            withdrawalRepository.save(withdrawal);
            response.setData("퇴사 신청이 완료되었습니다.".getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol checkRefundStatus(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            String studentNumber = new String(data, StandardCharsets.UTF_8);
            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            Optional<Withdrawal> withdrawal = withdrawalRepository.findByStudentId(student.getId());
            if (withdrawal.isPresent()) {
                String statusInfo = String.format("%s,%d,%s",
                        withdrawal.get().getStatus(),
                        withdrawal.get().getRefundAmount(),
                        withdrawal.get().getLeaveDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                );
                response.setData(statusInfo.getBytes());
            } else {
                response.setCode(Protocol.CODE_FAIL);
                response.setData("퇴사 신청 내역이 없습니다.".getBytes());
            }
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol getWithdrawalList(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            List<Withdrawal> withdrawals = withdrawalRepository.findAll();
            StringBuilder resultBuilder = new StringBuilder();

            for (Withdrawal withdrawal : withdrawals) {
                Student student = studentRepository.findByStudentNumber(withdrawal.getStudentId())
                        .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

                resultBuilder.append(String.format("%s,%s,%s,%d,%s\n",
                        student.getStudentNumber(),
                        student.getName(),
                        withdrawal.getLeaveDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        withdrawal.getRefundAmount(),
                        withdrawal.getStatus()
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
    public Protocol processRefund(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            String[] refundData = new String(data, StandardCharsets.UTF_8).split(",");
            Long withdrawalId = Long.parseLong(refundData[0]);

            Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                    .orElseThrow(() -> new RuntimeException("퇴사 신청 정보를 찾을 수 없습니다."));

            withdrawal.setStatus("REFUNDED");
            withdrawalRepository.save(withdrawal);

            response.setData("환불 처리가 완료되었습니다.".getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    private int calculateRefundAmount(Long studentId, LocalDateTime leaveDate) {
        // 환불 금액 계산 로직 구현
        return 100000; // 임시 구현
    }
}