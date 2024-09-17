package dormitoryMS.domain.user.student.repository;

import dormitoryMS.domain.user.student.Student;
import dormitoryMS.domain.user.student.Graduate;
import dormitoryMS.domain.user.student.Undergraduate;
import dormitoryMS.global.jdbc.MySqlDBConnection;

import java.sql.*;

public class StudentRepository {

    MySqlDBConnection mySqlDBConnection = new MySqlDBConnection();

    public void save(Student student) {
        String sql = "INSERT INTO students (student_id, name, gpa, distance_to_school, student_type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = mySqlDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, student.getStudentId());
            pstmt.setString(2, student.getName());
            pstmt.setDouble(3, student.getGpa());
            pstmt.setDouble(5, student.getDistanceToSchool());
            pstmt.setString(6, student.getStudentType());

            pstmt.executeUpdate();

            if (student instanceof Undergraduate) {
                saveUndergraduate((Undergraduate) student);
            } else if (student instanceof Graduate) {
                saveGraduate((Graduate) student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveUndergraduate(Undergraduate undergraduate) {
        String sql = "INSERT INTO undergraduates (student_id, major) VALUES (?, ?)";
        try (Connection conn = mySqlDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, undergraduate.getStudentId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveGraduate(Graduate graduate) {
        String sql = "INSERT INTO graduates (student_id, research_area) VALUES (?, ?)";
        try (Connection conn = mySqlDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, graduate.getStudentId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
