package dormitoryMS.domain.user.student.graduate;

import dormitoryMS.domain.user.student.Student;

public class Graduate extends Student {

    public Graduate(String name, String studentId, double gpa, String school, double distanceToSchool) {
        super(name, studentId, gpa, school, distanceToSchool);
    }

    @Override
    public String getUserType() {
        return "Graduate";
    }

    @Override
    public String getStudentType() {
        return "Graduate";
    }
}