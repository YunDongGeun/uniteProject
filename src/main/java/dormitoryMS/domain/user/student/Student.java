package dormitoryMS.domain.user.student;

public abstract class Student{
    protected String name;
    protected String studentId;  // 8자리 숫자
    protected double gpa; // 성적(학점)
    protected double distanceToSchool; // 학교와 집의 거리

    public Student(String name, String studentId, double gpa, double distanceToSchool) {
        this.name = name;
        this.studentId = studentId;
        this.gpa = gpa;
        this.distanceToSchool = distanceToSchool;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public double getDistanceToSchool() {
        return distanceToSchool;
    }

    public void setDistanceToSchool(double distanceToSchool) {
        this.distanceToSchool = distanceToSchool;
    }

    public abstract String getUserType();

    public abstract String getStudentType();
}