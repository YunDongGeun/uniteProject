package uniteProject.domain.user.student;

public class Graduate extends Student {

    public Graduate(String name, String studentId, double gpa, double distanceToSchool) {
        super(name, studentId, gpa, distanceToSchool);
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