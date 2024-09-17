package uniteProject.domain.user.student;

public class Undergraduate extends Student {

    public Undergraduate(String name, String studentId, double gpa, String school, double distanceToSchool) {
        super(name, studentId, gpa, distanceToSchool);
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
