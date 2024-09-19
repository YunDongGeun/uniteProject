package uniteProject.domain.user.student;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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
    public abstract String getUserType();

    public abstract String getStudentType();
}