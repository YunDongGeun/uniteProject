package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.*;
import uniteProject.mvc.repository.*;
import uniteProject.mvc.service.interfaces.RoomAssignmentService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

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
            List<Application> applications = applicationRepository.findAllByOrderByPriorityScoreDesc();
            int passedCount = 0;

            for (Application app : applications) {
                if (Objects.equals(app.getStatus(), "대기")) {
                    app.setStatus("선발");
                    app.setUpdateAt(LocalDateTime.now());
                    applicationRepository.save(app);

                    String dormName = recruitmentRepository.findById(app.getRecruitmentId()).get().getDormName();

                    int amount = 0;
                    amount += feeManagementRepository.findByDormNameAndFeeType(dormName, "ROOM_"+app.getRoomType()).get().getAmount();
                    amount += feeManagementRepository.findByDormNameAndFeeType(dormName, "MEAL_"+app.getMealType()).get().getAmount();

                    // 1. 결제 정보 생성
                    Payment payment = Payment.builder()
                            .applicationId(app.getId())
                            .amount(amount)  // 기숙사비 금액
                            .paymentStatus("미납")  // 미납 상태
                            .paymentDate(null)   // 납부 전이므로 날짜는 null
                            .build();
                    paymentRepository.save(payment);

                    // 2. 결핵진단서 기본 엔트리 생성 (빈 데이터로)
                    TBCertificate certificate = TBCertificate.builder()
                            .applicationId(app.getId())
                            .image(null)
                            .uploadedAt(null)
                            .build();
                    documentRepository.save(certificate);

                    passedCount++;
                }
            }

            String result = String.format("총 %d명의 합격자가 선발되었습니다. 결제 정보와 결핵진단서 제출 정보가 생성되었습니다.", passedCount);
            response.setData(result.getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol assignRooms(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            List<Application> passedStudents = applicationRepository.findAllByStatus("PASSED");
            List<Room> availableRooms = roomRepository.findAllAvailableRooms();

            for (Application app : passedStudents) {
                Student student = studentRepository.findById(app.getStudentId())
                        .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));

                Room room = findSuitableRoom(availableRooms, student);

                RoomStatus roomStatus = RoomStatus.builder()
                        .roomId(room.getId())
                        .studentId(student.getId())
                        .bedNumber(findAvailableBed(room))
                        .build();

                roomStatusRepository.save(roomStatus);
            }

            String result = String.format("총 %d명의 학생 호실 배정이 완료되었습니다.", passedStudents.size());
            response.setData(result.getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    private Room findSuitableRoom(List<Room> rooms, Student student) {
        if (rooms == null || rooms.isEmpty()) {
            throw new RuntimeException("사용 가능한 방이 없습니다.");
        }
        Random random = new Random();
        return rooms.get(random.nextInt(rooms.size()));
    }

    private String findAvailableBed(Room room) {
        return roomRepository.findAvailableBed(room);
    }
}