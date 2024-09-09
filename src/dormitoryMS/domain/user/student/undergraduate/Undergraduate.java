package dormitoryMS.domain.user.student.undergraduate;

import dormitoryMS.domain.user.student.Student;

public class Undergraduate extends Student {

    public Undergraduate(String name, String studentId, double gpa, String school, double distanceToSchool) {
        super(name, studentId, gpa, school, distanceToSchool);
    }

    @Override
    public String getUserType() {
        return "Undergraduate";
    }

    @Override
    public String getStudentType() {
        return "Undergraduate";
    }
}
