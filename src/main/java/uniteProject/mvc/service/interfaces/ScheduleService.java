package uniteProject.mvc.service.interfaces;

import uniteProject.persistence.dto.req_res.Response;

public interface ScheduleService {
    Response getSchedule(byte[] data);
    Response getFees(byte[] data);
    Response registerSchedule(byte[] data);
    Response registerFees(byte[] data);

    byte[] handleScheduleRequest(byte code, byte[] data);
}
