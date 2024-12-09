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
import java.util.concurrent.atomic.AtomicInteger;

public class DormitoryServer {
    private static final int PORT = 8888;
    private final RequestHandler requestHandler;
    private static final AtomicInteger clientCounter = new AtomicInteger(0);

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
        RecruitmentRepository recruitmentRepository = new RecruitmentRepository(PooledDataSource.getDataSource());

        // 각 Service 초기화
        AuthService authService = new AuthServiceImpl(memberRepository, studentRepository);
        ApplicationService applicationService = new ApplicationServiceImpl(applicationRepository, studentRepository, recruitmentRepository);
        ScheduleService scheduleService = new ScheduleServiceImpl(scheduleRepository, feeManagementRepository);

        RoomAssignmentService roomAssignmentService = new RoomAssignmentServiceImpl(
                roomStatusRepository, applicationRepository, studentRepository, roomRepository, paymentRepository,
                documentRepository, recruitmentRepository, feeManagementRepository, dormitoryRepository);

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
                int clientId = clientCounter.incrementAndGet();
                new Thread(new ClientHandler(clientSocket, requestHandler, clientId))
                        .start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final RequestHandler requestHandler;
        private final int clientId;
        private final String threadInfo;

        public ClientHandler(Socket socket, RequestHandler handler, int clientId) {
            this.clientSocket = socket;
            this.requestHandler = handler;
            this.clientId = clientId;
            this.threadInfo = Thread.currentThread().getName();
        }

        @Override
        public void run() {
            String clientAddress = clientSocket.getInetAddress().getHostAddress();
            int clientPort = clientSocket.getPort();

            System.out.printf("[Client %d] Connected - Address: %s:%d, Thread: %s%n",
                    clientId, clientAddress, clientPort, threadInfo);

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

                    System.out.printf("[Client %d] Received request - Type: %d, Code: %d, Length: %d%n",
                            clientId, type, code, length);

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

                    System.out.printf("[Client %d] Sent response - Type: %d, Code: %d%n",
                            clientId, response.getType(), response.getCode());
                }
            } catch (EOFException e) {
                System.out.printf("[Client %d] Disconnected - Address: %s:%d, Thread: %s%n",
                        clientId, clientAddress, clientPort, threadInfo);
            } catch (IOException e) {
                System.out.printf("[Client %d] Error occurred: %s%n", clientId, e.getMessage());
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