package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;

public interface ScheduleService {
    Protocol getSchedule(byte[] data);
    Protocol getFees(byte[] data);
    Protocol registerSchedule(byte[] data);
    Protocol registerFees(byte[] data);

}
