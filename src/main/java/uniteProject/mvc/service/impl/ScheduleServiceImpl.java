package uniteProject.mvc.service.impl;

import lombok.RequiredArgsConstructor;
import uniteProject.global.Protocol;
import uniteProject.mvc.model.FeeManagement;
import uniteProject.mvc.model.Schedule;
import uniteProject.mvc.repository.FeeManagementRepository;
import uniteProject.mvc.repository.ScheduleRepository;
import uniteProject.mvc.service.interfaces.ScheduleService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final FeeManagementRepository feeRepository;

    @Override
    public Protocol getSchedule(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            List<Schedule> schedules = scheduleRepository.findAllByOrderByStartTime();
            StringBuilder resultBuilder = new StringBuilder();

            for (Schedule schedule : schedules) {
                resultBuilder.append(String.format("%s,%s,%s,%s\n",
                        schedule.getEventName(),
                        schedule.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        schedule.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        schedule.getId()
                ));
            }

            response.setData(resultBuilder.toString().getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol getFees(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            List<FeeManagement> fees = feeRepository.findAll();
            StringBuilder resultBuilder = new StringBuilder();

            for (FeeManagement fee : fees) {
                resultBuilder.append(String.format("%s,%s,%d\n",
                        fee.getDormName(),
                        fee.getFeeType(),
                        fee.getAmount()
                ));
            }

            response.setData(resultBuilder.toString().getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol registerSchedule(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            String[] scheduleData = new String(data, StandardCharsets.UTF_8).split(",");
            if (scheduleData.length < 3) {
                throw new IllegalArgumentException("필수 일정 정보가 부족합니다.");
            }

            Schedule schedule = Schedule.builder()
                    .eventName(scheduleData[0])
                    .startTime(LocalDateTime.parse(scheduleData[1]))
                    .endTime(LocalDateTime.parse(scheduleData[2]))
                    .build();

            scheduleRepository.save(schedule);
            response.setData("일정이 등록되었습니다.".getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }

    @Override
    public Protocol registerFees(byte[] data) {
        Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_SUCCESS);
        try {
            String[] feeData = new String(data, StandardCharsets.UTF_8).split(",");
            if (feeData.length < 3) {
                throw new IllegalArgumentException("필수 비용 정보가 부족합니다.");
            }

            FeeManagement fee = FeeManagement.builder()
                    .dormName(feeData[0])
                    .feeType(feeData[1])
                    .amount(Integer.parseInt(feeData[2]))
                    .build();

            feeRepository.save(fee);
            response.setData("비용이 등록되었습니다.".getBytes());
        } catch (Exception e) {
            response.setCode(Protocol.CODE_FAIL);
            response.setData(e.getMessage().getBytes());
        }
        return response;
    }
}