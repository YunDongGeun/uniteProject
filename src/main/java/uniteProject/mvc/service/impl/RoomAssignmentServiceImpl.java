package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.*;
import uniteProject.mvc.repository.*;
import uniteProject.mvc.service.interfaces.RoomAssignmentService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
public class RoomAssignmentServiceImpl implements RoomAssignmentService {
    private final RoomStatusRepository roomStatusRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;
    private final DocumentRepository documentRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final FeeManagementRepository feeManagementRepository;
    private final DormitoryRepository dormitoryRepository;

    @Override
    public Protocol checkPassStatus(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            String studentNumber = new String(data, StandardCharsets.UTF_8);
            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            Application application = applicationRepository.findByStudentId(student.getId())
                    .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다."));

            response.setData(application.getStatus().getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol checkRoomInfo(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            String studentNumber = new String(data, StandardCharsets.UTF_8);
            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

            RoomStatus roomStatus = roomStatusRepository.findByStudentId(student.getId())
                    .orElseThrow(() -> new RuntimeException("배정된 방 정보가 없습니다."));

            Room room = roomRepository.findById(roomStatus.getRoomId())
                    .orElseThrow(() -> new RuntimeException("방 정보를 찾을 수 없습니다."));

            String roomInfo = String.format("%d동 %d호 %s번 침대",
                    room.getDormitoryId(), room.getRoomNumber(), roomStatus.getBedNumber());
            response.setData(roomInfo.getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol selectPassedStudents(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            String preferenceStr = new String(data, StandardCharsets.UTF_8);
            int preference = Integer.parseInt(preferenceStr);

            if (preference != 1 && preference != 2) {
                throw new IllegalArgumentException("선발 지망 순위는 1 또는 2만 가능합니다.");
            }

            int passedCount = (preference == 1) ?
                    processFirstPreference() :
                    processSecondPreference();

            String result = String.format(
                    "총 %d명의 %d지망 합격자가 선발되었습니다. 결제 정보와 결핵진단서 제출 정보가 생성되었습니다.",
                    passedCount,
                    preference
            );
            response.setData(result.getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    private int processFirstPreference() {
        List<Application> firstPreferenceApplications = applicationRepository
                .findAllByStatusAndPreferenceOrderByPriorityScoreDesc("대기", 1);

        int passedCount = 0;
        Set<Long> selectedStudentIds = new HashSet<>();
        Map<Long, Integer> recruitmentSelectionCount = new HashMap<>();  // 생활관별 선발 인원 카운트

        // 생활관별 수용 가능 인원수 확인
        Map<Long, Integer> recruitmentCapacities = getRecruitmentCapacities();

        // 1지망자 처리
        for (Application app : firstPreferenceApplications) {
            if (selectedStudentIds.contains(app.getStudentId())) {
                continue;
            }

            // 해당 생활관의 현재까지 선발된 인원 확인
            int currentCount = recruitmentSelectionCount
                    .getOrDefault(app.getRecruitmentId(), 0);

            // 수용 가능 인원 확인
            int maxCapacity = recruitmentCapacities
                    .getOrDefault(app.getRecruitmentId(), 0);

            // 수용 가능 인원 초과 체크
            if (currentCount >= maxCapacity) {
                continue;
            }

            // 룸 타입별 가능 여부 체크
            if (!hasAvailableRoomForType(app.getRecruitmentId(), app.getRoomType())) {
                continue;
            }

            // 선발 처리
            app.setStatus("선발");
            app.setUpdateAt(LocalDateTime.now());
            applicationRepository.save(app);

            // 카운트 업데이트
            recruitmentSelectionCount.put(
                    app.getRecruitmentId(),
                    currentCount + 1
            );

            selectedStudentIds.add(app.getStudentId());

            createPaymentInfo(app);
            createTBCertificateInfo(app);

            passedCount++;

            // 해당 학생의 2지망 자동 거부 처리
            rejectOtherApplications(app.getStudentId(), app.getId());
        }

        // 선발되지 않은 1지망 신청 거부 처리
        for (Application app : firstPreferenceApplications) {
            if (!selectedStudentIds.contains(app.getStudentId())) {
                app.setStatus("거부");
                app.setUpdateAt(LocalDateTime.now());
                applicationRepository.save(app);
            }
        }

        return passedCount;
    }

    private int processSecondPreference() {
        List<Application> secondPreferenceApplications = applicationRepository
                .findAllByStatusAndPreferenceOrderByPriorityScoreDesc("대기", 2);

        Map<Long, Integer> recruitmentSelectionCount = new HashMap<>();
        Map<Long, Integer> recruitmentCapacities = getRecruitmentCapacities();

        int passedCount = 0;

        for (Application app : secondPreferenceApplications) {
            Optional<Application> firstPreference = applicationRepository
                    .findByStudentIdAndPreference(app.getStudentId(), 1);

            if (firstPreference.isEmpty() ||
                    !"거부".equals(firstPreference.get().getStatus())) {
                continue;
            }

            // 수용 인원 체크
            int currentCount = recruitmentSelectionCount
                    .getOrDefault(app.getRecruitmentId(), 0);
            int maxCapacity = recruitmentCapacities
                    .getOrDefault(app.getRecruitmentId(), 0);

            if (currentCount >= maxCapacity) {
                continue;
            }

            // 룸 타입별 가능 여부 체크
            if (!hasAvailableRoomForType(app.getRecruitmentId(), app.getRoomType())) {
                continue;
            }

            app.setStatus("선발");
            app.setUpdateAt(LocalDateTime.now());
            applicationRepository.save(app);

            recruitmentSelectionCount.put(
                    app.getRecruitmentId(),
                    currentCount + 1
            );

            createPaymentInfo(app);
            createTBCertificateInfo(app);

            passedCount++;
        }

        return passedCount;
    }

    private Map<Long, Integer> getRecruitmentCapacities() {
        List<Recruitment> recruitments = recruitmentRepository.findAll();
        Map<Long, Integer> capacities = new HashMap<>();

        for (Recruitment recruitment : recruitments) {
            capacities.put(recruitment.getId(), recruitment.getCapacity());
        }

        return capacities;
    }

    private boolean hasAvailableRoomForType(Long recruitmentId, int roomType) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new RuntimeException("모집 정보를 찾을 수 없습니다."));

        // 해당 생활관의 특정 룸타입의 총 수용가능 인원 계산
        int totalCapacityForType = roomRepository
                .countAvailableCapacityByDormitoryAndType(
                        dormitoryRepository.findByDormName(recruitment.getDormName())
                                .orElseThrow(() -> new RuntimeException("생활관 정보를 찾을 수 없습니다."))
                                .getId(),
                        roomType
                );

        // 이미 선발된 인원 확인
        int selectedCount = applicationRepository
                .countSelectedApplicationsByRecruitmentIdAndRoomType(
                        recruitmentId,
                        roomType
                );

        return selectedCount < totalCapacityForType;
    }

    private void rejectOtherApplications(Long studentId, Long selectedApplicationId) {
        applicationRepository.rejectOtherApplications(studentId, selectedApplicationId);
    }

    private void createPaymentInfo(Application app) {
        String dormName = recruitmentRepository.findById(app.getRecruitmentId())
                .orElseThrow(() -> new RuntimeException("모집 정보를 찾을 수 없습니다."))
                .getDormName();

        int amount = 0;
        amount += feeManagementRepository
                .findByDormNameAndFeeType(dormName, "ROOM_" + app.getRoomType())
                .orElseThrow(() -> new RuntimeException("방 타입에 대한 요금 정보를 찾을 수 없습니다."))
                .getAmount();

        if (app.getMealType() > 0) {  // 식사 선택한 경우에만
            amount += feeManagementRepository
                    .findByDormNameAndFeeType(dormName, "MEAL_" + app.getMealType())
                    .orElseThrow(() -> new RuntimeException("식사 타입에 대한 요금 정보를 찾을 수 없습니다."))
                    .getAmount();
        }

        Payment payment = Payment.builder()
                .applicationId(app.getId())
                .amount(amount)
                .paymentStatus("미납")
                .paymentDate(null)
                .build();
        paymentRepository.save(payment);
    }


    private void createTBCertificateInfo(Application app) {
        TBCertificate certificate = TBCertificate.builder()
                .applicationId(app.getId())
                .image(null)
                .uploadedAt(null)
                .build();
        documentRepository.save(certificate);
    }

    @Override
    public Protocol assignRooms(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            List<Application> selectedStudents = applicationRepository.findAllByStatus("선발");
            Map<Long, Integer> updatedCapacities = new HashMap<>(); // recruitmentId -> remaining capacity

            // 1. 자격 검증 및 호실 배정
            for (Application app : selectedStudents) {
                boolean isPaymentCompleted = checkPaymentStatus(app.getId());
                boolean isTBCertificateSubmitted = checkTBCertificateStatus(app.getId());

                if (!isPaymentCompleted /*|| !isTBCertificateSubmitted*/) {
                    // 자격 미달자 처리
                    app.setStatus("거부");
                    app.setUpdateAt(LocalDateTime.now());
                    applicationRepository.save(app);
                    continue;
                }

                // 호실 배정 진행
                assignRoomToStudent(app);

                // 남은 수용 인원 카운트 업데이트
                Long recruitmentId = app.getRecruitmentId();
                updatedCapacities.merge(recruitmentId, -1, Integer::sum);
            }

            // 2. Recruitment 테이블 capacity 업데이트
            updateRecruitmentCapacities(updatedCapacities);

            String result = "호실 배정이 완료되었습니다.";
            response.setData(result.getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    private boolean checkPaymentStatus(Long applicationId) {
        return paymentRepository.findByApplicationId(applicationId)
                .map(payment -> "납부".equals(payment.getPaymentStatus()))
                .orElse(false);
    }

    private boolean checkTBCertificateStatus(Long applicationId) {
        return documentRepository.findByApplicationId(applicationId)
                .map(cert -> cert.getImage() != null && cert.getUploadedAt() != null)
                .orElse(false);
    }

    private void assignRoomToStudent(Application app) {
        // 해당 생활관의 사용 가능한 방 조회
        Recruitment recruitment = recruitmentRepository.findById(app.getRecruitmentId())
                .orElseThrow(() -> new RuntimeException("모집 정보를 찾을 수 없습니다."));

        Dormitory dormitory = dormitoryRepository.findByDormName(recruitment.getDormName())
                .orElseThrow(() -> new RuntimeException("생활관 정보를 찾을 수 없습니다."));

        List<Room> availableRooms = roomRepository.findAvailableRoomsByDormitoryAndType(
                dormitory.getId(),
                app.getRoomType()
        );

        if (availableRooms.isEmpty()) {
            throw new RuntimeException("배정 가능한 방이 없습니다.");
        }

        // 가장 적절한 방 선택
        Room selectedRoom = availableRooms.get(0);  // 또는 더 복잡한 방 선택 로직 구현

        // 사용 가능한 침대 번호 찾기
        String bedNumber = roomRepository.findAvailableBed(selectedRoom);

        // 호실 상태 저장
        RoomStatus roomStatus = RoomStatus.builder()
                .roomId(selectedRoom.getId())
                .studentId(app.getStudentId())
                .bedNumber(bedNumber)
                .build();

        roomStatusRepository.save(roomStatus);
    }

    private void updateRecruitmentCapacities(Map<Long, Integer> updatedCapacities) {
        for (Map.Entry<Long, Integer> entry : updatedCapacities.entrySet()) {
            Recruitment recruitment = recruitmentRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("모집 정보를 찾을 수 없습니다."));

            int currentCapacity = recruitment.getCapacity();
            int newCapacity = currentCapacity + entry.getValue();  // 감소된 인원만큼 차감

            recruitment.setCapacity(Math.max(0, newCapacity));
            recruitmentRepository.save(recruitment);
        }
    }
}