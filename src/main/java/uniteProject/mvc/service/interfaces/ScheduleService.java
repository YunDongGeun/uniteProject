package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;

public interface ScheduleService {

    /**
     * 일정 조회
     * @param data 일정 리스트 데이터
     * @return 일정 리스트 데이터를 포함한 Protocol 객체
     */
    Protocol getSchedule(byte[] data);

    /**
     * 비용 조회
     * @param data 비용 리스트 데이터
     * @return 비용 리스트 데이터를 포함한 Protocol 객체
     */
    Protocol getFees(byte[] data);

    /**
     * 일정 등록
     * @param data 일정 등록 후 안내메시지
     * @return 일정 등록 후 안내메시지를 포함한 Protocol 객체
     */
    Protocol registerSchedule(byte[] data);

    /**
     * 비용 등록
     * @param data 비용 등록  후 안내메시지
     * @return 비용 등록  후 안내메시지를 포함한 Protocol 객체
     */
    Protocol registerFees(byte[] data);

}
