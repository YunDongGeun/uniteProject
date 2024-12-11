package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.Application;
import uniteProject.mvc.model.Student;
import uniteProject.mvc.model.TBCertificate;
import uniteProject.mvc.repository.ApplicationRepository;
import uniteProject.mvc.repository.DocumentRepository;
import uniteProject.mvc.repository.StudentRepository;
import uniteProject.mvc.service.ChunkedDataHandler;
import uniteProject.mvc.service.interfaces.DocumentService;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final ChunkedDataHandler chunkedDataHandler = new ChunkedDataHandler();

    @Override
    public Protocol submitTBCertificate(byte[] data) {
        try {
            // 현재 처리 중인 학번 가져오기
            String studentNumber = chunkedDataHandler.getCurrentStudentNumber();
            if (studentNumber == null) {
                throw new IllegalStateException("학번이 먼저 제출되어야 합니다.");
            }

            // 청크 데이터 처리
            byte[] completeData = chunkedDataHandler.handleChunkedData(data);

            // 아직 모든 청크가 도착하지 않았다면
            if (completeData == null) {
                return null; // 중간 청크는 응답하지 않음
            }

            // 마지막 청크일 경우 - 최종 처리
            Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
            try {
                // Base64 디코딩
                byte[] imageData = Base64.getDecoder().decode(completeData);

                Student student = studentRepository.findByStudentNumber(studentNumber)
                        .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

                Application application = applicationRepository.findByStudentId(student.getId())
                        .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));

                // 한 번 더 검증 (동시성 문제 방지)
                if (documentRepository.existsByApplicationId(application.getId())) {
                    response.setCode(Protocol.CODE_FAIL);
                    response.setData("이미 제출된 결핵진단서가 있습니다.".getBytes(StandardCharsets.UTF_8));
                    return response;
                }

                TBCertificate certificate = TBCertificate.builder()
                        .applicationId(application.getId())
                        .image(imageData)
                        .uploadedAt(LocalDateTime.now())
                        .build();

                documentRepository.save(certificate);
                response.setData("결핵진단서가 성공적으로 제출되었습니다.".getBytes(StandardCharsets.UTF_8));

            } catch (IllegalArgumentException e) {
                response.setCode(Protocol.CODE_INVALID_REQ);
                response.setData(e.getMessage().getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                response.setCode(Protocol.CODE_FAIL);
                response.setData(e.getMessage().getBytes(StandardCharsets.UTF_8));
            }

            return response;

        } catch (Exception e) {
            Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_FAIL);
            response.setData(("결핵진단서 제출 중 오류가 발생했습니다: " + e.getMessage())
                    .getBytes(StandardCharsets.UTF_8));
            return response;
        }
    }

    @Override
    public Protocol checkSubmissionStatus(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);

        try {
            String studentNumber = new String(data, StandardCharsets.UTF_8);
            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            Application application = applicationRepository.findByStudentId(student.getId())
                    .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));

            Optional<TBCertificate> certificate = documentRepository.findByApplicationId(application.getId());

            if (certificate.isPresent() && certificate.get().getImage() != null) {
                // 이미지를 base64로 인코딩
                String base64Image = Base64.getEncoder().encodeToString(certificate.get().getImage());

                LocalDateTime uploadTime = certificate.get().getUploadedAt();
                String timeString = uploadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // 응답 형식: "제출완료,시간,base64이미지"
                String responseData = String.format("제출완료,%s,%s", timeString, base64Image);
                response.setData(responseData.getBytes(StandardCharsets.UTF_8));
            } else {
                response.setData("미제출".getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            String errorMessage = "제출 상태 확인 중 오류가 발생했습니다: " + e.getMessage();
            response.setData(errorMessage.getBytes(StandardCharsets.UTF_8));
        }

        return response;
    }

    @Override
    public Protocol submitTBCertificateBeforeId(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);

        try {
            if (data == null || data.length == 0) {
                throw new IllegalArgumentException("입력된 학번이 없습니다.");
            }

            String studentNumber = new String(data, StandardCharsets.UTF_8);

            // 학번 유효성 검사
            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            // 신청 정보 확인
            Application application = applicationRepository.findByStudentId(student.getId())
                    .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));

            // 이미 제출된 결핵진단서가 있는지 확인
            if (documentRepository.existsByApplicationId(application.getId())) {
                response.setCode(Protocol.CODE_FAIL);
                response.setData("이미 제출된 결핵진단서가 있습니다.".getBytes(StandardCharsets.UTF_8));
                return response;
            }

            // ChunkedDataHandler에 학번 정보 미리 저장
            chunkedDataHandler.setCurrentStudentNumber(studentNumber);

            response.setData("학번이 확인되었습니다. 파일을 전송해주세요.".getBytes(StandardCharsets.UTF_8));

        } catch (IllegalArgumentException e) {
            response.setCode(Protocol.CODE_INVALID_REQ);
            response.setData(e.getMessage().getBytes(StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(("예기치 않은 오류가 발생했습니다: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }

        return response;
    }
}