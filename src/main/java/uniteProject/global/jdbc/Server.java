package uniteProject.global.jdbc;

import uniteProject.domain.user.student.service.StudentService;

public class Server {
    private static final int PORT = 8080;
    private final StudentService studentService;

    public Server() {
        this.studentService = new StudentService();
    }

//    public void start() {
//        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//            System.out.println("Server is listening on port " + PORT);
//
//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                System.out.println("New client connected");
//
//                new Thread(() -> handleClient(clientSocket)).start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void handleClient(Socket clientSocket) {
//        try (
//                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
//        ) {
//            String inputLine;
//            while ((inputLine = in.readLine()) != null) {
//                if ("GET_USERS".equals(inputLine)) {
//                    List<Student> students = studentService.getAllStudents();
//                    for (Student student : students) {
//                        out.println(student.toString());
//                    }
//                } else if (inputLine.startsWith("CREATE_USER:")) {
//                    String[] parts = inputLine.split(":");
//                    if (parts.length == 3) {
//                        Student student = new Student();
//                        student.setStudentname(parts[1]);
//                        student.setEmail(parts[2]);
//                        studentService.createStudent(student);
//                        out.println("Student created successfully");
//                    } else {
//                        out.println("Invalid input format");
//                    }
//                } else if ("EXIT".equals(inputLine)) {
//                    break;
//                } else {
//                    out.println("Unknown command");
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                clientSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}