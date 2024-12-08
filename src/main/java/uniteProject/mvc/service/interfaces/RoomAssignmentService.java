package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;

public interface RoomAssignmentService {

    /**
     * 합격 여부 조회
     * @param data 합격 여부 데이터
     * @return 합격 여부 데이터를 포함한 Protocol 객체
     */
    Protocol checkPassStatus(byte[] data);

    /**
     * 학생에게 배정된 호실 정보 조회
     * @param data 학생에게 배정된 호실 정보 데이터
     * @return 학생에게 배정된 호실 정보 데이터를 포함한 Protocol 객체
     */
    Protocol checkRoomInfo(byte[] data);

    /**
     * 합격자 선발
     * @param data 합격자 선발 진행 후 n명의 협격자 선발 안내 메시지
     * @return 합격자 선발 진행 후 n명의 협격자 선발 안내 메시지를 포함한 Protocol 객체
     */
    Protocol selectPassedStudents(byte[] data);

    /**
     * 호실 배정
     * @param data 호실 배정 후 n명의 호실 배정 안내 메시지
     * @return 호실 배정 후 n명의 호실 배정 안내 메시지를 포함한 Protocol 객체
     */
    Protocol assignRooms(byte[] data);

}