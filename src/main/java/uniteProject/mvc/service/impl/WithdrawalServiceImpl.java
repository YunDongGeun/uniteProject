package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.*;
import uniteProject.mvc.repository.*;
import uniteProject.mvc.service.interfaces.WithdrawalService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {
    private final WithdrawalRepository withdrawalRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final RoomStatusRepository roomStatusRepository;

    @Override
    public Protocol submitWithdrawal(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            // data format: "studentNumber,leaveDate,bankName,accountNumber"
            String[] withdrawalData = new String(data, StandardCharsets.UTF_8).split(",");
            if (withdrawalData.length < 4) {
                throw new IllegalArgumentException("필수 퇴사 정보가 부족합니다. (학번, 퇴사일, 은행명, 계좌번호 필요)");
            }

            String studentNumber = withdrawalData[0];
            LocalDateTime leaveDate = LocalDateTime.parse(withdrawalData[1]);
            String bankName = withdrawalData[2];
            String accountNumber = withdrawalData[3];

            // 1. 학생 정보 확인
            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            // 2. 기존 퇴사 신청 확인
            if (withdrawalRepository.findByStudentId(student.getId()).isPresent()) {
                throw new RuntimeException("이미 퇴사 신청이 존재합니다.");
            }

            // 3. 계좌 정보 저장
            Account account = Account.builder()
                    .studentId(student.getId())
                    .bankName(bankName)
                    .accountNumber(accountNumber)
                    .build();
            accountRepository.save(account);

            // 4. 환불 금액 계산
            int refundAmount = calculateRefundAmount(student.getId(), leaveDate);

            // 5. 퇴사 신청 저장
            Withdrawal withdrawal = Withdrawal.builder()
                    .studentId(student.getStudentNumber())
                    .leaveDate(leaveDate)
                    .status("대기")  // 초기 상태는 '대기'
                    .refundAmount(refundAmount)
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
                Account account = accountRepository.findByStudentId(student.getId())
                        .orElseThrow(() -> new RuntimeException("계좌 정보를 찾을 수 없습니다."));

                String statusInfo = String.format("%s,%d,%s,%s,%s",
                        withdrawal.get().getStatus(),
                        withdrawal.get().getRefundAmount(),
                        withdrawal.get().getLeaveDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        account.getBankName(),
                        account.getAccountNumber()
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

                Account account = accountRepository.findByStudentId(student.getId())
                        .orElseThrow(() -> new RuntimeException("계좌 정보를 찾을 수 없습니다."));

                resultBuilder.append(String.format("%s,%s,%s,%d,%s,%s,%s\n",
                        student.getStudentNumber(),
                        student.getName(),
                        withdrawal.getLeaveDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        withdrawal.getRefundAmount(),
                        withdrawal.getStatus(),
                        account.getBankName(),
                        account.getAccountNumber()
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
            // data format: "withdrawalId,approval" (approval: true/false)
            String[] refundData = new String(data, StandardCharsets.UTF_8).split(",");
            Long withdrawalId = Long.parseLong(refundData[0]);
            boolean isApproved = Boolean.parseBoolean(refundData[1]);

            Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                    .orElseThrow(() -> new RuntimeException("퇴사 신청 정보를 찾을 수 없습니다."));

            // 승인/거부 처리
            if (isApproved) {
                withdrawal.setStatus("승인");
                // 실제 환불 처리 후
                withdrawal.setStatus("환불완료");
            } else {
                withdrawal.setStatus("거부");
            }

            withdrawalRepository.save(withdrawal);
            response.setData("환불 처리가 완료되었습니다.".getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    private LocalDateTime getEndOfSemester() {
        // 현재 학기 종료일 반환
        return LocalDateTime.of(2024, 12, 21, 0, 0);
    }

    private int calculateRefundAmount(Long studentId, LocalDateTime leaveDate) {
        // 1. 납부한 금액 조회
        Application application = applicationRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));

        Payment payment = paymentRepository.findByApplicationId(application.getId())
                .orElseThrow(() -> new RuntimeException("납부 정보를 찾을 수 없습니다."));

        // 2. 입사 여부 확인
        boolean hasCheckedIn = roomStatusRepository.findByStudentId(studentId).isPresent();

        // 3. 환불 금액 계산
        int totalAmount = payment.getAmount();

        if (!hasCheckedIn) {
            // 입사 전 퇴사: 100% 환불
            return totalAmount;
        } else {
            // 입사 후 퇴사: 거주 잔여기간 일할계산
            long totalDays = 120; // 한 학기 4개월 기준
            long remainingDays = ChronoUnit.DAYS.between(leaveDate, getEndOfSemester());
            return (int) (totalAmount * (remainingDays / (double) totalDays));
        }
    }
}