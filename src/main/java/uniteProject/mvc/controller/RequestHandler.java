package uniteProject.mvc.controller;

import lombok.RequiredArgsConstructor;
import uniteProject.global.Protocol;
import uniteProject.mvc.service.interfaces.*;

@RequiredArgsConstructor
public class RequestHandler {
    private final AuthService authService;
    private final ApplicationService applicationService;
    private final ScheduleService scheduleService;
    private final RoomAssignmentService roomAssignmentService;
    private final PaymentService paymentService;
    private final DocumentService documentService;
    private final WithdrawalService withdrawalService;

    public Protocol handleRequest(Protocol protocol) {
        try {
            if (!protocol.isValid()) {
                return new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_INVALID_REQ);
            }

            return switch (protocol.getType()) {
                case Protocol.TYPE_AUTH -> handleAuth(protocol);
                case Protocol.TYPE_APPLICATION -> handleApplication(protocol);
                case Protocol.TYPE_SCHEDULE -> handleSchedule(protocol);
                case Protocol.TYPE_ROOM -> handleRoomAssignment(protocol);
                case Protocol.TYPE_PAYMENT -> handlePayment(protocol);
                case Protocol.TYPE_DOCUMENT -> handleDocument(protocol);
                case Protocol.TYPE_WITHDRAWAL -> handleWithdrawal(protocol);
                case Protocol.TYPE_REGISTER -> handleRegister(protocol);
                default -> new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_INVALID_REQ);
            };
        } catch (Exception e) {
            Protocol response = new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_FAIL);
            response.setData(e.getMessage());
            return response;
        }
    }

    private Protocol handleAuth(Protocol protocol) {
        return switch (protocol.getCode()) {
            case Protocol.CODE_AUTH_ID_REQ -> authService.validateId(protocol.getData());
            case Protocol.CODE_AUTH_PW_REQ -> authService.validatePassword(protocol.getData());
            default -> new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_INVALID_REQ);
        };
    }

    private Protocol handleApplication(Protocol protocol) {
        return switch (protocol.getCode()) {
            case Protocol.CODE_APPLICATION_SUBMIT -> applicationService.submitApplication(protocol.getData());
            case Protocol.CODE_APPLICATION_STATUS -> applicationService.checkApplicationStatus(protocol.getData());
            case Protocol.CODE_APPLICATION_LIST -> applicationService.getApplicationList(protocol.getData());
            default -> new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_INVALID_REQ);
        };
    }

    private Protocol handleSchedule(Protocol protocol) {
        return switch (protocol.getCode()) {
            case Protocol.CODE_SCHEDULE_VIEW -> scheduleService.getSchedule(protocol.getData());
            case Protocol.CODE_SCHEDULE_FEE_VIEW -> scheduleService.getFees(protocol.getData());
            case Protocol.CODE_SCHEDULE_REG -> scheduleService.registerSchedule(protocol.getData());
            case Protocol.CODE_SCHEDULE_FEE_REG -> scheduleService.registerFees(protocol.getData());
            default -> new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_INVALID_REQ);
        };
    }

    private Protocol handleRoomAssignment(Protocol protocol) {
        return switch (protocol.getCode()) {
            case Protocol.CODE_ROOM_PASS_CHECK -> roomAssignmentService.checkPassStatus(protocol.getData());
            case Protocol.CODE_ROOM_INFO -> roomAssignmentService.checkRoomInfo(protocol.getData());
            case Protocol.CODE_ROOM_SELECT -> roomAssignmentService.selectPassedStudents(protocol.getData());
            case Protocol.CODE_ROOM_ASSIGN -> roomAssignmentService.assignRooms(protocol.getData());
            default -> new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_INVALID_REQ);
        };
    }

    private Protocol handlePayment(Protocol protocol) {
        return switch (protocol.getCode()) {
            case Protocol.CODE_PAYMENT_AMOUNT -> paymentService.getPaymentAmount(protocol.getData());
            case Protocol.CODE_PAYMENT_PAY -> paymentService.processPayment(protocol.getData());
            case Protocol.CODE_PAYMENT_PAID_LIST -> paymentService.getPaidList(protocol.getData());
            case Protocol.CODE_PAYMENT_UNPAID_LIST -> paymentService.getUnpaidList(protocol.getData());
            default -> new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_INVALID_REQ);
        };
    }

    private Protocol handleDocument(Protocol protocol) {
        return switch (protocol.getCode()) {
            case Protocol.CODE_DOCUMENT_SUBMIT -> documentService.submitTBCertificate(protocol.getData());
            case Protocol.CODE_DOCUMENT_STATUS -> documentService.checkSubmissionStatus(protocol.getData());
            default -> new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_INVALID_REQ);
        };
    }

    private Protocol handleWithdrawal(Protocol protocol) {
        return switch (protocol.getCode()) {
            case Protocol.CODE_WITHDRAWAL_REQ -> withdrawalService.submitWithdrawal(protocol.getData());
            case Protocol.CODE_WITHDRAWAL_STATUS -> withdrawalService.checkRefundStatus(protocol.getData());
            case Protocol.CODE_WITHDRAWAL_LIST -> withdrawalService.getWithdrawalList(protocol.getData());
            case Protocol.CODE_WITHDRAWAL_REFUND -> withdrawalService.processRefund(protocol.getData());
            default -> new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_INVALID_REQ);
        };
    }

    private Protocol handleRegister(Protocol protocol) {
        return switch (protocol.getCode()) {
            case Protocol.CODE_REGISTER_REQUEST -> authService.register(protocol.getData());
            default -> new Protocol(Protocol.TYPE_RESPONSE, Protocol.CODE_INVALID_REQ);
        };
    }
}