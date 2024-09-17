package dormitoryMS.domain.user.student.service;

import dormitoryMS.domain.user.student.Student;
import dormitoryMS.domain.user.student.repository.StudentRepository;

public class StudentService {
    private StudentRepository studentRepository;

    public StudentService() {
        this.studentRepository = new StudentRepository();
    }

    public void createStudent(Student student) {
        studentRepository.save(student);
    }

//    public Student getStudent(int id) {
//        return studentRepository.findStudentById(id);
//    }
}
