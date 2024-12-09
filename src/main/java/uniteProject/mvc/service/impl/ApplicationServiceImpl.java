package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.exception.ServerException;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.Application;
import uniteProject.mvc.model.Recruitment;
import uniteProject.mvc.model.Student;
import uniteProject.mvc.repository.ApplicationRepository;
import uniteProject.mvc.repository.RecruitmentRepository;
import uniteProject.mvc.repository.StudentRepository;
import uniteProject.mvc.service.interfaces.ApplicationService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final RecruitmentRepository recruitmentRepository;

    @Override
    public Protocol submitApplication(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);

        System.out.println("start");
        try {
            if (data == null || data.length > Protocol.LEN_MAX) {
                response.setCode(Protocol.CODE_INVALID_REQ);
                response.setData("데이터 크기가 유효하지 않습니다.".getBytes());
                System.out.println("error");
                return response;
            }

            // data format: "studentNumber,dormName,dormitoryPreference"
            String[] applicationData = new String(data, StandardCharsets.UTF_8).split(",");
            if (applicationData.length < 3) {
                response.setCode(Protocol.CODE_INVALID_REQ);
                response.setData("필수 신청 정보가 부족합니다.".getBytes());
                return response;
            }

            String studentNumber = applicationData[0];
            String dormName = applicationData[1];
            int dormitoryPreference = Integer.parseInt(applicationData[2]);

            // 학생 정보 확인
            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            // 생활관 모집공고 확인
            Recruitment recruitment = recruitmentRepository.findByDormName(dormName)
                    .orElseThrow(() -> new RuntimeException("해당 생활관의 모집공고를 찾을 수 없습니다."));

            // 기존 신청 내역 확인
            if (applicationRepository.existsByStudentId(student.getId())) {
                response.setCode(Protocol.CODE_FAIL);
                response.setData("이미 신청 내역이 존재합니다.".getBytes());
                return response;
            }

            // dormitoryPreference 값 검증 (1 또는 2)
            if (dormitoryPreference < 1 || dormitoryPreference > 2) {
                response.setCode(Protocol.CODE_INVALID_REQ);
                response.setData("기숙사 지망 순위는 1 또는 2만 가능합니다.".getBytes());
                return response;
            }

            // 신청서 생성
            Application application = Application.builder()
                    .studentId(student.getId())
                    .recruitmentId(recruitment.getId())
                    .status("대기")
                    .preference(dormitoryPreference)
                    .isPaid(false)
                    .priorityScore(calculatePriorityScore(student))
                    .createdAt(LocalDateTime.now())
                    .updateAt(LocalDateTime.now())
                    .build();

            applicationRepository.save(application);
            response.setData("입사 신청이 완료되었습니다.".getBytes());

        } catch (NumberFormatException e) {
            response.setCode(Protocol.CODE_INVALID_REQ);
            response.setData("잘못된 형식의 데이터입니다.".getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(("신청 처리 중 오류가 발생했습니다: " + e.getMessage()).getBytes());
        }

        return response;
    }

    @Override
    public Protocol checkApplicationStatus(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);

        try {
            String studentNumber = new String(data, StandardCharsets.UTF_8);

            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            Optional<Application> application = applicationRepository.findByStudentId(student.getId());

            if (application.isPresent()) {
                String statusInfo = String.format("신청상태: %s, 납부여부: %s, 신청일자: %s",
                        application.get().getStatus(),
                        application.get().getIsPaid() ? "완료" : "미납",
                        application.get().getCreatedAt().toString()
                );
                response.setData(statusInfo.getBytes());
            } else {
                response.setCode(Protocol.CODE_FAIL);
                response.setData("신청 내역이 존재하지 않습니다.".getBytes());
            }

        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(("상태 조회 중 오류가 발생했습니다: " + e.getMessage()).getBytes());
        }

        return response;
    }

    @Override
    public Protocol getApplicationList(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);

        try {
            // data format: "status,dormitoryPreference"
            String[] searchCriteria = new String(data, StandardCharsets.UTF_8).split(",");
            String status = searchCriteria.length > 0 ? searchCriteria[0] : null;
            String preference = searchCriteria.length > 1 ? searchCriteria[1] : null;

            List<Application> applications = applicationRepository.findAllBySearchCriteria(status, preference);

            // 응답 데이터 생성
            StringBuilder resultBuilder = new StringBuilder();
            for (Application app : applications) {

                String dormName = recruitmentRepository.findById(app.getRecruitmentId()).get().getDormName();

                studentRepository.findById(app.getStudentId()).ifPresent(
                        student -> resultBuilder.append(String.format("%s,%s,%s,%s,%s,%s\n",
                        student.getStudentNumber(),
                        student.getName(),
                        app.getStatus(),
                        dormName,
                        app.getIsPaid() ? "납부완료" : "미납",
                        app.getCreatedAt()
                )));

            }

            response.setData(resultBuilder.toString().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(("목록 조회 중 오류가 발생했습니다: " + e.getMessage()).getBytes());
        }

        return response;
    }

    private int calculatePriorityScore(Student student) {
        int score = 0;

        // 학생 유형에 따른 우선순위 점수 (대학원생 우선)
        if ("대학원생".equals(student.getStudentType())) {
            score += 1000;
        }

        // GPA 점수 (최대 45점)
        if (student.getGpa() != null) {
            score += (int) (student.getGpa() * 10); // 4.5 만점 기준 최대 45점
        }

        // 거리 점수 (최대 30점)
        if (student.getDistanceFromSchool() != null) {
            if (student.getDistanceFromSchool() > 20.0) score += 30;
            else if (student.getDistanceFromSchool() > 10.0) score += 20;
            else if (student.getDistanceFromSchool() > 5.0) score += 10;
        }

        return score;
    }
}