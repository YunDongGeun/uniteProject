package uniteProject.mvc.service.interfaces;

import uniteProject.persistence.dto.req_res.Response;

public interface RoomAssignmentService {
    Response checkPassStatus(byte[] data);
    Response checkRoomInfo(byte[] data);
    Response selectPassedStudents(byte[] data);
    Response assignRooms(byte[] data);

    byte[] handleRoomAssignmentRequest(byte code, byte[] data);
}