package uniteProject.server;

import uniteProject.global.Protocol;
import uniteProject.mvc.controller.RequestHandler;
import uniteProject.mvc.repository.*;
import uniteProject.mvc.service.impl.*;
import uniteProject.mvc.service.interfaces.*;
import uniteProject.persistence.PooledDataSource;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DormitoryServer {
    private static final int PORT = 8888;
    private final RequestHandler requestHandler;

    public DormitoryServer() {
        // 각 Repository 초기화
        MemberRepository memberRepository = new MemberRepository(PooledDataSource.getDataSource());
        StudentRepository studentRepository = new StudentRepository(PooledDataSource.getDataSource());
        ApplicationRepository applicationRepository = new ApplicationRepository(PooledDataSource.getDataSource());
        RoomRepository roomRepository = new RoomRepository(PooledDataSource.getDataSource());
        DormitoryRepository dormitoryRepository = new DormitoryRepository(PooledDataSource.getDataSource());
        RoomStatusRepository roomStatusRepository = new RoomStatusRepository(PooledDataSource.getDataSource());
        PaymentRepository paymentRepository = new PaymentRepository(PooledDataSource.getDataSource());
        DocumentRepository documentRepository = new DocumentRepository(PooledDataSource.getDataSource());
        WithdrawalRepository withdrawalRepository = new WithdrawalRepository(PooledDataSource.getDataSource());
        FeeManagementRepository feeManagementRepository = new FeeManagementRepository(PooledDataSource.getDataSource());
        ScheduleRepository scheduleRepository = new ScheduleRepository(PooledDataSource.getDataSource());

        // 각 Service 초기화
        AuthService authService = new AuthServiceImpl(memberRepository, studentRepository);
        ApplicationService applicationService = new ApplicationServiceImpl(applicationRepository, studentRepository);
        ScheduleService scheduleService = new ScheduleServiceImpl(scheduleRepository, feeManagementRepository);
        RoomAssignmentService roomAssignmentService = new RoomAssignmentServiceImpl(
                roomStatusRepository, applicationRepository, studentRepository, roomRepository, dormitoryRepository
        );
        PaymentService paymentService = new PaymentServiceImpl(paymentRepository, applicationRepository, studentRepository);
        DocumentService documentService = new DocumentServiceImpl(documentRepository, studentRepository, applicationRepository);
        WithdrawalService withdrawalService = new WithdrawalServiceImpl(withdrawalRepository, studentRepository);

        // RequestHandler 초기화
        this.requestHandler = new RequestHandler(
                authService, applicationService, scheduleService, roomAssignmentService, paymentService, documentService, withdrawalService
        );
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, requestHandler)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final RequestHandler requestHandler;

        public ClientHandler(Socket socket, RequestHandler handler) {
            this.clientSocket = socket;
            this.requestHandler = handler;
        }

        @Override
        public void run() {
            try (
                    BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
                    BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream());
                    DataInputStream in = new DataInputStream(bis);
                    DataOutputStream out = new DataOutputStream(bos)
            ) {
                while (true) {
                    // 프로토콜 헤더 읽기
                    byte type = in.readByte();
                    byte code = in.readByte();
                    short length = in.readShort();

                    // 데이터 읽기
                    byte[] data = null;
                    if (length > 0) {
                        data = new byte[length];
                        in.readFully(data);
                    }

                    // 요청 처리
                    Protocol request = new Protocol(type, code);
                    request.setData(data);
                    Protocol response = requestHandler.handleRequest(request);

                    // 응답 전송
                    out.writeByte(response.getType());
                    out.writeByte(response.getCode());

                    byte[] responseData = response.getData();
                    if (responseData != null) {
                        out.writeShort(responseData.length);
                        out.write(responseData);
                    } else {
                        out.writeShort(0);
                    }
                    out.flush();
                }
            } catch (EOFException e) {
                // 클라이언트 연결 종료
                System.out.println("Client disconnected");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        new DormitoryServer().start();
    }
}