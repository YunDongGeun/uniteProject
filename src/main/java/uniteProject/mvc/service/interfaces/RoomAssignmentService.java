package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;

public interface RoomAssignmentService {
    Protocol checkPassStatus(byte[] data);
    Protocol checkRoomInfo(byte[] data);
    Protocol selectPassedStudents(byte[] data);
    Protocol assignRooms(byte[] data);

}