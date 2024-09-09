package dormitoryMS.domain.user.student;

public abstract class Student{
    protected String name;
    protected String studentId;  // 8자리 숫자
    protected double gpa;
    protected String school;
    protected double distanceToSchool;

    public Student(String name, String studentId, double gpa, String school, double distanceToSchool) {
        this.name = name;
        this.studentId = studentId;
        this.gpa = gpa;
        this.school = school;
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

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
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