package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.Application;
import uniteProject.mvc.model.Room;
import uniteProject.mvc.model.RoomStatus;
import uniteProject.mvc.model.Student;
import uniteProject.mvc.repository.*;
import uniteProject.mvc.service.interfaces.RoomAssignmentService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class RoomAssignmentServiceImpl implements RoomAssignmentService {
    private final RoomStatusRepository roomStatusRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
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
            for (Application app : applications) {
                app.setStatus("PASSED");
                applicationRepository.save(app);
            }

            String result = String.format("총 %d명의 합격자가 선발되었습니다.", applications.size());
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